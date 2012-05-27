package se.alexanderblom.delicious.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.JsonReader;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PostsParser {
	private static final String TAG = "PostsParser";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	private JsonReader reader;
	private SimpleDateFormat dateFormat;

	public PostsParser(InputStream is) {
		reader = new JsonReader(new InputStreamReader(is, Charsets.UTF_8));
		
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}
	
	public PostsParser(Reader in) {
		reader = new JsonReader(in);
		
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}

	public List<Post> getPosts() throws IOException {
		try {
			ArrayList<Post> posts = new ArrayList<Post>();
			
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("posts")) {
					reader.beginArray();
					
					while (reader.hasNext()) {
						Post post = readPost();
						posts.add(post);
					}
					
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();

			return posts;
		} finally {
			reader.close();
		}
	}

	private Post readPost() throws IOException {
		String link = null;
		String title = null;
		String notes = null;
		List<String> tags = Collections.emptyList();
		long timeMillis = 0;
		
		reader.beginObject();
		waitFor("post");

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("href")) {
				link = reader.nextString();
			} else if (name.equals("description")) {
				title = reader.nextString();
			} else if (name.equals("extended")) {
				notes = reader.nextString();
			} else if (name.equals("tag")) {
				Splitter splitter = Splitter.on(' ')
					.trimResults()
					.omitEmptyStrings();
				
				tags = Lists.newArrayList(splitter.split(reader.nextString()));
			} else if (name.equals("time")) {
				String time = reader.nextString();
				
				try {
					timeMillis = dateFormat.parse(time).getTime();
				} catch (ParseException e) {
					Log.e(TAG, "Failed to parse date", e);
				}
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		
		reader.endObject();
		
		return new Post(link, title, notes, tags, timeMillis);
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
