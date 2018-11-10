package simutool.DBpopulator;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.BatchPoints.Builder;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import simutool.CSVprocessor.FileDTO;
import simutool.postgres.Series;
import simutool.postgres.SeriesRepo;

/**
 * @deprecated 
 * Class for parsing xlsx datasheets and pushing data to a postgres database
 */
public class DBpopulator {

	static InfluxDB influxDB;
	static SeriesRepo seriesRepo;

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

	public ArrayList<Double> readXlsx(String path, int sheetNr, int columnNr) {

		try {
			File excelFile = new File(path);
			FileInputStream fis = new FileInputStream(excelFile);

			// we create an XSSF Workbook object for our XLSX Excel File
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			// we get first sheet
			XSSFSheet sheet = workbook.getSheetAt(sheetNr);

			// we iterate on rows
			ArrayList<Double> result = new ArrayList<Double>();

			// iterate on cells for the current row
			Iterator<Row> rowIterator = sheet.rowIterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				try {
					result.add(row.getCell(columnNr).getNumericCellValue());

				} catch (Exception ignored) {

				}

			}

			workbook.close();
			fis.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addSeries(int quantity, int millis, int datasource_id, String tableName) {
		Timer timer = new Timer();
		timer.schedule(new AddPoint(tableName, quantity, datasource_id, millis), 0, 1 * millis);
	}

	class AddPoint extends TimerTask {

		String tableName;
		int millis;
		int quantity;
		int datasource_id;
		int offset;
		long shift;

		public AddPoint(String tableName, int quantity, int datasource_id, int millis) {
			this.tableName = tableName;
			this.quantity = quantity;
			this.millis = millis;
			this.datasource_id = datasource_id;
			offset = 0;
			shift =  System.currentTimeMillis() - 
					seriesRepo.getSeries(0, 1, datasource_id).getSeries().get(0).getTime().getTime();
		}

		public void run() {
			
			ArrayList<Series> data = seriesRepo.getSeries(offset, quantity, datasource_id).getSeries();
			Builder builder = BatchPoints.database("simulation_dyn1");

			for(Series s : data) {
				Point batchPoint = Point.measurement(tableName).time(s.getTime().getTime()+shift, TimeUnit.MILLISECONDS)
					.addField("temperature", s.getTemperature()).build();
				System.out.println(new Timestamp(s.getTime().getTime()+shift));
				builder.points(batchPoint);
			}
			influxDB.write(builder.build());
		
			influxDB.close();
			offset = offset + quantity;
		}
	}

	public void startInflux(List<FileDTO> sensorData, List<FileDTO> simData) {
		createConnection("test", "http://localhost:8086", "admin", "12345");
	}
	
	public void main(String[] args) {
		
		//POSTGRES CONNECTION
	//	seriesRepo = new SeriesRepo();
	//	seriesRepo.setConnection();
	
		// INFLUX CONNECTION

		final CyclicBarrier gate = new CyclicBarrier(2);
		
		// **** SETTINGS BLOCK ****
		final int quantity = 5;
		final int frequency = 5000;
		final int datasource_id = 1;
		// ***********************

		try {
			Thread t1 = new Thread() {
				public void run() {
					try {
						gate.await();
				//		db.addSeries(quantity, frequency, datasource_id, "sensor");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						e.printStackTrace();
					}
				}
			};
			
			t1.start();
			gate.await();

			

			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}

	}

}
