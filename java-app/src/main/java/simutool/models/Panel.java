package simutool.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import simutool.CSVprocessor.FileDTO;
import simutool.CSVprocessor.Parser;

public class Panel {
	
	private static int nextId = 1;	
	private int id;
	private int finalId;
	private String name;
	private int simulationId;
	private String simulationName;
	private List<FileDTO> files;
	private FileDTO pendingFile;

	

	@Autowired
	Parser parser;
	

	public Panel(int id, String name, String sensorPath, String simulationPath, String simulationName,
			String curingCyclePath) {
		super();
		this.id = id;
		this.name = name;
		this.finalId = 0;
		this.simulationName = simulationName;
	}
	public Panel() {
		super();
		this.files = new ArrayList<FileDTO>();
		// TODO Auto-generated constructor stub
	}
	public Panel(int id, String name, String sensorPath, String simulationPath, String curingCyclePath, int simulationId) {
		super();
		this.id = id;
		this.name = name;
		this.simulationId = simulationId;
	}
	public Panel(String name, String sensorPath, String simulationPath) {
		super();
		this.name = name;
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
	public int getFinalId() {
		return finalId;
	}
	public void setFinalId() {
		this.finalId = nextId;
		nextId++;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public boolean filesAreCSV() {
	//	boolean simIsValid =  simulationPath == null || simulationPath.length() < 1 || simulationPath.substring(simulationPath.lastIndexOf('.') + 1).equals("csv");
	//	boolean curIsValid = curingCyclePath == null || curingCyclePath.length() < 1 || curingCyclePath.substring(curingCyclePath.lastIndexOf('.') + 1).equals("csv");
	//	boolean sensIsValid = sensorPath == null || sensorPath.length() < 1 || sensorPath.substring(sensorPath.lastIndexOf('.') + 1).equals("csv");

	//	return simIsValid && curIsValid && sensIsValid;
		return true;
	}
	
	public boolean allPathsEmpty() {
	//	boolean simIsEmpty =  simulationPath == null || simulationPath.length() < 1;
	//	boolean curIsEmpty =  curingCyclePath == null || curingCyclePath.length() < 1;
	//	boolean sensIsEmpty =  sensorPath == null || sensorPath.length() < 1 ;
		
		
	//	return simIsEmpty && curIsEmpty && sensIsEmpty;
		return false;
	}
	
	public void editPanel(Panel newPanel) {
		name = newPanel.getName();
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
	
	public List<FileDTO> getFiles() {
		return files;
	}
	public void setFiles(List<FileDTO> files) {
		this.files = files;
	}
	
	public FileDTO getPendingFile() {
		return pendingFile;
	}
	public void setPendingFile(FileDTO pendingFile) {
		this.pendingFile = pendingFile;
	}
	

	@Override
	public String toString() {
		return "Panel [id=" + id + ", name=" + name + ", simulationId=" + simulationId + ", simulationName="
				+ simulationName + ", files=" + files + ", parser=" + parser + "]";
	}
	
	

}
