package simutool.models;


public class Comment {
	
	private String commentText;
	private long timeStamp;
	private String timeAsString;
	
	
	
	public Comment(String commentText, String timeAsString) {
		super();
		this.commentText = commentText;
		this.timeAsString = timeAsString;
	}
	
	public Comment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getTimeAsString() {
		return timeAsString;
	}
	public void setTimeAsString(String timeAsString) {
		this.timeAsString = timeAsString;
	}

	@Override
	public String toString() {
		return "Comment [commentText=" + commentText + ", timeStamp=" + timeStamp + ", timeAsString=" + timeAsString
				+ "]";
	}
	
	
}
