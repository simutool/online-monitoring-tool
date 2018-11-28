package simutool.repos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class SavedSimulationsRepo {

	/* @Autowired
	private JdbcTemplate template; */
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	public List<Simulation> getAllSavedSimulations() {
		File folder = new File(savingFolder);
		List<File> savedFiles = Arrays.asList(folder.listFiles()); 
		Map<String, List<File>> grouped = savedFiles.stream()
				.collect(Collectors.groupingBy(file -> file.getName().substring(4, file.getName().indexOf("_PANEL_")) ));
		List<List<File>> groupedList = new ArrayList<List<File>>(grouped.values());
		
		System.out.println("from first list" + groupedList.get(0).get(0).getName());
		System.out.println("from second list" + groupedList.get(1).get(0).getName());

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
				p.setName(panelName);
				for(File file : files) {
					String dataType = file.getName().substring(file.getName().indexOf("---")+3, file.getName().length()-4);
					System.out.println("it has file: " + dataType);

					switch(dataType) {
						case "SENSOR":{
							p.setSensorPath(savingFolder + "/" + file.getName());
							break;
						}
						case "SIMULATION":{
							p.setSimulationPath(savingFolder + "/" + file.getName());
							break;
						}
						case "CURING_CYCLE":{
							p.setCuringCyclePath(savingFolder + "/" + file.getName());
							break;
						}
						default:{
							break;
						}
					}		
				}

				myPanels.add(p);
				System.out.println(p);

			}
			sim.setPanelList(myPanels);
		}
		
		return result;
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
