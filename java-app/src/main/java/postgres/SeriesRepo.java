package postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Reposory for querying postgres database
 * @deprecated
 *
 */
public class SeriesRepo {

	Connection connection;
	
	public void setConnection() {
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres",  "12345");
			System.out.println("Java JDBC PostgreSQL Example");
			// When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within 
			// the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
			// Class.forName("org.postgresql.Driver"); 
		}catch (SQLException e) {
	        System.out.println("Connection failure.");
	        e.printStackTrace();
	    }	
	}
	
	public SeriesSet getSeries(int offset, int quantity, int data_id) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM temps " +
					" WHERE datasource_id=" + data_id + " LIMIT " + quantity +
					"OFFSET " + offset);
			ArrayList<Series> listOfSeries = new ArrayList<Series>();

			while(resultSet.next()) {
				Series s = new Series();
				s.setDatasource_id(resultSet.getInt("datasource_id"));
				s.setTemperature(resultSet.getDouble("temperature"));
				s.setTime(resultSet.getTimestamp("time"));
				listOfSeries.add(s);
			}
			return new SeriesSet(listOfSeries);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	


	
}

