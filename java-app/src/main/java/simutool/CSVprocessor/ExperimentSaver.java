package simutool.CSVprocessor;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import simutool.DBpopulator.InfluxPopulator;

@Repository
public class ExperimentSaver {

	@Autowired
	private InfluxPopulator influx;

	
	 public void savePanel(int number) {
		 Query query = new Query("SELECT * FROM " + influx.getTablename(), influx.getTablename());
		 QueryResult q = influx.getInfluxDB().query(query);
		 System.out.print(q);
	} 
	
}
