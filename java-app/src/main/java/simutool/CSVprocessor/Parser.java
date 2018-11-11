package simutool.CSVprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import simutool.models.Panel;

/**
 * This class parses .csv files with simulation and sensor data,
 * creates FileDTO Objects for every processed file and keeps lists of files in local variables.
 * @author vale
 *
 */
@Service
public class Parser {
	
	CsvParserSettings settings = new CsvParserSettings();
	CsvParser libParser;
	final String folderSim = "../CSV/";
	List<FileDTO> sensorFiles;
	List<FileDTO> simulationFiles;

	
	public Parser(){
		settings.detectFormatAutomatically();
		libParser = new CsvParser(settings);
	}
	
	/**
	 * Launches files parsing 
	 */
	public void parse(List<Panel> panels) {
//		sensorFiles = listFilesForFolder(folderSens, "sensor");
	//	simulationFiles = listFilesForFolder(folderSim, "simulation");
	}
	
	/**
	 * Reads .csv files in a specified folder, 
	 * splits files that have data with multiple datasource ids 
	 * and casts FileDTO objects
	 * @param folder folder with files
	 * @param type sensor or simulation
	 * @return List of FileDTO objects
	 */
	public List<FileDTO> parseFilesForPanels(final List<Panel> panels, String type) {
    	List<FileDTO> files = new ArrayList<FileDTO>();	 
    	int counter = 1;
    	
	    for (final Panel p : panels) {
	    	String path;
	    	if(type.equals("sensor")) {
	    		path = folderSim + p.getSensorPath().getName();
	    	}else if(type.equals("simulation")) {
	    		path = folderSim + p.getSimulationPath().getName();
	    	}else {
	    		path = folderSim + p.getCuringCyclePath().getName();
	    	}
	    	
	        	try {
					Reader r = new FileReader(path);
			    	List<String[]> rows = null;	 
					rows = libParser.parseAll(r);
					rows = rows.subList(1, rows.size())
						.stream().map(item -> Arrays.copyOfRange(item, 0, 3)).collect(Collectors.toList());
					
				//	Map<String, List<String[]>> grouped = rows.stream()
			    //            .collect(Collectors.groupingBy(list -> list[2]));
					
				//	List<List<String[]>> list = new ArrayList<List<String[]>>(grouped.values());

						FileDTO parsedFile = new FileDTO();
						parsedFile.setName(p.getName());
						parsedFile.setRows(rows);
						parsedFile.setType(type);
						parsedFile.setDatasource_id(Integer.parseInt(rows.get(0)[2]));
						parsedFile.setNumber(counter);
						files.add(parsedFile);		
						System.out.println("File added:    name: " + parsedFile.getName() +
								", datasourse: " + parsedFile.getDatasource_id() + ", rows count: " +
								parsedFile.getRows().size());
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					files.add(null);
				}	
				counter++;	
	        }
	    return files;
	}
	
	
	public List<FileDTO> getSensorFiles() {
		return sensorFiles;
	}


	public void setSensorFiles(List<FileDTO> sensorFiles) {
		this.sensorFiles = sensorFiles;
	}


	public List<FileDTO> getSimulationFiles() {
		return simulationFiles;
	}


	public void setSimulationFiles(List<FileDTO> simulationFiles) {
		this.simulationFiles = simulationFiles;
	}
	
	
	/** Returns data file with requested datasource, file name and data type.
	 * 
	 * @param type Data type: simulation, sensor.
	 * @param datasource Datasource id
	 * @param fileName name of the file
	 * @throws IllegalArgumantException If type is a not supported value 
	 * @return requested fileDTO object
	 */
	public FileDTO getFile(String type, int datasource, String fileName) {
		List<FileDTO> list;
		FileDTO file = null;
		
		if(type.equals("sensor"))
			list = sensorFiles;
		else if(type.equals("simulation"))
			list = simulationFiles;
		else
			throw new IllegalArgumentException("unsupported type");
		
		for(FileDTO entry : list) {
			if(entry.getDatasource_id() == datasource && entry.getName().equals(fileName)) {
				file = entry;
				break;			
			}

		}
		return file;
	}


	public static void main(String[] args) {
		Parser p = new Parser();
	//	p.parse();
	}
	

}
