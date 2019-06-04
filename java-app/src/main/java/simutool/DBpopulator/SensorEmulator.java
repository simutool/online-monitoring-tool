package simutool.DBpopulator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;

@Service
public class SensorEmulator {
	
	@Autowired
	private Parser parser;
	
	static Timer tmr;
	public void startRealSensor(String streamFieldName) {
		
		String myString = "C:/dev/online-monitoring-client/sample input/kit-trials-extracted/kit_trials_exp_1_05.11.2018_fo6.csv";
		FileReader is = null;
		
		try {
			is = new FileReader(myString);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			System.out.println("file is broken");
		}

		
		FileDTO file = parser.parseFilesForPanels("sensor", is);
		
		tmr= new Timer();
		

	
				// Start a thread for every sensor dataset
				Thread t = new Thread() {
					public void run() {
						System.out.println("streamFieldName: "+streamFieldName);

							tmr.schedule(new AddAnotherPoint(file.getDatasource_id(), 1000, file, tmr, streamFieldName), 0, 1000);
						
					}
				};
			t.start();
		
	}
	
	/**
	 * Scheduled service to push series every n milliseconds
	 *
	 */
	class AddAnotherPoint extends TimerTask {

		Timer timer;
		int millis;
		int counter;
		int panelNum;
		List<String[]> rows;
		String datasource_id;
		long shift;
		int internalNum;
		String streamFieldName;

		public AddAnotherPoint(String datasource_id, int millis, FileDTO file, Timer timer, String streamFieldName) {
			
			this.timer = timer;
			this.millis = millis;
			this.datasource_id = datasource_id;
			rows = file.getRows();
			this.panelNum = panelNum;
			this.internalNum = internalNum;
			this.streamFieldName = streamFieldName;
			counter = 0;
			shift =  System.currentTimeMillis() - Long.valueOf( rows.get(0)[0] ) - 1;
			System.out.println("streamFieldName2: "+streamFieldName);

		}

		public void run() {
			String[] data = rows.get(counter);
			System.out.println("rows: "+rows.size());

			 TimeZone tz = TimeZone.getTimeZone("UTC");
			 DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
			 df.setTimeZone(tz);
			 String nowAsISO = df.format(Long.parseLong(data[0])*1000 + shift);
				System.out.println("nowAsISO: "+ nowAsISO);

			Point point = Point.measurement("db").time( Long.parseLong(data[0])*1000 + shift, TimeUnit.MILLISECONDS)
					.addField(streamFieldName, Double.parseDouble((data[1]))).build();
			InfluxPopulator.influxDB.write("db", "autogen", point);
			InfluxPopulator.influxDB.close();
			counter++;
			if(counter == rows.size()) {
				timer.cancel();
				timer.purge();
			}

		}
	}
}
