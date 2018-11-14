package simutool.repos;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import simutool.models.Panel;
import simutool.models.Simulation;

@Repository
public class SavedSimulationsRepo {

	 @Autowired
	private JdbcTemplate template; 
	
	public List<Simulation> getAllSavedSimulations() {
		List<Simulation> simList = template.query("SELECT * FROM simulations", simulationsRowMapper);
		return simList;
	}
	
	public RowMapper<Simulation> simulationsRowMapper = new RowMapper<Simulation>() {
		@Override
		public Simulation mapRow(ResultSet rs, int i) throws SQLException {
			int id = rs.getInt("id");
			String name = rs.getString("name");
			int panelNum = rs.getInt("panelsnum");
			return new Simulation(id, name, panelNum);
		}
	};
	
	public boolean simulationNameExists(String name) {
		boolean exists = template.queryForObject("SELECT COUNT(id) FROM simulations where name='" + name + "'", Integer.class) > 0;
		return exists;
	}
	
	public List<Panel> getAllPanelsForSimulation(int simId) {
		List<Panel> simList = template.query("SELECT * FROM panels where simulationId=" + simId, panelsForSimulationRowMapper);
		return simList;
	}
	
	public RowMapper<Panel> panelsForSimulationRowMapper = new RowMapper<Panel>() {
		@Override
		public Panel mapRow(ResultSet rs, int i) throws SQLException {
			int id = rs.getInt("id");
			String name = rs.getString("name");
			 String sensorPath = rs.getString("sensorPath");
			 String simulationPath = rs.getString("simulationPath");
			 String curingCyclePath = rs.getString("curingCyclePath");
			 int simulationId = rs.getInt("simulationId");
			return new Panel(id, name, new File(sensorPath), new File(simulationPath), new File(curingCyclePath), simulationId);
		}
	}; 
}
