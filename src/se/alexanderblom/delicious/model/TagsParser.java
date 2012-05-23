package se.alexanderblom.delicious.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

import com.google.common.base.Charsets;

public class TagsParser {
	private JsonReader reader;
	
	public TagsParser(InputStream is) {
		reader = new JsonReader(new InputStreamReader(is, Charsets.UTF_8));
	}
	
	public List<Tag> getTags() throws IOException {
		try {
			ArrayList<Tag> tags = new ArrayList<Tag>();
			
			reader.beginObject();
			waitFor("tags");
			reader.beginArray();
			while (reader.hasNext()) {
				Tag tag = readTag();
				tags.add(tag);
			}
			reader.endArray();
			
			reader.endObject();
			
			return tags;
		} finally {
			reader.close();
		}
	}
	
	private Tag readTag() throws IOException {
		String tagName = null;
		int tagCount = 0;
		
		reader.beginObject();
		reader.nextName(); // "tag"

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("tag")) {
				tagName = reader.nextString();
			} else if (name.equals("count")) {
				tagCount = reader.nextInt();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		
		reader.endObject();
		
		return new Tag(tagName, tagCount);
	}
	
	private void waitFor(String name) throws IOException {
		while (reader.hasNext()) {
			if (name.equals(reader.nextName())) {
				return;
			} else {
				reader.skipValue();
			}
		}
		
		throw new IOException("Could not find '" + name + "'");
	}
}
