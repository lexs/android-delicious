package se.alexanderblom.delicious.model;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.JsonReader;

public class TagsParser {
	private JsonReader reader;
	
	public TagsParser(Reader in) {
		reader = new JsonReader(in);
	}
	
	public List<Tag> getTags() throws IOException {
		try {
			ArrayList<Tag> tags = new ArrayList<Tag>();
			
			reader.beginObject();
			waitFor("tags");
			reader.beginArray();
			while (reader.hasNext()) {
				readTag(tags);
			}
			reader.endArray();
			
			reader.endObject();
			
			return tags;
		} finally {
			reader.close();
		}
	}
	
	private void readTag(List<Tag> tags) throws IOException {
		String tagName = null;
		int tagCount = 0;
		
		reader.beginObject();
		waitFor("tag");

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
		
		// Delicious passes empty tags for some reason, ignore those
		if (!TextUtils.isEmpty(tagName)) {
			tags.add(new  Tag(tagName, tagCount));
		}
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
