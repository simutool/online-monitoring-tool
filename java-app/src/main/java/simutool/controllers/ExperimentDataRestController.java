package simutool.controllers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Series;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import simutool.CSVprocessor.ExperimentSaver;
import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Comment;
import simutool.models.Panel;
import simutool.models.Simulation;

@RestController
public class ExperimentDataRestController {

	@Autowired
	ExperimentSaver saver;
	
	
	/**
	 * Returns JSON with panels data in current simulation
	 * @return info on selected panels
	 */
	@RequestMapping("/getExperimentData")
	public List<Panel> getExperimentData(Model m) {
		 Query commentsQuery = new Query("SELECT * FROM " + InfluxPopulator.commentsTableName, InfluxPopulator.commentsTableName);
		 List<Series> seriesData = InfluxPopulator.influxDB.query(commentsQuery).getResults().get(0).getSeries();
		 List<Comment> comments = new ArrayList<Comment>();
		 InfluxPopulator.influxDB.setDatabase(InfluxPopulator.commentsTableName);

System.out.println(InfluxPopulator.influxDB.query(commentsQuery).getResults());

		 System.out.println("seriesData: " + seriesData);
		 if(seriesData != null) {
			 List<List<Object>> vals = seriesData.get(0).getValues(); 
		
			 for(List<Object> val : vals) {

				 PrettyTime pretty = new PrettyTime();
				 pretty.setLocale(Locale.ENGLISH);
				 
				 long time = saver.normalizeTimeStamp( val.get(0).toString() );
				LocalDateTime asDate = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);

				 String text = val.get(1).toString();
				 Comment c = new Comment();
				 System.out.println("time: " + asDate);
				 System.out.println("text: " + text);
				 System.out.println("pretty: " + pretty.format( new Date(asDate.toEpochSecond(ZoneOffset.UTC)) ));

				 c.setCommentText(text);
				 c.setTimeAsString( pretty.format( new Date(asDate.toEpochSecond(ZoneOffset.UTC)) ));
				 comments.add(c);
			 }
		 }
		 MainController.pendingPanels.get(0).setComments(comments);

		return MainController.pendingPanels;
	}
	

	@PostMapping("/sendComment")
	public void sendComment(@RequestBody Comment commentData) {
		LocalDateTime today = LocalDateTime.now();
		System.out.println(commentData);
		String formattedDate = today.getYear() + "-" + today.getMonth().getValue() + "-" + today.getDayOfMonth() + "T" + 
				commentData.getTimeAsString() + ".111Z"; 
		
		long timeStamp = saver.normalizeTimeStamp(formattedDate);
		System.out.println(timeStamp);

		Point point = Point.measurement(InfluxPopulator.commentsTableName).time( timeStamp, TimeUnit.MILLISECONDS)
				.addField("comment", commentData.getCommentText()).build();
		System.out.println("point: " + point);

		InfluxPopulator.influxDB.write(InfluxPopulator.commentsTableName, "autogen", point); 
		InfluxPopulator.influxDB.close();

	}
	

	@PostMapping("/setSimulationData")
	public void sendComment(@RequestBody Simulation simData) {
		if(simData.getName() != null) {
			MainController.pendingSimulation.setName(simData.getName());
		}
		if(simData.getDate() != null) {
			MainController.pendingSimulation.setDate(simData.getDate());
		}
		if(simData.getTime() != null) {
			MainController.pendingSimulation.setTime(simData.getTime());
		}
		if(simData.getDescription() != null) {
			MainController.pendingSimulation.setDescription(simData.getDescription());
		}
		if(simData.getOperators() != null) {
			MainController.pendingSimulation.setOperators(simData.getOperators());
		}
		if(simData.getOven() != null) {
			MainController.pendingSimulation.setOven(simData.getOven());
		}
		if(simData.getMaterial() != null) {
			MainController.pendingSimulation.setMaterial(simData.getMaterial());
		}
		if(simData.getTool() != null) {
			MainController.pendingSimulation.setTool(simData.getTool());
		}
		System.out.println(MainController.pendingSimulation);
	}
	

}






