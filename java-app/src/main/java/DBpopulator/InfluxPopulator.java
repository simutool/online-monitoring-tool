package DBpopulator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.BatchPoints.Builder;

import CSVprocessor.FileDTO;
import CSVprocessor.Parser;
import postgres.Series;

public class InfluxPopulator {
	
	final String tableName = "test";
	static InfluxDB influxDB;
	List<FileDTO> sensorData;
	List<FileDTO> simData;
	Parser parser;

	public void createConnection(String dbName, String url, String username, String password) {
		influxDB = InfluxDBFactory.connect(url, username, password);
		Query dropQuery = new Query("DROP database " + dbName, dbName);
		Query createQuery = new Query("create database " + dbName, dbName);

		influxDB.query(dropQuery);
		influxDB.query(createQuery);
		influxDB.setDatabase(dbName);
		// String rpName = "aRetentionPolicy";
		// influxDB.setRetentionPolicy(rpName);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
	}
	
	public void simulateSensor(int millis, int datasource_id, String fileName) {
		Timer timer = new Timer();
		FileDTO file = parser.getFile("sensor", datasource_id, fileName);
		timer.schedule(new AddPoint(datasource_id, millis, file, timer), 0, 1 * millis);
	}
	
	class AddPoint extends TimerTask {

		Timer timer;
		int millis;
		int counter;
		List<String[]> rows;
		int datasource_id;
		long shift;

		public AddPoint(int datasource_id, int millis, FileDTO file, Timer timer) {
			this.timer = timer;
			this.millis = millis;
			this.datasource_id = datasource_id;
			rows = file.getRows();
			counter = 0;
			shift =  System.currentTimeMillis() - Long.valueOf( rows.get(0)[0] ) - 1;
			System.out.println("sensor: ");



		}

		public void run() {
			String[] data = rows.get(counter);

			if(counter < 5) {
				System.out.println(Long.parseLong(data[0]) + shift);
			}
			Point point = Point.measurement(tableName).time( Long.parseLong(data[0])*1000 + shift, TimeUnit.MILLISECONDS)
					.addField("sensor_" + datasource_id, Double.parseDouble((data[1]))).build();
			influxDB.write(point);
			System.out.println(point.lineProtocol());

			influxDB.close();
			counter++;
			if(counter == rows.size()) {
				timer.cancel();
				timer.purge();
			}

		}
	}
	
	public void addSimulationPoints(int datasource_id, String fileName) {
		try {
			List<String[]> rows = parser.getFile("simulation", datasource_id, fileName).getRows();
			Builder builder = BatchPoints.database(tableName);
			long shift =  System.currentTimeMillis() - Long.valueOf( rows.get(0)[0] ) - 1;
			System.out.println("simulation: ");

			System.out.println(Long.parseLong(rows.get(0)[0]) + shift);
			System.out.println(Long.parseLong(rows.get(1)[0]) + shift);
			System.out.println(Long.parseLong(rows.get(2)[0]) + shift);
			System.out.println(Long.parseLong(rows.get(3)[0]) + shift);

			for(String[] data : rows) {
				Point batchPoint = Point.measurement(tableName).time( Long.valueOf(data[0])*1000 + shift, TimeUnit.MILLISECONDS)
						.addField("simulation_" + datasource_id, Double.parseDouble((data[1]))).build();
				builder.points(batchPoint);

			}
			influxDB.write(builder. build());			
			influxDB.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startInflux(Parser parser) {
		createConnection(tableName, "http://localhost:8086", "admin", "12345");
		this.sensorData = parser.getSensorFiles();
		this.simData = parser.getSimulationFiles();
		this.parser = parser;
		System.out.println("InfluxDB server started.");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
