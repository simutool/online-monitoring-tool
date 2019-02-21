package simutool.models;

import java.util.List;

import simutool.CSVprocessor.FileDTO;

public class Simulation {
	private String name;
	private String id;
	private  int panelNum;
	private  List<Panel> panelList;
	long earliestTime;
	long latestTime;
	String projectId;	
	String grafanaURL;
	List<Comment> comments;
	FileDTO commentsFile;
	long timeZone;
	boolean isLoaded;
	boolean staticsLoaded;
	public static String[] metaForUpload = new String[]{"name", "started", "ended", "description",
			"operators", "oven", "material", "tool"};
	
	String date;
	String started;
	String ended;
	String description;
	String operators;
	String oven;
	String material;
	String tool;
	String part;
	
	
	public Simulation() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Simulation(String id, String name, int panelNum) {
		super();
		this.name = name;
		this.id = id;
		this.panelNum = panelNum;
	}
	public Simulation(String name, int panelNum) {
		super();
		this.name = name;
		this.panelNum = panelNum;
	}
	public Simulation(String name) {
		super();
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	public boolean isStaticsLoaded() {
		return staticsLoaded;
	}
	public void setStaticsLoaded(boolean staticsLoaded) {
		this.staticsLoaded = staticsLoaded;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getGrafanaURL() {
		return grafanaURL;
	}
	public void setGrafanaURL(String grafanaURL) {
		this.grafanaURL = grafanaURL;
	}
	public int getPanelNum() {
		return panelNum;
	}
	public void setPanelNum(int panelNum) {
		this.panelNum = panelNum;
	}
	public List<Panel> getPanelList() {
		return panelList;
	}
	public void setPanelList(List<Panel> panelList) {
		this.panelList = panelList;
	}
	public long getEarliestTime() {
		return earliestTime;
	}
	public void setEarliestTime(long earliestTime) {
		this.earliestTime = earliestTime;
	}
	public long getLatestTime() {
		return latestTime;
	}
	public void setLatestTime(long latestTime) {
		this.latestTime = latestTime;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getStarted() {
		return started;
	}
	public void setStarted(String startTime) {
		this.started = startTime;
	}
	public String getEnded() {
		return ended;
	}
	public void setEnded(String endTime) {
		this.ended = endTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOperators() {
		return operators;
	}
	public void setOperators(String operators) {
		this.operators = operators;
	}
	public String getOven() {
		return oven;
	}
	public void setOven(String oven) {
		this.oven = oven;
	}
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public String getTool() {
		return tool;
	}
	public void setTool(String tool) {
		this.tool = tool;
	}
	
	public FileDTO getCommentsFile() {
		return commentsFile;
	}
	public void setCommentsFile(FileDTO commentsFile) {
		this.commentsFile = commentsFile;
	}
	
	
	public String getPart() {
		return part;
	}
	public void setPart(String part) {
		this.part = part;
	}
	public long getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(long timeZone) {
		this.timeZone = timeZone;
	}
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	@Override
	public String toString() {
		return "Simulation [name=" + name + ", id=" + id + ", panelNum=" + panelNum + ", panelList=" + panelList
				+ ", earliestTime=" + earliestTime + ", latestTime=" + latestTime + ", projectId=" + projectId
				+ ", grafanaURL=" + grafanaURL + ", comments=" + comments + ", commentsFile=" + commentsFile
				+ ", timeZone=" + timeZone + ", isLoaded=" + isLoaded + ", staticsLoaded=" + staticsLoaded + ", date="
				+ date + ", startTime=" + started + ", endTime=" + ended + ", description=" + description
				+ ", operators=" + operators + ", oven=" + oven + ", material=" + material + ", tool=" + tool + "]";
	}
	
	
	
}
