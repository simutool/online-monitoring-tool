package simutool.CSVprocessor;

import java.util.List;

/**
 * Single data file entity
 *
 */
public class FileDTO {
	private int panelNumber;
	private int internalNumber;
	private String name;
	private String type;
	private String datasource_id;
	private String streamField;
	
	// Each line is an array of values: time,value,dataset_id
	private List<String[]> rows;
	
	// Earliest and latest timestamp in a dataset - to calculate perfect scale
	long earliestTime;
	long latestTime;
	
	public long getEarliestTime() {
		return earliestTime;
	}
	public void setEarliestTime(long earliestTime) {
		this.earliestTime = earliestTime;
	}
	
	public long getLatestTime() {
		return latestTime;
	}
	public void setLatestTime(long latestTime) {
		this.latestTime = latestTime;
	}
	public int getPanelNumber() {
		return panelNumber;
	}
	public void setPanelNumber(int panelNumber) {
		this.panelNumber = panelNumber;
	}
	
	public int getInternalNumber() {
		return internalNumber;
	}
	public void setInternalNumber(int internalNumber) {
		this.internalNumber = internalNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String[]> getRows() {
		return rows;
	}
	public void setRows(List<String[]> rows) {
		this.rows = rows;
	}
	public String getDatasource_id() {
		return datasource_id;
	}
	public void setDatasource_id(String datasource_id) {
		this.datasource_id = datasource_id;
	}
	/**
	 * Calculate the duration of the dataset 
	 * @return duration of the sequence
	 */
	public long findDuration() {
		long result;
		try {
			result = (Long.parseLong( rows.get(rows.size()-1)[0] ) - Long.parseLong( rows.get(0)[0] ))/60;
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 100;
	}
	
	
	public String getStreamField() {
		return streamField;
	}
	public void setStreamField(String streamField) {
		this.streamField = streamField;
	}
	@Override
	public String toString() {
		return "FileDTO [panel number=" + panelNumber + ", name=" + name + ", type=" + type + ", datasource_id=" + datasource_id
				 + "]";
	}
	

}
