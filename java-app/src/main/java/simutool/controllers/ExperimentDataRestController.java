package simutool.controllers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import simutool.CSVprocessor.ExperimentSaver;
import simutool.CSVprocessor.FileDTO;
import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Comment;
import simutool.models.Panel;
import simutool.models.Simulation;

/**
 * Mapping for interaction between grafana frontend and Spring server
 *
 */
@RestController
public class ExperimentDataRestController {

	@Autowired
	ExperimentSaver saver;

	@Autowired
	private InfluxPopulator influx;

	/**
	 * Returns JSON with panels data in current simulation
	 * @param m empty Model (currently not used)
	 * @return Simulation - info on experiment with comments
	 */
	@RequestMapping("/getExperimentData")
	public Simulation getExperimentData(Model m) {
		
		//Queries comments and series databases
		Query commentsQuery = new Query("SELECT * FROM " + InfluxPopulator.commentsTableName, InfluxPopulator.commentsTableName);
		List<Series> seriesData = InfluxPopulator.influxDB.query(commentsQuery).getResults().get(0).getSeries();
		List<Comment> comments = new ArrayList<Comment>();
		InfluxPopulator.influxDB.setDatabase(InfluxPopulator.commentsTableName);

		if(seriesData != null) {
			List<List<Object>> vals = seriesData.get(0).getValues(); 

			for(List<Object> val : vals) {

				// Normalize time of each comment
				long time = saver.normalizeTimeStamp( val.get(0).toString() );
				System.out.println("here: " + val.get(0).toString());
				Date now = new Date();
				Date dateWithTimezone = new Date(time + (now.getTimezoneOffset() * 60000));
				String text = val.get(1).toString();
				
				// Collect a comment
				Comment c = new Comment();
				c.setCommentText(text);
				String hours = dateWithTimezone.getHours() < 10 ? "0" + dateWithTimezone.getHours() : "" + dateWithTimezone.getHours();
				String minutes = dateWithTimezone.getMinutes() < 10 ? "0" + dateWithTimezone.getMinutes() : "" + dateWithTimezone.getMinutes();
				String seconds = dateWithTimezone.getSeconds() < 10 ? "0" + dateWithTimezone.getSeconds() : "" + dateWithTimezone.getSeconds();
				c.setTimeAsString( hours + ":" + minutes + ":" + seconds );
				comments.add(c);
			}
		}
		try {
			MainController.pendingSimulation.setComments(comments);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return MainController.pendingSimulation;
	}


	/**
	 * Save comment passed from grafana frontend vie ajax 
	 * @param commentData comment recieved from request
	 */
	@PostMapping("/sendComment")
	public void sendComment(@RequestBody Comment commentData) {
		// Retrieve todays date and combine it with time of comment
		LocalDateTime today = LocalDateTime.now();
		String formattedDate = today.getYear() + "-" + today.getMonth().getValue() + "-" + today.getDayOfMonth() + "T" + 
				commentData.getTimeAsString() + ".111Z"; 
		
		System.out.println("getTimeAsString: " + commentData.getTimeAsString());
		System.out.println("formattedDate: " + formattedDate);
		
		long timeStamp = saver.normalizeTimeStamp(formattedDate);
		
		// Push point to the database
		Point point = Point.measurement(InfluxPopulator.commentsTableName).time( timeStamp, TimeUnit.MILLISECONDS)
				.addField("comment", "\"" + commentData.getCommentText() + "\"").build();
		System.out.println("point: " + point);

		InfluxPopulator.influxDB.write(InfluxPopulator.commentsTableName, "autogen", point); 
		InfluxPopulator.influxDB.close();

	}

	/**
	 * Saves changes to experiment metadata
	 * @param simData Simulation, where only one attribute/value pair is not null 
	 */
	@PostMapping("/setSimulationData")
	public void setSimulationData(@RequestBody Simulation simData) {
		if(simData.getName() != null) {
			MainController.pendingSimulation.setName(simData.getName());
		}

		if(simData.getSaved() != null) {
			MainController.pendingSimulation.setSaved(simData.getSaved());
		}
		if(simData.getDescription() != null) {
			System.out.println("new description: " + simData.getDescription());
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


	/**
	 * Push all static data to the database 
	 * @return if there is any unpushed data left
	 */
	@GetMapping("/launchStatics")
	public boolean launchStatics() {
		int panelCounter = 1;
		int simCounter = 1;
		int curCounter = 1;
		List<FileDTO> sims  = new ArrayList<FileDTO>();
		List<FileDTO> curs  = new ArrayList<FileDTO>();
		long longestDuration = 0;

		if (!MainController.staticsAreLaunched) {
			for (Panel p : MainController.pendingPanels) {
				simCounter = 1;
				curCounter = 1;
				for (FileDTO file : p.getFiles()) {
					if (file.getType().equals("Simulation")) {
						sims.add(file);
						file.setInternalNumber(simCounter);
						file.setPanelNumber(panelCounter);
						simCounter++;
					} else if (file.getType().equals("Curing cycle")) {
						curs.add(file);
						file.setInternalNumber(curCounter);
						file.setPanelNumber(panelCounter);
						curCounter++;
					}
					longestDuration = Math.max(longestDuration, file.findDuration());

				}
				MainController.pendingSimulation.setLoaded(false);
				panelCounter++;
			}
			influx.addStaticPoints(sims, "simulation");
			influx.addStaticPoints(curs, "curing_cycle");
			MainController.staticsAreLaunched = true;
			MainController.pendingSimulation.setStaticsLoaded(true);

		}
		return !MainController.staticsAreLaunched;
	}

}






