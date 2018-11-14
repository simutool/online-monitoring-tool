package simutool.models;

import java.io.File;

public class Panel {
	
	private static int nextId = 0;	
	private int id;
	private String name;
	private File sensorPath;
	private File simulationPath;
	private int simulationId;
	private File curingCyclePath;
	
	
	public Panel() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Panel(int id, String name, File sensorPath, File simulationPath, File curingCyclePath, int simulationId) {
		super();
		this.id = id;
		this.name = name;
		this.sensorPath = sensorPath;
		this.simulationPath = simulationPath;
		this.curingCyclePath = curingCyclePath;
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
	public void setId() {
		this.id = nextId;
		nextId++;
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
	public File getCuringCyclePath() {
		return curingCyclePath;
	}
	public void setCuringCyclePath(File curingCyclePath) {
		this.curingCyclePath = curingCyclePath;
	}
	public boolean filesAreCSV() {
		boolean simIsValid =  simulationPath == null || simulationPath.getName().length() < 1 || simulationPath.getName().substring(simulationPath.getName().lastIndexOf('.') + 1).equals("csv");
		boolean curIsValid = curingCyclePath == null || curingCyclePath.getName().length() < 1 || curingCyclePath.getName().substring(curingCyclePath.getName().lastIndexOf('.') + 1).equals("csv");
		boolean sensIsValid = sensorPath == null || sensorPath.getName().length() < 1 || sensorPath.getName().substring(sensorPath.getName().lastIndexOf('.') + 1).equals("csv");

		System.out.println(simulationPath + " : " + simIsValid);
		System.out.println(curingCyclePath + " : " + curIsValid);
		System.out.println(sensorPath + " : " + sensIsValid);
		System.out.println(simIsValid && curIsValid && sensIsValid);

		return simIsValid && curIsValid && sensIsValid;
	}
	public boolean allPathsEmpty() {
		boolean simIsEmpty =  simulationPath == null || simulationPath.getName().length() < 1;
		boolean curIsEmpty =  curingCyclePath == null || curingCyclePath.getName().length() < 1;
		boolean sensIsEmpty =  sensorPath == null || sensorPath.getName().length() < 1 ;
		
		System.out.println(simulationPath + " : " + simIsEmpty);
		System.out.println(curingCyclePath + " : " + curIsEmpty);
		System.out.println(sensorPath + " : " + sensIsEmpty);
		System.out.println(simIsEmpty && curIsEmpty && sensIsEmpty);
		
		return simIsEmpty && curIsEmpty && sensIsEmpty;
	}
	public static long getNextId() {
		return nextId;
	}
	@Override
	public String toString() {
		return "Panel [id=" + id + ", name=" + name + ", sensorPath=" + sensorPath + ", simulationPath="
				+ simulationPath + ", simulationId=" + simulationId + ", curingCyclePath=" + curingCyclePath + "]";
	}
	

}
