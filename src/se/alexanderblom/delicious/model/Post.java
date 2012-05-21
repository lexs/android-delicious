package se.alexanderblom.delicious.model;

import java.util.List;

import se.alexanderblom.delicious.helpers.TagsBinder;
import android.text.Spannable;

public class Post {
	private String link;
	private String title;
	private String notes;
	private List<String> tags;
	private Spannable styledTags;
	private long time;
	
	public Post(String link, String title, String notes, List<String> tags, long time) {
		this.link = link;
		this.title = title;
		this.notes = notes;
		this.tags = tags;
		this.time = time;
		
		generateStyledTags();
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
	
	public Spannable getStyledTags() {
		return styledTags;
	}
	
	private void generateStyledTags() {
		styledTags = TagsBinder.buildTagList(tags);
	}
}
