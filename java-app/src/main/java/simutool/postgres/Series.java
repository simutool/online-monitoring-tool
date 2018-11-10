package simutool.postgres;

import java.sql.Timestamp;

/**
 * Single entry of query result on postgres
 * @deprecated
 *
 */
public class Series {
	
	private Timestamp time;
	private double temperature;
	private int datasource_id;
	
	
	
	public Series() {
		super();
	}
	public Series(Timestamp time, double temperature, int datasource_id) {
		super();
		this.time = time;
		this.temperature = temperature;
		this.datasource_id = datasource_id;
	}
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public int getDatasource_id() {
		return datasource_id;
	}
	public void setDatasource_id(int datasource_id) {
		this.datasource_id = datasource_id;
	}
	
	

}
