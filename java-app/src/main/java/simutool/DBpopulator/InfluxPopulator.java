package simutool.DBpopulator;

import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;

@Service
public class InfluxPopulator {
	
	final String tableName = "test";
	static InfluxDB influxDB;
	Parser parser;
	Timer timer;
	
	@Value("${influx.host}")
	private String influxHost;

	@Value("${influx.user}")
	private String influxUser;
	
	@Value("${influx.password}")
	private String influxPassword;
	
	public void createConnection() {
		influxDB = InfluxDBFactory.connect(influxHost, influxUser, influxPassword);
		influxDB.setDatabase(tableName);
		// String rpName = "aRetentionPolicy";
		// influxDB.setRetentionPolicy(rpName);
		influxDB.enableBatch(BatchOptions.DEFAULTS);
	}
	
	public void tearDownTables() {
		Query dropQuery = new Query("DROP database " + tableName, tableName);
		Query createQuery = new Query("create database " + tableName, tableName);
		influxDB.query(dropQuery);
		influxDB.query(createQuery);
		//tear down sensor threads
		if(timer != null ) {
			timer.cancel();
			timer.purge();
		}
		

	}
	
	/**
	 * Simultaneously pushes series from all parsed sensor files as corresponding measurements
	 * @param millis Interval between series
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
							timer.schedule(new AddPoint(file.getDatasource_id(), millis, file, timer, file.getNumber()), 0, 1 * millis);
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
		int threadNum;
		List<String[]> rows;
		int datasource_id;
		long shift;

		public AddPoint(int datasource_id, int millis, FileDTO file, Timer timer, int threadNum) {
			
			this.timer = timer;
			this.millis = millis;
			this.datasource_id = datasource_id;
			rows = file.getRows();
			this.threadNum = threadNum;
			counter = 0;
			shift =  System.currentTimeMillis() - Long.valueOf( rows.get(0)[0] ) - 1;
			System.out.println("Start adding sensor");
		}

		public void run() {
			String[] data = rows.get(counter);

			Point point = Point.measurement(tableName).time( Long.parseLong(data[0])*1000 + shift, TimeUnit.MILLISECONDS)
					.addField("sensor_" + threadNum, Double.parseDouble((data[1]))).build();
			influxDB.write(point);

			influxDB.close();
			counter++;
			if(counter == rows.size()) {
				timer.cancel();
				timer.purge();
			}

		}
	}
	
	/**
	 * Pushes data from all simulation files as corresponding measurements
	 */
	public void addSimulationPoints(List<FileDTO> simData, String type) {
		try {
			simData.removeIf(i -> i == null);

			for (FileDTO file : simData) {
				List<String[]> rows = file.getRows();
				Builder builder = BatchPoints.database(tableName);
				long shift = System.currentTimeMillis() - Long.valueOf(rows.get(0)[0]) - 1;
				
				for (String[] data : rows) {
					Point batchPoint = Point.measurement(tableName)
							.time(Long.valueOf(data[0]) * 1000 + shift, TimeUnit.MILLISECONDS)
							.addField(type + "_" + file.getNumber(), Double.parseDouble((data[1]))).build();
					builder.points(batchPoint);

				}
				System.out.println(" adding " + type);

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
