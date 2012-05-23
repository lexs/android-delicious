package se.alexanderblom.delicious.model;

public class Tag {
	private String name;
	private int count;
	
	public Tag(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public int getCount() {
		return count;
	}
}
