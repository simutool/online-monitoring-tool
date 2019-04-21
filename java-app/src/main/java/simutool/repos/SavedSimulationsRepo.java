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
	
	@Value("${influx.tableName}")
	private String tableName;
	
	@Value("${influx.commentsTableName}")
	private String commentsTableName;
	
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
			List<Panel> myPanels = new ArrayList<Panel>();
			
			//Takes all file names in folder
			List<String> panelPaths =  new LinkedList( Arrays.asList(exp.list()) );
			
			FileDTO commentsFile = new FileDTO();
			try {
				for(String filePath : panelPaths) {
					if(filePath.equals("comments.csv")) {
						commentsFile = parser.parseFilesForPanels("comments", new FileReader(folder + "/" + path + "/comments.csv"));
						sim.setCommentsFile(commentsFile);
					}
					if(filePath.equals("metadata.csv")) {
						parser.parseMetadata(sim, new FileReader(folder + "/" + path + "/metadata.csv"));

					}
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Removes all files that are not datasets
			panelPaths.removeIf(i -> i.equals("comments.csv"));
			panelPaths.removeIf(i -> i.equals("metadata.csv"));
			panelPaths.removeIf(i -> i.equals("upload.json"));


			// Retrieves panel name from file name and groups data
			// panel name is everything between "PANEL_" and "---"
			Map<String, List<String>> groupedByPanelMap = panelPaths.stream()
					.collect(Collectors.groupingBy(file -> file.substring(file.indexOf("_PANEL_")+7, file.indexOf("---")) ));
			List<List<String>> groupedByPanelList = new ArrayList<List<String>>(groupedByPanelMap.values());

			//Get panel name and panel number
			for(List<String> files : groupedByPanelList) {
				String panelName = files.get(0).substring(files.get(0).indexOf("_PANEL_")+7, files.get(0).indexOf("---"));
				String panelNum = panelName.substring(0,1);
				
				// Creates Panel object for every panel
				Panel p = new Panel();
				List<FileDTO> newFiles = new ArrayList<FileDTO>();
				p.setName(panelName);
				for(String file : files) {
					
					// Retrieves file datatype
					// datatype is everything between "---" and file.length()-6
					String dataType = file.substring(file.indexOf("---")+3, file.length()-6);

							try {
								// Parse single files
								FileDTO fileToAdd = parser.parseFilesForPanels(dataType.toLowerCase() , new FileReader(savingFolder +
										"/EXP_" + simName + "/" + file));
								fileToAdd.setName( file.substring(0, file.indexOf("_PANEL_")) );
								fileToAdd.setInternalNumber( Integer.parseInt( file.substring(file.length()-5, file.length()-4) ));
								fileToAdd.setPanelNumber( Integer.parseInt( panelNum ) );

								// Find files with earliest and latest time, save them to show the right scale
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
				Builder builder = BatchPoints.database(commentsTableName);
				
				for (String[] data : rows) {
					
					Point batchPoint = Point.measurement(commentsTableName)
							.time(Long.parseLong( data[0] ), TimeUnit.MILLISECONDS)
							.addField("comment", data[1]).build();
					builder.points(batchPoint);

				}
				InfluxPopulator.influxDB.setDatabase(commentsTableName);
				InfluxPopulator.influxDB.write(builder.build()); 
				InfluxPopulator.influxDB.close();
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
