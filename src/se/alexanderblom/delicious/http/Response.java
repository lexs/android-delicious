package se.alexanderblom.delicious.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.protocol.HTTP;

import com.google.common.io.InputSupplier;

public class Response implements InputSupplier<InputStream> {
	private HttpURLConnection connection;
	
	Response(HttpURLConnection connection) {
		this.connection = connection;
	}
	
	public String getHeader(String key) {
		return connection.getHeaderField(key);
	}
	
	public int getStatusCode() throws IOException {
		return connection.getResponseCode();
	}
	
	@Override
	public InputStream getInput() throws IOException {
		return connection.getInputStream();
	}
	
	public Reader getReader() throws IOException {
		InputStream is = new BufferedInputStream(connection.getInputStream());
		return new InputStreamReader(is, getCharset());
	}
	
	public String getContentEncoding() {
		return connection.getContentEncoding();
	}
	
	public String getContentType() {
		return connection.getContentType();
	}
	
	public int getContentLength() {
		return connection.getContentLength();
	}
	
	public String getCharset() {
		String contentType = connection.getContentType();

		if (contentType != null) {
			HeaderValueParser parser = new BasicHeaderValueParser();
			HeaderElement[] values = BasicHeaderValueParser.parseElements(contentType, parser);
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					return param.getValue();
				}
			}
		}

		// No encoding specified
		return HTTP.DEFAULT_CONTENT_CHARSET;
	}
	
	public <T> T as(Class<? extends Resource<T>> cl) throws IOException {
		
		try {
			Resource<T> handler = cl.newInstance();
			return handler.get(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	public void disconnect() {
		connection.disconnect();
	}
}
