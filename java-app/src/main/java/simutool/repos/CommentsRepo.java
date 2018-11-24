package simutool.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import simutool.DBpopulator.InfluxPopulator;
import simutool.models.Simulation;

@Repository
public class CommentsRepo {
	
	
	@Autowired
	private InfluxPopulator influx;

	
	 public QueryResult getCommentsForSimulation() {
		 Query query = new Query("SELECT * FROM cpu", influx.getTablename());
		 QueryResult q = influx.getInfluxDB().query(query);
		 return q;
	} 
	 
	 public void saveComment() {
		 
	 }
}



