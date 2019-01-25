package simutool.CSVprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.DBpopulator.InfluxPopulator;
import simutool.controllers.MainController;
import simutool.models.Simulation;

@Repository
public class ExperimentSaver {

	@Autowired
	private InfluxPopulator influx;
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	public void savePanels(Simulation s) {
		 
		 Query query = new Query("SELECT * FROM " + influx.getTablename(), influx.getTablename());
		 Query commentsQuery = new Query("SELECT * FROM " + InfluxPopulator.commentsTableName, InfluxPopulator.commentsTableName);
		 List<Series> seriesData = InfluxPopulator.influxDB.query(commentsQuery).getResults().get(0).getSeries();
		 if(seriesData != null) {
			 commentCsvWriter(seriesData.get(0).getValues());
		 }
		 s.setDate( Calendar.getInstance().getTime().toString() );
		 Date now = new Date();
		 s.setTimeZone(now.getTimezoneOffset());
		 metadatdaCsvWriter();
		 List<String> columns = influx.getInfluxDB().query(query).getResults().get(0).getSeries().get(0).getColumns();
		 List<List<Object>> q = influx.getInfluxDB().query(query).getResults().get(0).getSeries().get(0).getValues();
		 
		 System.out.println("columns: " + columns);
		
		 
		 List<List<String[]>> collectedFiles = new ArrayList<List<String[]>>();
		 for(int i = 1; i < columns.size(); i++) {
			 collectedFiles.add( new ArrayList<String[]>() );
		 }
	
		 String simulationName = s.getName();

		 for(List<Object> point : q) {
			 for(int i = 1; i < point.size(); i++) {
				 if(point.get(i) != null) {
					 collectedFiles.get(i-1).add( new String[]{point.get(0).toString(), point.get(i).toString(), "0"} );
				 }
			 }
		 }
		 
		 for(int i = 0; i < collectedFiles.size(); i++) {
			 FileDTO file = new FileDTO();
			 file.setRows(collectedFiles.get(i));
			 file.setType(columns.get(i+1).substring(3, columns.get(i+1).length()-2));
			 file.setInternalNumber(Integer.parseInt(columns.get(i+1).substring(columns.get(i+1).length()-1, columns.get(i+1).length())));
			 file.setPanelNumber(Integer.parseInt(columns.get(i+1).substring(1, 2)));
			 writeCSV(file, simulationName);
		 }

		 
	} 
	 
	public void writeCSV(FileDTO file, String simulationName) {
		
		String fileName = ("/EXP_" + simulationName.replaceAll("[^A-Za-z0-9]+", "_") + "_PANEL_" + 
				+ file.getPanelNumber() + "-"  + MainController.pendingPanels.get(file.getPanelNumber()-1).getName() + "---" + file.getType().toUpperCase() + "-" + file.getInternalNumber() + ".csv");
		
	    System.out.println(fileName);
	    File directory = new File(savingFolder + "/" + "EXP_" + simulationName);

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

	    		writer.write( normalizeTimeStamp(entry[0]) + "," + entry[index] + ",0" + "\r\n");
	    	}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long normalizeTimeStamp(String inputTime) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String cleanedTime = inputTime.replaceAll("\\[", "");
		long date;
		try {
			date = dateFormat.parse(cleanedTime).getTime();
			System.out.println(date);
			return date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public void metadatdaCsvWriter() {
	    File directory = new File(savingFolder + "/" + "EXP_" + MainController.pendingSimulation.getName());
	    if (! directory.exists()){
	        directory.mkdir();
	    }
		File metaFile = new File(directory + "/metadata.csv");
	    try {
	    	System.out.println("writing metadata");
	    	FileWriter writer = new FileWriter(metaFile);
    		writer.write("name,date,timezone,description,operators,oven,material,tool\r\n");
    		Simulation s = MainController.pendingSimulation;
	    	System.out.println(s);
	    	String entry = (s.getName() + "," + s.getDate() + "," + s.getTimeZone() + "," + s.getDescription() + 
    				"," + s.getOperators() + ", " + s.getOven() + "," +
	    			s.getMaterial() + "," + s.getTool()  + "\r\n").replaceAll("null", "");

	    	writer.write(entry);
			
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
	

}




