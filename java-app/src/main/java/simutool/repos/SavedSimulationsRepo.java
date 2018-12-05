package simutool.repos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class SavedSimulationsRepo {

	/* @Autowired
	private JdbcTemplate template; */
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	@Autowired
	private Parser parser;
	
	static List<Simulation> savedSimulations;
	
	public void readSavedSimulations() {
		File folder = new File(savingFolder);
		List<File> savedFiles = Arrays.asList(folder.listFiles()); 
		Map<String, List<File>> grouped = savedFiles.stream()
				.collect(Collectors.groupingBy(file -> file.getName().substring(4, file.getName().indexOf("_PANEL_")) ));
		List<List<File>> groupedList = new ArrayList<List<File>>(grouped.values());

		List<Simulation> result = new ArrayList<Simulation>();
		
		//Iterate through list of experiments
		for(List<File> exp : groupedList) {
			String simName = exp.get(0).getName().substring(4, exp.get(0).getName().indexOf("_PANEL_"));
			Simulation sim = new Simulation();
			sim.setName(simName);
			System.out.println("Simulation found: " + simName);
			List<Panel> myPanels = new ArrayList<Panel>();
			
			//Group experiment by panel name
			Map<String, List<File>> groupedByPanelMap = exp.stream()
					.collect(Collectors.groupingBy(file -> file.getName().substring(file.getName().indexOf("_PANEL_")+7, file.getName().indexOf("---")) ));
			List<List<File>> groupedByPanelList = new ArrayList<List<File>>(groupedByPanelMap.values());

			//Iterate through panels of one experiment
			for(List<File> files : groupedByPanelList) {
				String panelName = files.get(0).getName().substring(files.get(0).getName().indexOf("_PANEL_")+7, files.get(0).getName().indexOf("---"));

				System.out.println("it has panels: " + panelName);
				Panel p = new Panel();
				List<FileDTO> newFiles = new ArrayList<FileDTO>();
				p.setName(panelName);
				for(File file : files) {
					String dataType = file.getName().substring(file.getName().indexOf("---")+3, file.getName().length()-4);
					System.out.println("it has file: " + dataType);
							try {
								newFiles.add( parser.parseFilesForPanels(dataType.toLowerCase() , new FileReader(savingFolder + "/" + file.getName())));

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}	
				}
				p.setFiles(newFiles);
				myPanels.add(p);
				System.out.println(p);

			}
			sim.setPanelList(myPanels);
			result.add(sim);
		}
		
		savedSimulations = result;
	}
	
/*	public RowMapper<Simulation> simulationsRowMapper = new RowMapper<Simulation>() {
		@Override
		public Simulation mapRow(ResultSet rs, int i) throws SQLException {
			int id = rs.getInt("id");
			String name = rs.getString("name");
			int panelNum = rs.getInt("panelsnum");
			return new Simulation(id, name, panelNum);
		}
	}; */
	
	public boolean simulationNameExists(String name) {
	//	boolean exists = template.queryForObject("SELECT COUNT(id) FROM simulations where name='" + name + "'", Integer.class) > 0;
		return false;
	}

	public static List<Simulation> getSavedSimulations() {
		return savedSimulations;
	}

	public static void setSavedSimulations(List<Simulation> savedSimulations) {
		SavedSimulationsRepo.savedSimulations = savedSimulations;
	}
	
	
	
	/* public List<Panel> getAllPanelsForSimulation(int simId) {
		List<Panel> simList = template.query("SELECT * FROM panels where simulationId=" + simId, panelsForSimulationRowMapper);
		return simList;
	}
	
	public RowMapper<Panel> panelsForSimulationRowMapper = new RowMapper<Panel>() {
		@Override
		public Panel mapRow(ResultSet rs, int i) throws SQLException {
			int id = rs.getInt("id");
			String name = rs.getString("name");
			 String sensorPath = rs.getString("sensorPath");
			 String simulationPath = rs.getString("simulationPath");
			 String curingCyclePath = rs.getString("curingCyclePath");
			 int simulationId = rs.getInt("simulationId");
			return new Panel(id, name, new File(sensorPath), new File(simulationPath), new File(curingCyclePath), simulationId);
		}
	}; */
}
