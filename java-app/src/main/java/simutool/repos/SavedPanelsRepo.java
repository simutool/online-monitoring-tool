package simutool.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import simutool.models.Panel;
import simutool.models.Simulation;

/**
 * Not working yet
 *
 */
@Repository
public class SavedPanelsRepo {

	@Autowired
	private JdbcTemplate template; 
	
	public List<Simulation> getPanelsForSimulation() {
		List<Simulation> simList = template.query("SELECT * FROM simulations", panelsRowMapper);
		return simList;
	} 
	
	public RowMapper<Simulation> panelsRowMapper = new RowMapper<Simulation>() {
		@Override
		public Simulation mapRow(ResultSet rs, int i) throws SQLException {
			int id = rs.getInt("id");
			String name = rs.getString("name");
			int panelNum = rs.getInt("panelsnum");
			return new Simulation(id, name, panelNum);
		}
	}; 
}