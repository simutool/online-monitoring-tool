package CSVprocessor;

import java.util.List;

/**
 * Single data file entity
 *
 */
public class FileDTO {
	private String name;
	private String type;
	private int datasource_id;
	private List<String[]> rows;
	
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
	

}
