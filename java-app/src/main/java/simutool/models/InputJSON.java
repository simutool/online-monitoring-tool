package simutool.models;

public class InputJSON {
	
	String type;
	String title;
	String identifier;
	String description;
	
	
	
	public InputJSON() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "InputJSON [type=" + type + ", title=" + title + ", identifier=" + identifier + ", description="
				+ description + "]";
	}
	
	
	

}
