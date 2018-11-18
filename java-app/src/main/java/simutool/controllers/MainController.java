package simutool.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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
	    public String saveSimulation(@ModelAttribute Simulation simulation, Model m, final RedirectAttributes redirectAttributes) {
			if(simulation.getName() == null || simulation.getName().length() < 1) {
				redirectAttributes.addFlashAttribute("error", "Enter a name");
			}else if(simRepo.simulationNameExists(simulation.getName())) {
				redirectAttributes.addFlashAttribute("error", "Simulation with this name already exists");
			}else if(pendingPanels.size() < 1) {
				redirectAttributes.addFlashAttribute("error", "You must add at least 1 graph");
			}
			else {
				List<FileDTO> sens  = new ArrayList<FileDTO>();
				List<FileDTO> sims  = new ArrayList<FileDTO>();
				List<FileDTO> cur  = new ArrayList<FileDTO>();
				int counter = 1;
				for(Panel p : pendingPanels) {
					if(p.getSensorPathDTO() != null)
						p.getSensorPathDTO().setNumber(counter);
					sens.add(p.getSensorPathDTO());
					if(p.getSimulationPathDTO() != null)
						p.getSimulationPathDTO().setNumber(counter);
					sims.add(p.getSimulationPathDTO());
					if(p.getCuringCyclePathDTO() != null)
						p.getCuringCyclePathDTO().setNumber(counter);
					cur.add(p.getCuringCyclePathDTO());
					counter++;
				}

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
			return "redirect:/newsimulation";
	}
	
	@PostMapping(value="/newpanel", consumes = "multipart/form-data")
		public String savePanel(@ModelAttribute Panel panel, HttpServletRequest request, @RequestParam(name="sensorPath", required = false) MultipartFile file, Model m, final RedirectAttributes redirectAttributes) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

		List<String> corruptedFiles = new ArrayList<String>(0);
		System.out.println(1);
			try {
	
				System.out.println(3);

				panel.setSensorPath(multipartRequest.getPart("sensorPath").getSubmittedFileName());
				System.out.println(4);

					InputStream sensorStream = multipartRequest.getPart("sensorPath").getInputStream();
					System.out.println(5);

					Reader r = new InputStreamReader(sensorStream);
					System.out.println(6);

					System.out.println(7);
					String extension = multipartRequest.getPart("sensorPath").getSubmittedFileName().substring(multipartRequest.getPart("sensorPath").getSubmittedFileName().lastIndexOf('.') + 1);
					if(!extension.equals("csv") && panel.getSensorPath().length()>1) {
						System.out.println("panel.getSensorPath().length(): " + panel.getSensorPath().length());
						corruptedFiles.add("sensor");
					}
					panel.setSensorPathDTO( parser.parseFilesForPanels("sensor", r) );

			}catch (Exception e) {
				panel.setSensorPath(null);
				panel.setSensorPathDTO(null);
			}
			
			try {
		
				panel.setSimulationPath(multipartRequest.getPart("simulationPath").getSubmittedFileName());
					InputStream simulationStream = multipartRequest.getPart("simulationPath").getInputStream();
					Reader r = new InputStreamReader(simulationStream);	
					String extension = multipartRequest.getPart("simulationPath").getSubmittedFileName().substring(multipartRequest.getPart("simulationPath").getSubmittedFileName().lastIndexOf('.') + 1);
					if(!extension.equals("csv") && panel.getSimulationPath().length()>1) {
						corruptedFiles.add("simulation");
					}
					panel.setSimulationPathDTO( parser.parseFilesForPanels("simulation", r) );

			}catch (Exception e) {
				panel.setSimulationPath(null);
				panel.setSimulationPathDTO(null);
			}
			
			try {
				panel.setCuringCyclePath(multipartRequest.getPart("curingCyclePath").getSubmittedFileName());
				InputStream curingCycleStream = multipartRequest.getPart("curingCyclePath").getInputStream();
				Reader r = new InputStreamReader(curingCycleStream);	
				String extension = multipartRequest.getPart("curingCyclePath").getSubmittedFileName().substring(multipartRequest.getPart("curingCyclePath").getSubmittedFileName().lastIndexOf('.') + 1);
				if(!extension.equals("csv") && panel.getCuringCyclePath().length()>1) {
					corruptedFiles.add("curing cycle");
				}
				panel.setCuringCyclePathDTO( parser.parseFilesForPanels("curing_cycle", r) );

			}catch (Exception e) {
				panel.setCuringCyclePath( null );
				panel.setCuringCyclePathDTO( null );
			}
				
			redirectAttributes.addFlashAttribute("pendingName", panel.getSimulationName());
			pendingSimulation.setName(panel.getSimulationName());
			System.out.println("size: " + corruptedFiles.size());
				
				if(corruptedFiles.size() > 0) {
					redirectAttributes.addFlashAttribute("panelError", "Following datasets could not be parsed: " + String.join(", ", corruptedFiles) );
				}else if(!panel.filesAreCSV()) {
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
								p.setSensorPath(panel.getSensorPath());
								p.setSensorPathDTO(panel.getSensorPathDTO());
								p.setSimulationPathDTO(panel.getSimulationPathDTO());
								p.setSimulationPathDTO(panel.getSimulationPathDTO());
								p.setCuringCyclePathDTO(panel.getCuringCyclePathDTO());
								p.setCuringCyclePathDTO(panel.getCuringCyclePathDTO());
								panelWasEdited = true;
								break;
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



