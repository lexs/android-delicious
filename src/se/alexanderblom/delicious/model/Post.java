package se.alexanderblom.delicious.model;

import java.util.List;

public class Post {
	private String link;
	private String title;
	private String notes;
	private List<String> tags;
	private long time;
	
	public Post(String link, String title, String notes, List<String> tags, long time) {
		this.link = link;
		this.title = title;
		this.notes = notes;
		this.tags = tags;
		this.time = time;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public long getTime() {
		return time;
	}
}
