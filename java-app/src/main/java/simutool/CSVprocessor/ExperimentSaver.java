package simutool.CSVprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import simutool.DBpopulator.InfluxPopulator;
import simutool.controllers.MainController;
import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class ExperimentSaver {

	@Autowired
	private InfluxPopulator influx;
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	/**
	 * Writes csv files with experiment datasets and metadata
	 * @param s Current simulation
	 */
	public void savePanels(Simulation s) {
		
		 List<String> fileNames = new ArrayList<>();
		 for(Panel p : MainController.pendingSimulation.getPanelList()) {
			 for(FileDTO f : p.getFiles()) {
				 fileNames.add(f.getName());
			 }
		 }
		 // Get all data that is in influx at the moment
		 Query query = new Query("SELECT * FROM " + influx.getTablename(), influx.getTablename());
		 Query commentsQuery = new Query("SELECT * FROM " + InfluxPopulator.commentsTableName, InfluxPopulator.commentsTableName);
		 List<Series> seriesData = InfluxPopulator.influxDB.query(commentsQuery).getResults().get(0).getSeries();
		 if(seriesData != null) {
			 commentCsvWriter(seriesData.get(0).getValues());
		 }
		 TimeZone tz = TimeZone.getTimeZone("UTC");
		 DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
		 df.setTimeZone(tz);
		 String nowAsISO = df.format(new Date());
		 s.setSaved( nowAsISO);
		 Date now = new Date();
		 s.setTimeZone(now.getTimezoneOffset());
		 metadataCsvWriter();
		 jsonMetadataWriter();
		 List<String> columns = influx.getInfluxDB().query(query).getResults().get(0).getSeries().get(0).getColumns();
		 List<List<Object>> q = influx.getInfluxDB().query(query).getResults().get(0).getSeries().get(0).getValues();
		 
		 System.out.println("columns: " + columns);
		
		 
		 // Create an empty ArrayList<String[]>() for  every column
		 List<List<String[]>> collectedFiles = new ArrayList<List<String[]>>();
		 for(int i = 1; i < columns.size(); i++) {
			 collectedFiles.add( new ArrayList<String[]>() );
		 }
	
		 String simulationName = s.getName();

		 // Populate ArrayList with String Arrays [time, value, dataset_id (0 by default)]
		 for(List<Object> point : q) {
			 for(int i = 1; i < point.size(); i++) {
				 if(point.get(i) != null) {
					 collectedFiles.get(i-1).add( new String[]{point.get(0).toString(), point.get(i).toString(), "0"} );
				 }
			 }
		 }
		 
		 // Create FileDTO instances from collected data and write it to file 
		 for(int i = 0; i < collectedFiles.size(); i++) {
			 FileDTO file = new FileDTO();
			 file.setRows(collectedFiles.get(i));
			 file.setType(columns.get(i+1).substring(3, columns.get(i+1).length()-2));
			 file.setInternalNumber(Integer.parseInt(columns.get(i+1).substring(columns.get(i+1).length()-1, columns.get(i+1).length())));
			 file.setPanelNumber(Integer.parseInt(columns.get(i+1).substring(1, 2)));
			 
			 for(Panel p : MainController.pendingSimulation.getPanelList()) {
				 for(FileDTO f : p.getFiles()) {
					 if(f.getInternalNumber() == file.getInternalNumber() && 
						f.getPanelNumber() == file.getPanelNumber() && 
						f.getType().toLowerCase().replace(' ', '_').equals(file.getType().toLowerCase().replace(' ', '_'))) {
						 
						 file.setName(f.getName());

					 }
				 }
			 }
			 
			 writeCSV(file, simulationName);
		 }

		 
	} 
	 
	/**
	 * writes static datasets to a csv file
	 * 
	 * @param file File instance
	 * @param simulationName name of experiment
	 */
	public void writeCSV(FileDTO file, String simulationName) {
		
		// Remove all non-literal and non-digit characters from file name
		String fileName = ("/" + file.getName().replaceAll("[^A-Za-z0-9]+", "_") + "_PANEL_" + 
				+ file.getPanelNumber() + "-"  + MainController.pendingPanels.get(file.getPanelNumber()-1).getName() + "---" + file.getType().toUpperCase() + "-" + file.getInternalNumber() + ".csv");
		
	    File directory = new File(savingFolder + "/" + "EXP_" + simulationName);

	    // If directory with experiment name doesn't exist, create it, otherwise overwrite
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
		File fileToWrite = new File(directory + fileName);
	    int index = 1;

	    
	    try {
	    	FileWriter writer = new FileWriter(fileToWrite);
    		writer.write("time,value,datasource_id\r\n");

	    	for(int i=0; i < file.getRows().size(); i++) {
	    		String[] entry = file.getRows().get(i);
	    		String escaped = "\"" + entry[index] + "\"";

	    		writer.write( normalizeTimeStamp(entry[0]) + "," + escaped + ",0" + "\r\n");
	    	}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long normalizeTimeStamp(String inputTime) {
    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String cleanedTime = inputTime.replaceAll("\\[", "");
		
			try {
				long date;
				date = dateFormat.parse(cleanedTime).getTime();
				return date;
			} catch (ParseException e) {
				e.printStackTrace();
				
			}
	
		return 0;

		
	}
	
	public void metadataCsvWriter() {
	    File directory = new File(savingFolder + "/" + "EXP_" + MainController.pendingSimulation.getName());
	    if (! directory.exists()){
	        directory.mkdir();
	    }
		File metaFile = new File(directory + "/metadata.csv");
	    try {
	    	System.out.println("writing metadata");
	    	FileWriter writer = new FileWriter(metaFile);
    		writer.write("operators,oven,material,tool,name,start_time,end_time,timezone,description,id\r\n");
    		Simulation s = MainController.pendingSimulation;
	    	String entry = (s.getOperators() + ", " + s.getOven() + "," +
	    			s.getMaterial() + "," + s.getTool()  + "," + s.getName() + "," + s.getCreated() + "," +
	    			s.getSaved() + "," + s.getTimeZone() + "," + s.getDescription() + 
    				"," + s.getId() + "\r\n").replaceAll("null", " ");

	    	writer.write(entry);
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void jsonMetadataWriter() {
	    File directory = new File(savingFolder + "/" + "EXP_" + MainController.pendingSimulation.getName());
	    if (! directory.exists()){
	        directory.mkdir();
	    }
		File metaFile = new File(directory + "/upload.json");
	    try {
	    	FileWriter writer = new FileWriter(metaFile);
	    	
	    	Gson gson = new GsonBuilder()
	                .addSerializationExclusionStrategy(new ExclusionStrategy() {
	                    @Override
	                    public boolean shouldSkipField(FieldAttributes field) {
	                    	String f = field.getName().toLowerCase();
	                        return !Arrays.asList(Simulation.metaForUpload).contains(f);
	                    }

	                    @Override
	                    public boolean shouldSkipClass(Class<?> aClass) {
	                        return false;
	                    }
	                })
	                .create();
	    	
	    	JsonObject jj = new JsonObject();
	    		JsonArray payload = new JsonArray();
	    			JsonObject payObj = new JsonObject();
	    			payObj.addProperty("title", MainController.pendingSimulation.getName());
	    			payObj.addProperty("uploader", MainController.pendingSimulation.getOperators());

	    			JsonArray references = new JsonArray();
	    				references.add(MainController.pendingSimulation.getOven());
	    				references.add(MainController.pendingSimulation.getMaterial());
	    				references.add(MainController.pendingSimulation.getPart());
	    			payObj.add("references", references);
	    			payObj.addProperty("description", MainController.pendingSimulation.getDescription());
	    			payObj.addProperty("created", MainController.pendingSimulation.getCreated());
	    			payObj.addProperty("saved", MainController.pendingSimulation.getSaved());

	    			payload.add(payObj);
	    			jj.add("payload", payload);


	    	String j = gson.toJson(MainController.pendingSimulation);
	    	System.out.println(j);

	    	writer.write(jj.toString());
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void commentCsvWriter(List<List<Object>> comments) {
		String fileName = ("/comments.csv");
		
	    System.out.println(fileName);
	    File directory = new File(savingFolder + "/" + "EXP_" + MainController.pendingSimulation.getName());

	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
		File fileToWrite = new File(directory + fileName);
	    int index = 1;

	    
	    try {
	    	FileWriter writer = new FileWriter(fileToWrite);
    		writer.write("time,comment,dataset_id\r\n");

	    	for(int i=0; i < comments.size(); i++) {
	    		List<Object> entry = comments.get(i);
	    		writer.write( normalizeTimeStamp(entry.get(0).toString()) + "," + entry.get(1).toString() + ",0" + "\r\n");
	    	}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    


	}
	
	public static void main(String[] argv) {
	
	}
	

}




