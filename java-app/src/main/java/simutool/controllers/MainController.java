package simutool.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Panel;
import simutool.models.Simulation;
import simutool.repos.SavedSimulationsRepo;

@Controller
public class MainController {
	
	Simulation pendingSimulation;
	List<Panel> pendingPanels;
	
	@Autowired
	private SavedSimulationsRepo simRepo;
	
	@Autowired
	private InfluxPopulator influx;
	
	@Autowired
	private Parser parser;
	
	@Value("${grafana.host}")
	private String grafanaHost;
	

	@RequestMapping("/home")
		public String startMenu(Model m) {
			m.addAttribute("saved", simRepo.getAllSavedSimulations());
			return "index";
	}
	
	@RequestMapping("/load")
	public String loadSavedSimulation(@RequestParam("id") int id) {
	//	influx.getAllSavedSimulations();
		return "redirect://" + grafanaHost;
	}
	
	@GetMapping("/newsimulation")
	public String getSettingsForm(Model m) {
		if(pendingSimulation == null) {
			pendingSimulation = new Simulation();
		}
		if(pendingPanels == null) {
			pendingPanels = new ArrayList<Panel>();
		}
		m.addAttribute("simulation", pendingSimulation);
		m.addAttribute("simulationName", pendingSimulation.getName());
		System.out.println("pending sim name: " + pendingSimulation.getName());
		m.addAttribute("panel", new Panel());
		m.addAttribute("pendingPanels", pendingPanels);
		return "new-sim";
	}
	
	@PostMapping("/newsimulation")
	    public String saveSimulation(@ModelAttribute Simulation simulation, Model m) {
			if(simulation.getName() == null || simulation.getName().length() < 1) {
				m.addAttribute("error", "Enter a name");
			}else if(simRepo.simulationNameExists(simulation.getName())) {
				m.addAttribute("error", "Simulation with this name already exists");
			}else if(pendingPanels.size() < 1) {
				m.addAttribute("error", "You must add at least 1 graph");
			}
			else {
				List<FileDTO> sens  = parser.parseFilesForPanels(pendingPanels, "sensor");
				List<FileDTO> sims  = parser.parseFilesForPanels(pendingPanels, "simulation");
				List<FileDTO> cur = parser.parseFilesForPanels(pendingPanels, "curing_cycle");

				influx.tearDownTables();
				influx.addSimulationPoints(sims, "simulation");
				influx.addSimulationPoints(cur, "curing_cycle");
				influx.simulateSensor(1000, sens);
				String redirectLink;
				switch(pendingPanels.size()){
					case 1:{
						redirectLink = "d/ibjZzy-iz/1-panel-monitoring";
						break;
					}
					case 2:{
						redirectLink = "d/hUg4ks-ik/2-panel-monitoring";
						break; 
					}
					default:{
						redirectLink = "d/OSF-tramk/3-panels-monitoring";
					}
				}
				return "redirect://" + grafanaHost + redirectLink;
			}
			m.addAttribute("simulation", pendingSimulation);


			m.addAttribute("panel", new Panel());
			m.addAttribute("pendingPanels", pendingPanels);
			return "new-sim";
	}
	
	@PostMapping("/newpanel")
		public String savePanel(@ModelAttribute Panel panel, Model m, final RedirectAttributes redirectAttributes) {
			redirectAttributes.addFlashAttribute("pendingName", panel.getSimulationName());
			pendingSimulation.setName(panel.getSimulationName());

			if(!panel.filesAreCSV()) {
			//	m.addAttribute("panelError", "Only CSV files are allowed");
				redirectAttributes.addFlashAttribute("panelError", "Only CSV files are allowed");
			}else if(panel.allPathsEmpty()){
				redirectAttributes.addFlashAttribute("panelError", "You must pick at least one dataset");
			}else if(panel.getName() == null || panel.getName().length()<1){
				redirectAttributes.addFlashAttribute("panelError", "Name shall not be empty");
			}else {
				boolean panelWasEdited = false;
				for(Panel p : pendingPanels) {

					if(p.getId() == panel.getId()) {
						p.setName(panel.getName());
						p.setSensorPath(panel.getSensorPath());
						p.setSimulationPath(panel.getSimulationPath());
						p.setCuringCyclePath(panel.getCuringCyclePath());
						panelWasEdited = true;
					}
				}
				if(!panelWasEdited) {
					panel.setFinalId();
					pendingPanels.add(panel);	
				}
			}
			return "redirect:/newsimulation";
	}
	
	@GetMapping("/removePanel/{id}")
	public String removePanel(@PathVariable(value="id") Long id) {

		Panel panelToRemove = null;
		for(Panel p : pendingPanels) {	
			if(p.getId() == id){
				panelToRemove = p;
				break;
			}
		}
		if(panelToRemove != null) {
			pendingPanels.remove(panelToRemove);
		}
		return "redirect:/newsimulation";
}
	
	@RequestMapping("/")
		public String redirectToHome() {
	    	return "redirect:/home";
	}
}



