package simutool.DBpopulator;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.BatchPoints.Builder;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;
import simutool.controllers.MainController;

@Service
public class InfluxPopulator {
	
	public final static String tableName = "test";
	public final static String commentsTableName = "comments";

	public static InfluxDB influxDB;
	
	Parser parser;
	Timer timer;
	
	
	@Value("${influx.host}")
	private String influxHost;

	@Value("${influx.user}")
	private String influxUser;
	
	@Value("${influx.password}")
	private String influxPassword;
	
	
	public static String getTablename() {
		return tableName;
	}

	
	public static InfluxDB getInfluxDB() {
		return influxDB;
	}

	/**
	 * Sets connection with influx database using credentials from config file
	 */
	public void createConnection() {
		influxDB = InfluxDBFactory.connect(influxHost, influxUser, influxPassword);
		influxDB.setDatabase(tableName);
		// String rpName = "aRetentionPolicy";
		// influxDB.setRetentionPolicy(rpName);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
		 tearDownTables();
	}
	
	/**
	 * Clears influx tables and stops any active scheduled tasks
	 */
	public void tearDownTables() {
		Query dropQuery = new Query("DROP database " + tableName, tableName);
		Query createQuery = new Query("create database " + tableName, tableName);
		Query dropCommentsQuery = new Query("drop database " + commentsTableName, commentsTableName);
		Query createCommentsQuery = new Query("create database " + commentsTableName, commentsTableName);
		influxDB.setDatabase(tableName);
System.out.println("teared down");

		influxDB.query(dropQuery);
		influxDB.query(createQuery);
		influxDB.query(dropCommentsQuery);
		influxDB.query(createCommentsQuery);
		//tear down sensor threads
		if(timer != null ) {
			timer.cancel();
			timer.purge();
		}
	}
	
	/**
	 * Simultaneously pushes series from sensor file 
	 * @param millis Interval between series
	 * @param sensorData file to push
	 */
	public void simulateSensor(int millis, List<FileDTO> sensorData) {
		timer = new Timer();
		sensorData.removeIf(i -> i == null);
		
		final CyclicBarrier gate = new CyclicBarrier(sensorData.size()+1);

		try {
			for(FileDTO file : sensorData) {
				Thread t = new Thread() {
					public void run() {
						try {
							gate.await();
							timer.schedule(new AddPoint(file.getDatasource_id(), millis, file, timer, file.getPanelNumber(), file.getInternalNumber()), 0, 1 * millis);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (BrokenBarrierException e) {
							e.printStackTrace();
						}
					}
				};
				t.start();
			}
			gate.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	FileDTO file = parser.getFile("sensor", datasource_id, fileName);
	}
	
	/**
	 * Scheduled service to push series every n milliseconds
	 *
	 */
	class AddPoint extends TimerTask {

		Timer timer;
		int millis;
		int counter;
		int panelNum;
		List<String[]> rows;
		int datasource_id;
		long shift;
		int internalNum;

		public AddPoint(int datasource_id, int millis, FileDTO file, Timer timer, int panelNum, int internalNum) {
			
			this.timer = timer;
			this.millis = millis;
			this.datasource_id = datasource_id;
			rows = file.getRows();
			this.panelNum = panelNum;
			this.internalNum = internalNum;
			counter = 0;
			shift =  System.currentTimeMillis() - Long.valueOf( rows.get(0)[0] ) - 1;
			System.out.println("Start adding sensor");
		}

		public void run() {
			String[] data = rows.get(counter);

			Point point = Point.measurement(tableName).time( Long.parseLong(data[0])*1000 + shift, TimeUnit.MILLISECONDS)
					.addField("P" + panelNum + "_sensor_" + internalNum, Double.parseDouble((data[1]))).build();
			influxDB.write(tableName, "autogen", point);
			influxDB.close();
			counter++;
			if(counter == rows.size()) {
				timer.cancel();
				timer.purge();
			}

		}
	}
	
	/**
	 * Pushes data from simulation or curing cycle file 
	 */
	public void addStaticPoints(List<FileDTO> simData, String type) {
		try {
			simData.removeIf(i -> i == null);

			for (FileDTO file : simData) {
				List<String[]> rows = file.getRows();
				Builder builder = BatchPoints.database(tableName);
				long shift = System.currentTimeMillis() - Long.valueOf(rows.get(0)[0]) - 1;
				
				for (String[] data : rows) {
					long time = 0;
					System.out.println("file.getEarliestTime()" + file.getEarliestTime());
					if(file.getEarliestTime() != 0) {
						time = Long.valueOf(data[0]);
					}else{
						time = Long.valueOf(data[0]) * 1000 + shift;
					}
					
					Point batchPoint = Point.measurement(tableName)
							.time(time, TimeUnit.MILLISECONDS)
							.addField("P" + file.getPanelNumber() + "_" + type + "_" + file.getInternalNumber(), Double.parseDouble((data[1]))).build();
					builder.points(batchPoint);

				}
				influxDB.setDatabase(tableName);
				influxDB.write(builder.build()); 
				influxDB.close();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	public void startInflux() {
		createConnection();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
