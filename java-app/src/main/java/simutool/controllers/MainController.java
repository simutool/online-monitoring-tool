package simutool.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import simutool.CSVprocessor.ExperimentSaver;
import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Panel;
import simutool.models.Simulation;
import simutool.repos.CommentsRepo;
import simutool.repos.SavedSimulationsRepo;

@Controller
public class MainController {
	
	static Simulation pendingSimulation;
	static Panel pendingPanel;
	static List<Panel> pendingPanels;
	String redirectLink; 
	String refreshingPar;
	
	@Autowired
	private SavedSimulationsRepo simRepo;
	
	@Autowired
	private CommentsRepo comments;
	
	@Autowired
	private InfluxPopulator influx;
	
	@Autowired
	private Parser parser;

	@Autowired
	private ExperimentSaver saver;
	
	@Value("${grafana.host}")
	private String grafanaHost;
	
	/**
	 * Starts index page
	 * @param m Model for passing attributes to template
	 * @return index page
	 */
	@RequestMapping("/home")
		public String startMenu(Model m) {
			m.addAttribute("saved", simRepo.getAllSavedSimulations());
			return "index";
	}
	
	
	@RequestMapping("/load")
	public String loadSavedSimulation(@RequestParam("id") int id) {
		return "redirect://" + grafanaHost;
	}
	
	/**
	 * Adds currently selected simulation data to model and passes it to the template
	 * @return template for simulation dashboard
	 */
	@GetMapping("/newsimulation")
	public String getSettingsForm(Model m) {
		if(pendingSimulation == null) {
			pendingSimulation = new Simulation();
		}
		if(pendingPanels == null) {
			pendingPanels = new ArrayList<Panel>();
		}
		if(pendingPanel == null) {
			pendingPanel = new Panel();
		}
		m.addAttribute("simulation", pendingSimulation);
		m.addAttribute("simulationName", pendingSimulation.getName());
		m.addAttribute("panel", pendingPanel);
		m.addAttribute("pendingPanels", pendingPanels);
		return "new-sim";
	}
	
	/**
	 * Returns new simulation template with a form for submitting new panel
	 * @param m model for passing arguments to template
	 * @param id number of "new panel" container that was clicked (0 to 2), needed to preserve consistency 
	 * @param simName current simulation name, needs to be updated in case if user changes it meanwhile
	 * @return new simulation template with toggled modal window for adding panel
	 */
	@GetMapping("/newpanel/{id}")
	public String getPanelForm(Model m, @PathVariable(value="id") Integer id, @RequestParam(value="simulation") String simName) {
		if(pendingPanels.size() > id) {
			pendingPanel = pendingPanels.get(id);
			m.addAttribute("edit", true);
		}else {
			pendingPanel = new Panel();
		}

		pendingSimulation.setName(simName);

		m.addAttribute("modal", true);
		m.addAttribute("simulation", pendingSimulation);
		m.addAttribute("simulationName", pendingSimulation.getName());
		m.addAttribute("panel", pendingPanel);
		m.addAttribute("pendingPanels", pendingPanels);
		return "new-sim";
	}
	
	/**
	 * Processes submitted simulation form
	 * @param simulation simulation model with prefilled fields
	 * @param empty model for passing attributes to the template
	 * @param redirectAttributes allows pass attributes to another method when redirecting
	 * @return redirect to grafana dashboard if form is valid, otherwise new simulation template with comments on errors
	 */
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
				boolean allPanelsAreStatic = true;
				int longestDuration = 0;
				for(Panel p : pendingPanels) {
					if(p.getSensorPathDTO() != null) {
						p.getSensorPathDTO().setNumber(counter);
						allPanelsAreStatic = false;
					}
					sens.add(p.getSensorPathDTO());
					if(p.getSimulationPathDTO() != null) {
						p.getSimulationPathDTO().setNumber(counter);
						longestDuration = Math.max(longestDuration, p.getSimulationPathDTO().getDuration());
					}
					sims.add(p.getSimulationPathDTO());
					if(p.getCuringCyclePathDTO() != null) {
						p.getCuringCyclePathDTO().setNumber(counter);
						longestDuration = Math.max(longestDuration, p.getCuringCyclePathDTO().getDuration());
					}
					cur.add(p.getCuringCyclePathDTO());
					counter++;
				}

				influx.tearDownTables();
				influx.addStaticPoints(sims, "simulation");
				influx.addStaticPoints(cur, "curing_cycle");
				influx.simulateSensor(1000, sens);
				refreshingPar = allPanelsAreStatic ? "?from=now-1m&to=now%2B" + (longestDuration+2) + "m" : "?orgId=1&refresh=1s"; 
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
				return "redirect://" + grafanaHost + redirectLink + refreshingPar;
			}
			return "redirect:/newsimulation";
	}
	
	/**
	 * Processes submitted form with new panel
	 * @param panel panel model with prefilled fields
	 * @param request http request (needed to extract uploaded files)
	 * @param edited tells whether user is creating a new panel or editing an existing one
	 * @param m empty model for passing attributes to the template
	 * @param redirectAttributes allows to pass attributes to another method when redirecting
	 * @return redirect to updated new simulation template if submitted form is valid, otherwise new panel template with comments on errors 
	 */
	@PostMapping(value="/newpanel", consumes = "multipart/form-data")
		public String savePanel(@ModelAttribute Panel panel, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> corruptedFiles = new ArrayList<String>(0);
		boolean edit = edited != null;
		
		System.out.println("edit: " + edit);
			try {
				panel.setSensorPath(multipartRequest.getPart("sensorPath").getSubmittedFileName());
					InputStream sensorStream = multipartRequest.getPart("sensorPath").getInputStream();
					Reader r = new InputStreamReader(sensorStream);
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
				
				if(corruptedFiles.size() > 0) {
					redirectAttributes.addFlashAttribute("panelError", "Following datasets could not be parsed: " + String.join(", ", corruptedFiles) );
				}else if(!panel.filesAreCSV()) {
					redirectAttributes.addFlashAttribute("panelError", "Only CSV files are allowed");
				}else if(panel.allPathsEmpty() && !edit){
					redirectAttributes.addFlashAttribute("panelError", "You must pick at least one dataset");
				}else if(panel.getName() == null || panel.getName().length()<1){
					redirectAttributes.addFlashAttribute("panelError", "Name shall not be empty");
				}else {

						for(Panel p : pendingPanels) {
							if(p.getId() == panel.getId()) {
								p.editPanel(panel);
								break;
							}
						}
						if(!edit) {
							panel.setFinalId();
							pendingPanels.add(panel);	
						}
						return "redirect:/newsimulation";
				}
		
			return "redirect:/newpanel";
	}
	
	/**
	 * Removes added panel
	 * @param id id of the panel to be removed
	 * @return updated new simulation template
	 */
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
	
	@PostMapping("/saveComment/")
	public String sa(@RequestBody String reqBody) {
		
		return "redirect:/" + redirectLink;
	}
	
	@GetMapping("/savePanel")
	public String startSavingPanel(@RequestParam("id") int id) {
		saver.savePanel(pendingSimulation, pendingPanels.get(id), id+1);
		return "redirect://" + grafanaHost + redirectLink + refreshingPar;

	}
	
	@RequestMapping("/")
		public String redirectToHome() {
	    	return "redirect:/home";
	}
}



