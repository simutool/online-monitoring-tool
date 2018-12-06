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
		simRepo.readSavedSimulations();
		m.addAttribute("saved", simRepo.getSavedSimulations());
		return "index";
	}


	@RequestMapping("/load")
	public String loadSavedSimulation(@RequestParam("id") int id) {
		List<FileDTO> datasets = new ArrayList<FileDTO>();
		List<Panel> panels = simRepo.getSavedSimulations().get(id).getPanelList();
		for(Panel p: panels) {

			influx.addStaticPoints( datasets, "sensor" );
		}
		refreshingPar = "?from=now-1m&to=now%2B20m"; 
		switch(panels.size()){
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
	@GetMapping("/newpanel")
	public String getPanelForm(Model m, @RequestParam(value="simulation", required=false) String simName, HttpServletRequest request) {

		if(simName != null && simName.length() > 0) {
			pendingSimulation.setName(simName);
		}
		pendingPanel = new Panel();
		
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
			processPendingData();
			return "redirect://" + grafanaHost + redirectLink + refreshingPar;
		}
		return "redirect:/newsimulation";
	}
	
	/**
	 * Saves panel
	 */
	@PostMapping(value="/newpanel/{panelId}", consumes = "multipart/form-data", params = "new panel")
		public String savePanel(@ModelAttribute Panel panel,  @PathVariable(value="panelId") Integer id, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {
		
		boolean edit = edited != null;

		if(panel.getName().length() < 1) {
			redirectAttributes.addFlashAttribute("panelError", "Enter panel name");
			return  "redirect:/newpanel/" + id;
		}else if(id == 0) {
			pendingPanel.setFinalId();
			pendingPanel.setName(panel.getName());
			pendingPanels.add(pendingPanel);	
		}else {
			for(Panel p : pendingPanels) {
				if(p.getFinalId() == id) {
					p.editPanel(panel);
					break;
				}
			}
		}
		return  "redirect:/newsimulation/";
	}

	/**
	 * Saves panel
	 */
	@GetMapping(value="/editpanel/{panelId}")
		public String editPanelForm(@ModelAttribute Panel panel,  @RequestParam(value="simulation", required=false) String simName, @PathVariable(value="panelId") Integer id, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {
		
			for(Panel p : pendingPanels) {
				if(p.getFinalId() == id) {
					pendingPanel = p;
					break;
				}
			}
			if(simName != null && simName.length() > 0) {
				pendingSimulation.setName(simName);
			}
			m.addAttribute("modal", true);
			m.addAttribute("simulation", pendingSimulation);
			m.addAttribute("simulationName", pendingSimulation.getName());
			m.addAttribute("panel", pendingPanel);
			m.addAttribute("pendingPanels", pendingPanels);
			return "new-sim";
		
	}
	
	/**
	 * Adds file to the panel
	 * @param panel panel model with prefilled fields
	 * @param request http request (needed to extract uploaded files)
	 * @param edited tells whether user is creating a new panel or editing an existing one
	 * @param m empty model for passing attributes to the template
	 * @param redirectAttributes allows to pass attributes to another method when redirecting
	 * @return redirect to updated new simulation template if submitted form is valid, otherwise new panel template with comments on errors 
	 */
	@PostMapping(value="/newpanel/{panelId}", consumes = "multipart/form-data", params = "new file")
	public String saveFile(@ModelAttribute Panel panel,  @PathVariable(value="panelId") Integer id, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {

		redirectAttributes.addFlashAttribute("pendingName", panel.getSimulationName());
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		boolean edit = edited != null;
		String panelName = panel.getName() == null || panel.getName().length()<1 ? (panel.getPendingFile().getType() + "_" + (panel.getFiles().size()+1)) : panel.getName();
		pendingPanel.setName(panelName);
		
		System.out.println("edit: " + edit);
		try {

			String submittedPath = (multipartRequest.getPart("pendingFile").getSubmittedFileName());
			System.out.println("submittedPath: " + submittedPath);
			InputStream sensorStream = multipartRequest.getPart("pendingFile").getInputStream();
			Reader r = new InputStreamReader(sensorStream);
			String extension = submittedPath.substring(submittedPath.lastIndexOf('.') + 1);
			if(!extension.equals("csv") && submittedPath.length() > 1) {
				System.out.println("here 1--- " + extension);
				redirectAttributes.addFlashAttribute("panelError", "Submitted file is invalid or missing" );
			}
			List<FileDTO> currentFiles = pendingPanel.getFiles();
			FileDTO fileToAdd = parser.parseFilesForPanels(panel.getPendingFile().getType(), r);
			fileToAdd.setName(panel.getPendingFile().getName());
			currentFiles.add( fileToAdd );
			System.out.println("fileToAdd: " + fileToAdd.getRows());
			pendingPanel.setFiles(currentFiles);

		}catch (Exception e) {
			redirectAttributes.addFlashAttribute("panelError", "Submitted file is invalid or missing");
			e.printStackTrace();
		}

		return  "redirect:/editpanel/" + id;
	}

	public void processPendingData() {
		List<FileDTO> sens  = new ArrayList<FileDTO>();
		List<FileDTO> stats  = new ArrayList<FileDTO>();
		int counter = 1;
		boolean allPanelsAreStatic = true;
		int longestDuration = 0;
		for(Panel p : pendingPanels) {

			for(FileDTO file : p.getFiles()) {
				if(file.getType().equals("Sensor")) {
					sens.add(file);
					allPanelsAreStatic = false;
				}else {
					stats.add(file);
				}
				file.setNumber(counter);
				longestDuration = Math.max(longestDuration, file.findDuration());
				
			}
			counter++;
		}

		influx.tearDownTables();
		influx.addStaticPoints(stats, "simulation".replace(' ', '_'));
		influx.simulateSensor(1000, sens);
		refreshingPar = allPanelsAreStatic ? "?from=now-4m&to=now%2B" + (longestDuration+2) + "m" : "?orgId=1&refresh=1s"; 
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
			if(p.getFinalId() == id){
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



