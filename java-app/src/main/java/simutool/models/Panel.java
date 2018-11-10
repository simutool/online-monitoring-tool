package simutool.models;

import java.io.File;

public class Panel {
	
	private int id;
	private String name;
	private File sensorPath;
	private File simulationPath;
	private int simulationId;
	
	
	
	public Panel() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Panel(int id, String name, File sensorPath, File simulationPath, int simulationId) {
		super();
		this.id = id;
		this.name = name;
		this.sensorPath = sensorPath;
		this.simulationPath = simulationPath;
		this.simulationId = simulationId;
	}
	public Panel(String name, File sensorPath, File simulationPath) {
		super();
		this.name = name;
		this.sensorPath = sensorPath;
		this.simulationPath = simulationPath;
	}
	public int getSimulationId() {
		return simulationId;
	}
	public void setSimulationId(int simulationId) {
		this.simulationId = simulationId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getSensorPath() {
		return sensorPath;
	}
	public void setSensorPath(File sensorPath) {
		this.sensorPath = sensorPath;
	}
	public File getSimulationPath() {
		return simulationPath;
	}
	public void setSimulationPath(File simulationPath) {
		this.simulationPath = simulationPath;
	}
	
	

}
