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
				// PArse file with univocity parser
				rows = libParser.parseAll(r);

				// Take out first line that stands for column names
				// Take out everything from index 3 and further
				rows = rows.subList(1, rows.size())
						.stream().map(item -> Arrays.copyOfRange(item, 0, 3)).collect(Collectors.toList());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	

		file.setRows(rows);
		file.setType(type);
		file.setDatasource_id( rows.get(0)[2] );

		return file;
	}
	
	/** Parses csv metadata for internal use
	 * 
	 * @param s
	 * 			current Simulation
	 * @param path
	 * 		path to metadata file, if null set to default.csv
	 */
	public void parseMetadata(Simulation s, FileReader path) {
		try {
			if(path == null) {
				path = new FileReader(metadataFolder + "/default.csv");
			}
			List<String[]>rows = libParser.parseAll(path);

			s.setOperators(rows.get(1)[0]);
			s.setOven(rows.get(1)[1]);
			s.setMaterial(rows.get(1)[2]);
			s.setTool(rows.get(1)[3]);
			s.setCreated(rows.get(1)[5]);
			s.setSaved(rows.get(1)[6]);
			s.setId(rows.get(1)[9]);
			

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Parses metadata from a json file
	 * @param path
	 * 			path to metadata file - if null, {configuration folder}/default.json is used
	 * @return a list of metadata objects, like operator, oven etc. (see InputJSON.java)
	 */
	public List<InputJSON> parseJsonMetadata(FileReader pathMeta) {
		try {
			if(pathMeta == null) {
				pathMeta = new FileReader(metadataFolder + "/default.json");
			}
			ArrayList<InputJSON> list = new ArrayList<InputJSON>();
			
	    	Gson gsonMeta = new Gson();
	    	JsonReader readerMeta = new JsonReader(pathMeta);
	    	JsonObject dataMeta = gsonMeta.fromJson(readerMeta, JsonObject.class);
	    	JsonArray arr = dataMeta.get("payload").getAsJsonArray();
	    	
	    	Gson gsonUris = new Gson();
	    	JsonReader readerUris = new JsonReader( new FileReader(metadataFolder + "/uris.json") );
	    	JsonObject dataUris = gsonMeta.fromJson(readerUris, JsonObject.class);
	    	
	    	for(JsonElement a : arr) {
	    		InputJSON j = gsonMeta.fromJson(a, InputJSON.class);
	    		list.add(j);
	    	}
	    	
	    	for(InputJSON j : list) {
	    		if(j.getType().equals( dataUris.get("user_uri").toString().replaceAll("\"", "") )) {
	    			MainController.pendingSimulation.setOperators(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals( dataUris.get("oven_uri").toString().replaceAll("\"", "") )) {
	    			MainController.pendingSimulation.setOven(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals( dataUris.get("part_uri").toString().replaceAll("\"", "") )) {
	    			MainController.pendingSimulation.setPart(j.getIdentifier());
	    			break;
	    		}
	    	}
	    	for(InputJSON j : list) {
	    		if(j.getType().equals( dataUris.get("material_uri").toString().replaceAll("\"", "") )) {
	    			MainController.pendingSimulation.setMaterial(j.getIdentifier());
	    			break;
	    		}
	    	}
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




}
