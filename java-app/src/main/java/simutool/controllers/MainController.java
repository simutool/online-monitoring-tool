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
import simutool.DBpopulator.SensorEmulator;
import simutool.models.InputJSON;
import simutool.models.Panel;
import simutool.models.Simulation;
import simutool.repos.SavedSimulationsRepo;

@Controller
public class MainController {

	public static Simulation pendingSimulation;
	static Panel pendingPanel;
	public static List<Panel> pendingPanels;
	public static boolean staticsAreLaunched = false;
	public static String zipPath = null;
	public static boolean experimentStarted = false;
	boolean allPanelsAreStatic = true;
	String redirectLink; 
	String refreshingPar;
	public static List<InputJSON> meta;

	@Autowired
	private SavedSimulationsRepo simRepo;

	@Autowired
	private InfluxPopulator influx;

	@Autowired
	private Parser parser;

	@Autowired
	private ExperimentSaver saver;

	@Value("${grafana.host}")
	private String grafanaHost;
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	@Value("${importZIPfolder}")
	private String importZIPfolder;
	
	@Value("${simulated.sensor.interval}")
	private int interval;

	/**
	 * Starts index page
	 * @param m Model for passing attributes to template
	 * @return index page
	 */
	@RequestMapping("/home")
	public String startMenu(Model m) {
		if(zipPath == null) {
			simRepo.setSavedSimulations(null);
			simRepo.readSavedSimulations(savingFolder, null);
			simRepo.readSavedSimulations(importZIPfolder, null);
			simRepo.orderSimulations();

			m.addAttribute("saved", simRepo.getSavedSimulations());
			return "index";
		}else {
			simRepo.setSavedSimulations(null);
			simRepo.readSavedSimulations(importZIPfolder, zipPath);
			zipPath = null;
			return "redirect:/load?id=" + simRepo.getSavedSimulations().get(0).getId();
		}
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

		for(Panel p: panels) {
			for(FileDTO f : p.getFiles()) {
				f.setEarliestTime(s.getEarliestTime());
				f.setLatestTime(s.getLatestTime());
				List<FileDTO> dataset = new ArrayList<FileDTO>();
				dataset.add(f);
				influx.addStaticPoints( dataset, f.getType() );
			}
			s.setLoaded(true);
		}
		pendingSimulation = s;
		pendingPanels = s.getPanelList();
		
		// Defines perfect scale from earliest timestamp in experiment to the last one
		refreshingPar = "?from=" + s.getEarliestTime() + "&to=" + s.getLatestTime(); 
		experimentStarted = true;
		
		// Collect redirect url depending on the number of panels
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

		// If called for the first time, when simulation has no pending data yet, set pendingSimulation to a new Simulation
		if(experimentStarted || pendingSimulation == null) {
			pendingSimulation = new Simulation();

			// Parse metadata and save it in a global variable
			meta = parser.parseJsonMetadata( null);

			// Clear pending panel data
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
		
		// Save start date and time 
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"); 
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(new Date());
		pendingSimulation.setCreated( nowAsISO );
		pendingSimulation.setDate( Calendar.getInstance().getTime().toLocaleString() );

		// Send data to template
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
		m.addAttribute("json", meta);
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
		
		if(pendingPanel.getFiles() == null || pendingPanel.getFiles().size() < 1) {
			redirectAttributes.addFlashAttribute("panelError", "You must add at least one dataset");
			return  "redirect:/editpanel/" + id;

		}
		if(panel.getName().length() < 1) {
			int lastPanel = MainController.pendingPanels.size();
			for(Panel p : MainController.pendingPanels) {
				try {
					lastPanel = Math.max(Integer.parseInt(p.getName().substring(p.getName().length()-1)), lastPanel);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			panel.setName("Panel " + (lastPanel+1));
		}
		if(id == 0) {
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
	 * Saves changes to a panel
	 */
	@GetMapping(value="/editpanel/{panelId}")
		public String editPanelForm(@ModelAttribute Panel panel,  @RequestParam(value="simulation", required=false) String simName, @PathVariable(value="panelId") Integer id, Model m, final RedirectAttributes redirectAttributes) {
		
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
			m.addAttribute("json", meta);
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
	public String saveFile(@ModelAttribute Panel panel,  @PathVariable(value="panelId") Integer id, HttpServletRequest request, Model m, final RedirectAttributes redirectAttributes) {

		redirectAttributes.addFlashAttribute("pendingName", panel.getSimulationName());
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;


		if(panel.getName() != null) {
			pendingPanel.setName( panel.getName() );
		}
		
		if(panel.getPendingFile().getType().toLowerCase().equals("sensor")) {
			FileDTO fileToAdd = new FileDTO();
			if(panel.getPendingFile().getName() == null || panel.getPendingFile().getName().length()<1) {
				fileToAdd.setName( panel.getPendingFile().getType() + "_" + (pendingPanel.getFiles().size()+1) );
			}else {
				fileToAdd.setName(panel.getPendingFile().getName());
			}
			fileToAdd.setStreamField( MainController.pendingPanel.getPendingFile().getStreamField() );
			fileToAdd.setPanelNumber( MainController.pendingPanel.getPendingFile().getPanelNumber() );
			fileToAdd.setInternalNumber( MainController.pendingPanel.getPendingFile().getInternalNumber() );

			fileToAdd.setDatasource_id( panel.getPendingFile().getDatasource_id() );

			fileToAdd.setType( panel.getPendingFile().getType() );
			List<FileDTO> currentFiles = pendingPanel.getFiles();
			currentFiles.add( fileToAdd );
			pendingPanel.setFiles(currentFiles);
			return  "redirect:/editpanel/" + id;

		}

		try {

			String submittedPath = (multipartRequest.getPart("pendingFile").getSubmittedFileName());
			InputStream sensorStream = multipartRequest.getPart("pendingFile").getInputStream();
			Reader r = new InputStreamReader(sensorStream);
			String extension = submittedPath.substring(submittedPath.lastIndexOf('.') + 1);
			if(!extension.equals("csv") && submittedPath.length() > 1) {

				redirectAttributes.addFlashAttribute("panelError", "Submitted dataset is invalid or missing" );
			}
			List<FileDTO> currentFiles = pendingPanel.getFiles();
			FileDTO fileToAdd = parser.parseFilesForPanels(panel.getPendingFile().getType(), r);
			if(panel.getPendingFile().getName() == null || panel.getPendingFile().getName().length()<1) {
				fileToAdd.setName( panel.getPendingFile().getType() + "_" + (pendingPanel.getFiles().size()+1) );
			}else {
				fileToAdd.setName(panel.getPendingFile().getName());
			}
			currentFiles.add( fileToAdd );
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

		// Iterate over all datasets
		pendingSimulation.setPanelList(pendingPanels);
		for(Panel p : pendingPanels) {
			sensorCounter = 1;
			for(FileDTO file : p.getFiles()) {
				if(file.getType().equals("Simulated sensor")) {
					sens.add(file);
					allPanelsAreStatic = false;
					// Pick only sensor files, set internal number within panel (sensor 1, sensor 2 etc.)
					file.setInternalNumber(sensorCounter);
					// Set panel number
					file.setPanelNumber(panelCounter);
					sensorCounter++;
				}

			}
			//Simulation is not a saved one
			pendingSimulation.setLoaded(false);
			panelCounter++;
		}
		// Clear database
		influx.tearDownTables();
		
		// Push dynamic data
		influx.simulateSensor(interval, sens);
		
//		for(Panel p : pendingPanels) {
//			for(FileDTO f : p.getFiles()) {
//				if(f.getType().toLowerCase().equals("sensor")) {
//					emu.startRealSensor(f.getStreamField());
//		
//				}
//			}
//		}
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
		//pendingPanel.getFiles().set(fileId, null);

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
			simRepo.setSavedSimulations(null);
			simRepo.readSavedSimulations(savingFolder, null);
			simRepo.readSavedSimulations(importZIPfolder, null);
			return "redirect:/load?id=" + id;
		}else {
			return "redirect:/reset";
		}
	}
	
	/**
	 * Push all static data to the database 
	 * @return true if there is any unpushed data left
	 */
	@GetMapping("/launchStatics")
	public String launchStatics() {
		int panelCounter = 1;
		int simCounter = 1;
		int curCounter = 1;
		List<FileDTO> sims  = new ArrayList<FileDTO>();
		List<FileDTO> curs  = new ArrayList<FileDTO>();
		long longestDuration = 0;

		if (!MainController.staticsAreLaunched) {
			// Iterates over pendingPanels and creates two arraylists for simulations and curing cycles
			for (Panel p : MainController.pendingPanels) {
				simCounter = 1;
				curCounter = 1;
				for (FileDTO file : p.getFiles()) {
					if (file.getType().equals("Simulation")) {
						sims.add(file);
						file.setInternalNumber(simCounter);
						file.setPanelNumber(panelCounter);
						simCounter++;
					} else if (file.getType().equals("Curing cycle")) {
						curs.add(file);
						file.setInternalNumber(curCounter);
						file.setPanelNumber(panelCounter);
						curCounter++;
					}
					try {
						longestDuration = Math.max(longestDuration, file.findDuration());
					} catch (Exception e) {
						// TODO Auto-generated catch block
					}

				}
				MainController.pendingSimulation.setLoaded(false);
				panelCounter++;
			}
			influx.addStaticPoints(sims, "simulation");
			influx.addStaticPoints(curs, "curing_cycle");
			MainController.staticsAreLaunched = true;
			MainController.pendingSimulation.setStaticsLoaded(true);

		}
	//	return !MainController.staticsAreLaunched;
		return "redirect:/reset";

	}

	@RequestMapping("/")
	public String redirectToHome() {
		return "redirect:/home";
	}
}



