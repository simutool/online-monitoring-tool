package simutool.CSVprocessor;

import java.util.List;

/**
 * Single data file entity
 *
 */
public class FileDTO {
	private int number;
	private String name;
	private String type;
	private int datasource_id;
	private List<String[]> rows;
	
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
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
	public int getDatasource_id() {
		return datasource_id;
	}
	public void setDatasource_id(int datasource_id) {
		this.datasource_id = datasource_id;
	}
	/**
	 * Calculate the duration of the dataset 
	 * @return duration of the sequence
	 */
	public int getDuration() {
		int result = 0;
		return (Integer.parseInt( rows.get(rows.size()-1)[0] ) - Integer.parseInt( rows.get(0)[0] ))/60;
		
	}
	@Override
	public String toString() {
		return "FileDTO [number=" + number + ", name=" + name + ", type=" + type + ", datasource_id=" + datasource_id
				+ ", rows=" + rows + "]";
	}
	

}
