package simutool.repos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.BatchPoints.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.DBpopulator.InfluxPopulator;
import simutool.controllers.MainController;
import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class SavedSimulationsRepo {

	/* @Autowired
	private JdbcTemplate template; */
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	@Value("${metadataFolder}")
	private String metadataFolder;
	
	@Autowired
	private Parser parser;
	
	static List<Simulation> savedSimulations;
	
	public void readSavedSimulations() {
		File folder = new File(savingFolder);
		String[] directories = folder.list();

		List<Simulation> result = new ArrayList<Simulation>();
		
		//Iterate through list of experiments
		for(String path : directories) {
			long earliestTime = Long.MAX_VALUE;
			long latestTime = Long.MIN_VALUE;
			
			File exp = new File(folder + "/" + path);
			String simName = exp.getName().substring(4, exp.getName().length());
			Simulation sim = new Simulation();
			sim.setName(simName);
			System.out.println("Simulation found: " + exp.list());
			List<Panel> myPanels = new ArrayList<Panel>();
			
			//Group experiment by panel name
			List<String> panelPaths =  new LinkedList( Arrays.asList(exp.list()) );
			
			FileDTO commentsFile = new FileDTO();
			try {
				for(String filePath : panelPaths) {
					if(filePath.equals("comments.csv")) {
						commentsFile = parser.parseFilesForPanels("comments", new FileReader(folder + "/" + path + "/comments.csv"));
						sim.setCommentsFile(commentsFile);
					//	writeCommentsToDB(commentsFile);
					}
					if(filePath.equals("metadata.csv")) {
						parser.parseMetadata(sim, new FileReader(folder + "/" + path + "/metadata.csv"));

					}
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			panelPaths.removeIf(i -> i.equals("comments.csv"));
			panelPaths.removeIf(i -> i.equals("metadata.csv"));

			
			Map<String, List<String>> groupedByPanelMap = panelPaths.stream()
					.collect(Collectors.groupingBy(file -> file.substring(file.indexOf("_PANEL_")+7, file.indexOf("---")) ));
			List<List<String>> groupedByPanelList = new ArrayList<List<String>>(groupedByPanelMap.values());

			//Iterate through panels of one experiment
			for(List<String> files : groupedByPanelList) {
				String panelName = files.get(0).substring(files.get(0).indexOf("_PANEL_")+7, files.get(0).indexOf("---"));
				String panelNum = panelName.substring(0,1);

				System.out.println("it has panels: " + panelName);
				Panel p = new Panel();
				List<FileDTO> newFiles = new ArrayList<FileDTO>();
				p.setName(panelName);
				for(String file : files) {
					String dataType = file.substring(file.indexOf("---")+3, file.length()-6);
					System.out.println("datatype: " + dataType);
							try {
								FileDTO fileToAdd = parser.parseFilesForPanels(dataType.toLowerCase() , new FileReader(savingFolder +
										"/EXP_" + simName + "/" + file));
								fileToAdd.setName( file.substring(0, file.indexOf("_PANEL_")) );
								fileToAdd.setInternalNumber( Integer.parseInt( file.substring(file.length()-5, file.length()-4) ));
								fileToAdd.setPanelNumber( Integer.parseInt( panelNum ) );

								String fileStartTimeStr = fileToAdd.getRows().get(0)[0];
								String fileEndTimeStr = fileToAdd.getRows().get(fileToAdd.getRows().size()-1)[0];
   
								try {
									long fileStartTime = Long.parseLong(fileStartTimeStr);						
									long fileEndTime = Long.parseLong(fileEndTimeStr);

									if(fileStartTime < earliestTime) {
										earliestTime = fileStartTime;
									}
									if(fileEndTime > latestTime) {
										latestTime = fileEndTime;
									}

								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}finally {
									newFiles.add(fileToAdd);
								}
								 

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}	
				}
				p.setFiles(newFiles);
				myPanels.add(p);
				System.out.println(p);

			}
			sim.setPanelList(myPanels);
			sim.setEarliestTime(earliestTime);
			sim.setLatestTime(latestTime);
			result.add(sim);
		}
		
		savedSimulations = result;
	}
	
	public void writeCommentsToDB(FileDTO commentsFile) {
				
		try {
				List<String[]> rows = commentsFile.getRows();
				Builder builder = BatchPoints.database(InfluxPopulator.commentsTableName);
				long shift = System.currentTimeMillis() - Long.valueOf(rows.get(0)[0]) - 1;
				
				for (String[] data : rows) {
					long time = 0;
					System.out.println("Long.parseLong( data[0]  " + Long.parseLong( data[0]));
					System.out.println("Long.parseLong( data[1]  " + data[1]);

					if(commentsFile.getEarliestTime() != 0) {
						time = Long.valueOf(data[0]);
					}else{
						time = Long.valueOf(data[0]) * 1000 + shift;
					}
					
					Point batchPoint = Point.measurement(InfluxPopulator.commentsTableName)
							.time(Long.parseLong( data[0] ), TimeUnit.MILLISECONDS)
							.addField("comment", data[1]).build();
					builder.points(batchPoint);

				}
				InfluxPopulator.influxDB.setDatabase(InfluxPopulator.commentsTableName);
				InfluxPopulator.influxDB.write(builder.build()); 
				InfluxPopulator.influxDB.close();
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public Simulation getSimulationById(String id) {
		for(Simulation s : savedSimulations) {
			if(s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

}
