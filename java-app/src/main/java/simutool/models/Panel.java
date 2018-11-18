package simutool.models;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;

public class Panel {
	
	private static int nextId = 1;	
	private int id;
	private String name;
	private String sensorPath;
	private FileDTO sensorPathDTO;
	private String simulationPath;
	private FileDTO simulationPathDTO;
	private int simulationId;
	private String simulationName;
	private String curingCyclePath;
	private FileDTO curingCyclePathDTO;

	@Autowired
	Parser parser;
	

	public Panel(int id, String name, String sensorPath, String simulationPath, String simulationName,
			String curingCyclePath) {
		super();
		this.id = id;
		this.name = name;
		this.sensorPath = sensorPath;
		this.simulationPath = simulationPath;
		this.simulationName = simulationName;
		this.curingCyclePath = curingCyclePath;
	}
	public Panel() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Panel(int id, String name, String sensorPath, String simulationPath, String curingCyclePath, int simulationId) {
		super();
		this.id = id;
		this.name = name;
		this.sensorPath = sensorPath;
		this.simulationPath = simulationPath;
		this.curingCyclePath = curingCyclePath;
		this.simulationId = simulationId;
	}
	public Panel(String name, String sensorPath, String simulationPath) {
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
	public void setFinalId() {
		this.id = nextId;
		nextId++;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSensorPath() {
		return sensorPath;
	}
	public void setSensorPath(String sensorPath) {
		this.sensorPath = sensorPath;
	}
	
	public FileDTO getSensorPathDTO() {
		return sensorPathDTO;
	}
	public void setSensorPathDTO(FileDTO sensorPathDTO) {
		this.sensorPathDTO = sensorPathDTO;
	}
	
	public String getSimulationPath() {
		return simulationPath;
	}
	public void setSimulationPath(String simulationPath) {
		this.simulationPath = simulationPath;
	}
	public String getCuringCyclePath() {
		return curingCyclePath;
	}
	public void setCuringCyclePath(String curingCyclePath) {
		this.curingCyclePath = curingCyclePath;
	}
	
	public FileDTO getSimulationPathDTO() {
		return simulationPathDTO;
	}
	public void setSimulationPathDTO(FileDTO simulationPathDTO) {
		this.simulationPathDTO = simulationPathDTO;
	}
	public FileDTO getCuringCyclePathDTO() {
		return curingCyclePathDTO;
	}
	public void setCuringCyclePathDTO(FileDTO curingCyclePathDTO) {
		this.curingCyclePathDTO = curingCyclePathDTO;
	}
	public boolean filesAreCSV() {
		boolean simIsValid =  simulationPath == null || simulationPath.length() < 1 || simulationPath.substring(simulationPath.lastIndexOf('.') + 1).equals("csv");
		boolean curIsValid = curingCyclePath == null || curingCyclePath.length() < 1 || curingCyclePath.substring(curingCyclePath.lastIndexOf('.') + 1).equals("csv");
		boolean sensIsValid = sensorPath == null || sensorPath.length() < 1 || sensorPath.substring(sensorPath.lastIndexOf('.') + 1).equals("csv");

	

		return simIsValid && curIsValid && sensIsValid;
	}
	public boolean allPathsEmpty() {
		boolean simIsEmpty =  simulationPath == null || simulationPath.length() < 1;
		boolean curIsEmpty =  curingCyclePath == null || curingCyclePath.length() < 1;
		boolean sensIsEmpty =  sensorPath == null || sensorPath.length() < 1 ;
		
		
		return simIsEmpty && curIsEmpty && sensIsEmpty;
	}
	public void editPanel(Panel newPanel) {
		name = newPanel.getName();
		
		if(newPanel.getSensorPath() != null)
			sensorPath = newPanel.getSensorPath();
		if(newPanel.getSensorPathDTO() != null)
			sensorPathDTO = newPanel.getSensorPathDTO();

		if(newPanel.getSimulationPath() != null)
			simulationPath = newPanel.getSimulationPath();
		if(newPanel.getSimulationPathDTO() != null)
			simulationPathDTO = newPanel.getSimulationPathDTO();
		
		if(newPanel.getCuringCyclePath() != null)
			curingCyclePath = newPanel.getCuringCyclePath();
		if(newPanel.getCuringCyclePathDTO() != null)
			curingCyclePathDTO = newPanel.getCuringCyclePathDTO();
	}
	public static long getNextId() {
		return nextId;
	}
	
	public String getSimulationName() {
		return simulationName;
	}
	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
	}
	@Override
	public String toString() {
		return "Panel [id=" + id + ", name=" + name + ", sensorPath=" + sensorPath + ", sensorPathDTO=" + sensorPathDTO
				+ ", simulationPath=" + simulationPath + ", simulationId=" + simulationId + ", simulationName="
				+ simulationName + ", curingCyclePath=" + curingCyclePath + "]";
	}

	

}
