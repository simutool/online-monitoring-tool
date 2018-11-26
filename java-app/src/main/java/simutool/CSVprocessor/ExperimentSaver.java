package simutool.CSVprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Panel;

@Repository
public class ExperimentSaver {

	@Autowired
	private InfluxPopulator influx;
	
	@Value("${saveCSVfolder}")
	private String savingFolder;
	
	public void savePanel(Panel p, int id) {
		 
		 Query query = new Query("SELECT sensor_" + id + ", simulation_" + id + ", curing_cycle_" + id + " FROM " + influx.getTablename(), influx.getTablename());
		 List<List<Object>> q = influx.getInfluxDB().query(query).getResults().get(0).getSeries().get(0).getValues();
		 
		 System.out.println(q);
		 
		 List<String[]> sensorPoints = new ArrayList<String[]>();
		 List<String[]> simulationPoints = new ArrayList<String[]>();
		 List<String[]> curingCyclePoints = new ArrayList<String[]>();

		 for(List<Object> point : q) {
			 if(point.get(1) != null) {
				 sensorPoints.add(point.toString().split(", "));
			 }else if(point.get(2) != null) {
				 simulationPoints.add(point.toString().split(", "));
			 }else if(point.get(3) != null) {
				 curingCyclePoints.add(point.toString().split(", "));
			 }
		 }

		 if(sensorPoints.size() > 0) {
			 FileDTO fileSens = new FileDTO();
			 fileSens.setRows(sensorPoints);
			 fileSens.setType("sensor");
			 fileSens.setName(p.getName());
			 writeCSV(fileSens);
		 }
		 if(simulationPoints.size() > 0) {
			 FileDTO fileSim = new FileDTO();
			 fileSim.setRows(simulationPoints);
			 fileSim.setType("simulation");
			 fileSim.setName(p.getName());
			 writeCSV(fileSim);
		 }
		 if(curingCyclePoints.size() > 0) {		 
			 FileDTO fileCur = new FileDTO();
			 fileCur.setRows(curingCyclePoints);
			 fileCur.setType("curing_cycle");
			 fileCur.setName(p.getName());
			 writeCSV(fileCur);
		 }
		 
	} 
	 
	public void writeCSV(FileDTO file) {
		String fileName = (savingFolder + "/PANEL_" + file.getName() +  "_" + new Date() + "_" + file.getType().toUpperCase() + ".csv").replaceAll(" ", "_").replace(":", ".");
	    File fileToWrite = new File(fileName);
	    int index = 1;
	    if(file.getType().equals("simulation")){
	    	index = 2;
	    }else if(file.getType().equals("curing_cycle")) {
	    	index = 3;
	    }
	    
	    try {
	    	FileWriter writer = new FileWriter(fileToWrite);
    		writer.write("time,value,datasource_id\r\n");

	    	for(int i=0; i < file.getRows().size(); i++) {
	    		String[] entry = file.getRows().get(i);
	    		writer.write(entry[0] + ',' + entry[index] + ',' + 0 + "\r\n");
	    	}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}




