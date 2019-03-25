package simutool.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

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
import simutool.models.InputJSON;
import simutool.models.Panel;
import simutool.models.Simulation;
import simutool.repos.CommentsRepo;
import simutool.repos.SavedSimulationsRepo;

@Controller
public class MainController {

	public static Simulation pendingSimulation;
	static Panel pendingPanel;
	public static List<Panel> pendingPanels;
	public static boolean staticsAreLaunched = false;
	public static boolean experimentStarted = false;
	boolean allPanelsAreStatic = true;
	String redirectLink; 
	String refreshingPar;
	public static List<InputJSON> meta;

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

	/**
	 * Loads selected simulation
	 * @param id uri of experiment to load
	 * @return static grafana view 
	 */
	@RequestMapping("/load")
	public String loadSavedSimulation(@RequestParam("id") String id) {
		influx.tearDownTables();
		Simulation s = simRepo.getSimulationById(id);
		if(s.getCommentsFile() != null) {
			simRepo.writeCommentsToDB(s.getCommentsFile());
		}
		List<Panel> panels = s.getPanelList();
		int counter = 1;
		for(Panel p: panels) {
			for(FileDTO f : p.getFiles()) {
			//	f.setNumber(counter);
				f.setEarliestTime(s.getEarliestTime());
				f.setLatestTime(s.getLatestTime());
				List<FileDTO> dataset = new ArrayList<FileDTO>();
				dataset.add(f);
				influx.addStaticPoints( dataset, f.getType() );
			}
			s.setLoaded(true);
			counter++;
		}
		pendingSimulation = s;
		pendingPanels = s.getPanelList();
		refreshingPar = "?from=" + s.getEarliestTime() + "&to=" + s.getLatestTime(); 
		experimentStarted = true;
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
		pendingSimulation.setGrafanaURL(grafanaHost + redirectLink + refreshingPar);
		return "redirect://" + pendingSimulation.getGrafanaURL();
	}

	/**
	 * Adds currently selected simulation data to model and passes it to the template
	 * @return template for simulation dashboard
	 */
	@GetMapping("/newsimulation")
	public String getSettingsForm(Model m) {
		System.out.println("pendingSimulation 1: "+pendingSimulation);
		if(experimentStarted || pendingSimulation == null) {
			pendingSimulation = new Simulation();
			System.out.println("pendingSimulation 2: "+pendingSimulation);
		//	parser.parseMetadata(pendingSimulation, null);
			meta = parser.parseJsonMetadata(pendingSimulation, null);

			System.out.println("pendingSimulation 3: "+pendingSimulation);
			pendingPanels = null;
			pendingPanel = null;
			experimentStarted = false;
		}
		if(pendingPanels == null) {
			pendingPanels = new ArrayList<Panel>();
		}
		if(pendingPanel == null) {
			pendingPanel = new Panel();
		}
		System.out.println("arr" + meta);
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(new Date());
		pendingSimulation.setCreated( nowAsISO );
		pendingSimulation.setDate( Calendar.getInstance().getTime().toLocaleString() );


		m.addAttribute("simulation", pendingSimulation);
		m.addAttribute("simulationName", pendingSimulation.getName());
		m.addAttribute("json", meta);
		m.addAttribute("panel", pendingPanel);
		m.addAttribute("pendingPanels", pendingPanels);
		return "new-sim";
	}

	/**
	 * Returns new simulation template with a form for submitting new panel
	 * @param m model for passing arguments to template
	 * @param simName current simulation name, needs to be updated in case if user changes it meanwhile
	 * @return new simulation template with toggled modal window for adding panel
	 */
	@GetMapping("/newpanel")
	public String getPanelForm(Model m, @RequestParam(value="simulation", required=false) String simName) {

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
	
	@GetMapping(value="/newpanel/{panelId}", consumes = "multipart/form-data", params = "new panel")
	public String redirectToEditPanel(@ModelAttribute Panel panel,  @PathVariable(value="panelId") Integer id, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {

		return  "redirect:/editpanel/" + id;
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
			experimentStarted = true;
			pendingSimulation.setGrafanaURL(grafanaHost + redirectLink + refreshingPar);
			return "redirect://" + pendingSimulation.getGrafanaURL();
		}
		return "redirect:/newsimulation";
	}
	
	/** 
	 *  Saves panel
	 *  @param panel Panel instance passed by user
	 *  @param id shows panel id. <br> if id is not zero, we are editing an existing panel. Otherwise  panel is new
	 */
	@PostMapping(value="/newpanel/{panelId}", consumes = "multipart/form-data", params = "new panel")
		public String savePanel(@ModelAttribute Panel panel,  @PathVariable(value="panelId") Integer id, HttpServletRequest request, @RequestParam(name = "edit", required=false) Integer edited, Model m, final RedirectAttributes redirectAttributes) {
		System.out.println("post new panel");

		boolean edit = edited != null;

		if(panel.getName().length() < 1) {
			redirectAttributes.addFlashAttribute("panelError", "Enter panel name");
			System.out.println("no panel name entered");
			return  "redirect:/editpanel/" + id;
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
System.out.println("adding file triggered");
		redirectAttributes.addFlashAttribute("pendingName", panel.getSimulationName());
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		boolean edit = edited != null;
		if(panel.getName() != null) {
			pendingPanel.setName( panel.getName() );
		}
		
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
			if(panel.getPendingFile().getName() == null || panel.getPendingFile().getName().length()<1) {
				fileToAdd.setName( panel.getPendingFile().getType() + "_" + (pendingPanel.getFiles().size()+1) );
			}else {
				fileToAdd.setName(panel.getPendingFile().getName());
			}
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
		int panelCounter = 1;
		int sensorCounter = 1;

		pendingSimulation.setPanelList(pendingPanels);
		for(Panel p : pendingPanels) {
			sensorCounter = 1;
			for(FileDTO file : p.getFiles()) {
				if(file.getType().equals("Sensor")) {
					sens.add(file);
					allPanelsAreStatic = false;
					file.setInternalNumber(sensorCounter);
					file.setPanelNumber(panelCounter);
					sensorCounter++;
				}
			}
			pendingSimulation.setLoaded(false);
			panelCounter++;
		}

		influx.tearDownTables();
		influx.simulateSensor(1000, sens);
		staticsAreLaunched = false;
		refreshingPar = "?orgId=1&refresh=1s"; 
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

	
	@RequestMapping("/reset")
	public String resetGrafanaLink() {
		return "redirect://" + pendingSimulation.getGrafanaURL();
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
	
	@GetMapping("/removeFile/{panelId}/{fileId}")
	public String removeFile(@PathVariable(value="panelId") Long panelId, @PathVariable(value="fileId") int fileId) {

		pendingPanel.getFiles().remove(pendingPanel.getFiles().get(fileId));
		
		return  "redirect:/editpanel/" + panelId;
	}
	

	@PostMapping("/saveComment/")
	public String saveComment(@RequestBody String reqBody) {

		return "redirect:/" + redirectLink;
	}

	@GetMapping("/savePanel/{exit}")
	public String startSavingPanel(@PathVariable(value="exit") boolean exit) {
		String id = UUID.randomUUID().toString();
		pendingSimulation.setId(id);
		saver.savePanels(pendingSimulation);
		if(exit) {
			simRepo.readSavedSimulations();
			System.out.println("Saved and exited");
			return "redirect:/load?id=" + id;
		}else {
			System.out.println("Saved and resumed");
			return "redirect:/reset";
		}
	}
	

	@RequestMapping("/")
	public String redirectToHome() {
		return "redirect:/home";
	}
}



