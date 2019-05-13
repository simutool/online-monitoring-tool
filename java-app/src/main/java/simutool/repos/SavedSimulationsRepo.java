package simutool.repos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.BatchPoints.Builder;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class SavedSimulationsRepo {

	/* @Autowired
	private JdbcTemplate template; */

	@Value("${saveCSVfolder}")
	private String savingFolder;

	@Value("${importZIPfolder}")
	private String importZIPfolder;

	@Value("${metadataFolder}")
	private String metadataFolder;

	@Value("${influx.tableName}")
	private String tableName;

	@Value("${influx.commentsTableName}")
	private String commentsTableName;

	@Autowired
	private Parser parser;

	static List<Simulation> savedSimulations;

	public void readSavedSimulations(String folderPath, String zipPath) {

		List<String> directories = new ArrayList<String>();
		File folder = new File(folderPath);

		if(zipPath != null) {
			zipPath = unzip(zipPath);
		}

		if(zipPath == null) {
			directories = Arrays.asList( folder.list() );

		}else {
			directories.add(zipPath);
		}

		List<Simulation> result = new ArrayList<Simulation>();


		//Iterate through list of experiments
		for (String path : directories) {

			if(path.contains(".zip")) {
				String unzip;
				try {
					unzip = unzip(importZIPfolder + "/" + path);
					if(unzip == null) continue;
					path = unzip;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}

			long earliestTime = Long.MAX_VALUE;
			long latestTime = Long.MIN_VALUE;

			File exp = new File(folder + "/" + path);

			if(!exp.isDirectory() && !folderPath.equals(importZIPfolder)) continue;

			String simName = "Invalid name";
			Simulation sim = new Simulation();
			List<Panel> myPanels = new ArrayList<Panel>();

			try {
				simName = exp.getName().substring(4, exp.getName().length());
				sim.setName(simName);
			} catch (IndexOutOfBoundsException e2) {
				e2.printStackTrace();
			}
			List<String> panelPaths = new LinkedList(Arrays.asList(exp.list()));


			FileDTO commentsFile = new FileDTO();
			try {
				//Takes all file names in folder
				for (String filePath : panelPaths) {
					if (filePath.equals("comments.csv")) {
						commentsFile = parser.parseFilesForPanels("comments",
								new FileReader(exp + "/comments.csv"));
						sim.setCommentsFile(commentsFile);
					}
				}
			} catch (Exception e1) {
				sim.setErrorMessage("File comments.csv is invalid and cannot be parsed");
				e1.printStackTrace();
			}

			try {
				boolean metaFound = false;
				for (String filePath : panelPaths) {
					if (filePath.equals("metadata.csv")) {
						parser.parseMetadata(sim, new FileReader(exp + "/metadata.csv"));
						metaFound = true;
					}
				}

				if (!metaFound) {
					sim.setErrorMessage("File metadata.csv is missing");
				}


			} catch (Exception e1) {
				sim.setErrorMessage("File metadata.csv is invalid and cannot be parsed");
				e1.printStackTrace();
			}

			List<Simulation> duplicates = new ArrayList<Simulation>();
			for(Simulation s : result) {
				try {
					if(s.getId().equals( sim.getId() )) {
						duplicates.add(s);
					}
				} catch (Exception e) {
					continue;
				}
			}

			result.removeAll(duplicates);
			// Removes all files that are not datasets
			panelPaths.removeIf(i -> i.equals("comments.csv"));
			panelPaths.removeIf(i -> i.equals("metadata.csv"));
			panelPaths.removeIf(i -> i.equals("upload.json"));

			// Retrieves panel name from file name and groups data
			// panel name is everything between "PANEL_" and "---"
			Map<String, List<String>> groupedByPanelMap;
			try {
				groupedByPanelMap = panelPaths.stream().collect(Collectors
						.groupingBy(file -> file.substring(file.indexOf("_PANEL_") + 7, file.indexOf("---"))));

				List<List<String>> groupedByPanelList = new ArrayList<List<String>>(groupedByPanelMap.values());

				//Get panel name and panel number
				for (List<String> files : groupedByPanelList) {
					String panelName = files.get(0).substring(files.get(0).indexOf("_PANEL_") + 7,
							files.get(0).indexOf("---"));
					String panelNum = panelName.substring(0, 1);

					// Creates Panel object for every panel
					Panel p = new Panel();
					List<FileDTO> newFiles = new ArrayList<FileDTO>();
					p.setName(panelName);
					for (String file : files) {

						// Retrieves file datatype
						// datatype is everything between "---" and file.length()-6
						String dataType = file.substring(file.indexOf("---") + 3, file.length() - 6);

						try {
							// Parse single files
							FileDTO fileToAdd = parser.parseFilesForPanels(dataType.toLowerCase(),
									new FileReader(folder + "/EXP_" + simName + "/" + file));
							fileToAdd.setName(file.substring(0, file.indexOf("_PANEL_")));
							fileToAdd.setInternalNumber(
									Integer.parseInt(file.substring(file.length() - 5, file.length() - 4)));
							fileToAdd.setPanelNumber(Integer.parseInt(panelNum));

							// Find files with earliest and latest time, save them to show the right scale
							String fileStartTimeStr = fileToAdd.getRows().get(0)[0];
							String fileEndTimeStr = fileToAdd.getRows().get(fileToAdd.getRows().size() - 1)[0];

							try {
								long fileStartTime = Long.parseLong(fileStartTimeStr);
								long fileEndTime = Long.parseLong(fileEndTimeStr);

								if (fileStartTime < earliestTime) {
									earliestTime = fileStartTime;
								}
								if (fileEndTime > latestTime) {
									latestTime = fileEndTime;
								}

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								newFiles.add(fileToAdd);
							}

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
					p.setFiles(newFiles);
					myPanels.add(p);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				sim.setErrorMessage("One of dataset files has invalid name or format");
				e1.printStackTrace();
			}
			sim.setPanelList(myPanels);
			sim.setEarliestTime(earliestTime);
			sim.setLatestTime(latestTime);
			result.add(sim);
		} 
		if(savedSimulations == null) {
			savedSimulations = result;
		}else {
			savedSimulations.addAll(result);
		}
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

	private String unzip(String zipFilePath) {
		String extension = zipFilePath.substring(zipFilePath.lastIndexOf('.') + 1);
		String fileWithoutEx = zipFilePath.substring(zipFilePath.indexOf("EXP"), zipFilePath.lastIndexOf('.'));


		if(!extension.equals("zip")){
			return null;		
		}
		String destDir = importZIPfolder;
		File dir = new File(destDir + "/" + fileWithoutEx);
		// create output directory if it doesn't exist
		if(!dir.exists()) dir.mkdirs();
		FileInputStream fis;
		//buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while(ze != null ){
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);

				//create directories for sub directories in zip
				//  new File(newFile.getParent()).mkdir();
				if( !ze.isDirectory() ) {
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					//close this ZipEntry
					zis.closeEntry();	                	
				}

				ze = zis.getNextEntry();
			}
			//close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return fileWithoutEx;

	}



	public static List<Simulation> getSavedSimulations() {
		return savedSimulations;
	}

	public static void setSavedSimulations(List<Simulation> savedSimulations) {
		SavedSimulationsRepo.savedSimulations = savedSimulations;
	}

	public Simulation getSimulationById(String id) {
		for(Simulation s : savedSimulations) {
			try {
				if(s.getId().equals(id)) {
					return s;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public void orderSimulations(){
		Collections.sort(savedSimulations, new Comparator<Simulation>() {
			@Override
			public int compare(Simulation u1, Simulation u2) {
				if(u1.getErrorMessage() != null) {
					return 1;
				}
				if(u2.getErrorMessage() != null) {
					return -1;
				}

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
				try {
					Date d1 = df.parse(u1.getSaved()); 
					Date d2 = df.parse(u2.getSaved()); 
					return -d1.compareTo(d2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				return 0;
			}
		});

	}


}


