package simutool.CSVprocessor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import simutool.controllers.MainController;
import simutool.models.InputJSON;
import simutool.models.Simulation;

/**
 * This class parses .csv files and creates FileDTO Objects.
 *
 */
@Service
public class Parser {

	CsvParserSettings settings = new CsvParserSettings();
	CsvParser libParser;
	List<FileDTO> sensorFiles;
	List<FileDTO> simulationFiles;
	
	@Value("${metadataFolder}")
	private String metadataFolder;

	public Parser(){
		settings.detectFormatAutomatically();
		libParser = new CsvParser(settings);
	}



	/**
	 * Reads .csv file in the passed FileReader and casts FileDTO object.
	 * @param type sensor or simulation
	 * @return FileDTO object
	 */
	public FileDTO parseFilesForPanels(String type, Reader r) {
		FileDTO file = new FileDTO();	 

		List<String[]> rows = null;	 
		
			try {
				rows = libParser.parseAll(r);

				rows = rows.subList(1, rows.size())
						.stream().map(item -> Arrays.copyOfRange(item, 0, 3)).collect(Collectors.toList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	

		//	Map<String, List<String[]>> grouped = rows.stream()
		//            .collect(Collectors.groupingBy(list -> list[2]));

		//	List<List<String[]>> list = new ArrayList<List<String[]>>(grouped.values());

		file.setRows(rows);
		file.setType(type);
		file.setDatasource_id(Integer.parseInt(rows.get(0)[2]));

		return file;
	}
	
	public void parseMetadata(Simulation s, FileReader path) {
		try {
			if(path == null) {
				path = new FileReader(metadataFolder + "/default.csv");
			}
			List<String[]>rows = libParser.parseAll(path);
			System.out.println("metadata rows: " + rows.get(1).length);
			s.setOperators(rows.get(1)[0]);
			s.setOven(rows.get(1)[1]);
			s.setMaterial(rows.get(1)[2]);
			s.setTool(rows.get(1)[3]);
			s.setId(rows.get(1)[9]);
			

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public List<InputJSON> parseJsonMetadata(Simulation s, FileReader path) {
		try {
			if(path == null) {
				path = new FileReader(metadataFolder + "/default.json");
			}
			ArrayList<InputJSON> list = new ArrayList<InputJSON>();
			
	    	Gson gson = new Gson();
	    	JsonReader reader = new JsonReader(path);
	    	JsonObject data = gson.fromJson(reader, JsonObject.class);
	    	JsonArray arr = data.get("payload").getAsJsonArray();
	    	for(JsonElement a : arr) {
	    		InputJSON j = gson.fromJson(a, InputJSON.class);
	    		list.add(j);
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals("http://www.simutool.com/User")) {
	    			MainController.pendingSimulation.setOperators(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals("http://www.simutool.com/Oven")) {
	    			MainController.pendingSimulation.setOven(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals("http://www.simutool.com/Part")) {
	    			MainController.pendingSimulation.setPart(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals("http://www.simutool.com/Material")) {
	    			MainController.pendingSimulation.setMaterial(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	System.out.println("meta constructed: " + MainController.pendingSimulation);
	    	return list;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

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
