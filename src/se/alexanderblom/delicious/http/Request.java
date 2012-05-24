package se.alexanderblom.delicious.http;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

public class Request {
	public enum Method {
		GET;
	}
	
	private Method method;
	private String url;
	
	private Map<String, String> headers;
	
	public static Request get(String url) {
		return new Request(Method.GET, url);
	}
	
	public Request(Method method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public Request addHeader(String key, String value) {
		if (headers == null) {
			headers = Maps.newHashMap();
		}
		
		headers.put(key, value);
		
		return this;
	}
	
	public Request addAuth(Authentication auth) {
		if (auth != null) {
			auth.authenticate(this);
		}
		
		return this;
	}
	
	public Response execute() throws IOException {
		return HttpClient.execute(this);
	}

	Method getMethod() {
		return method;
	}

	String getUrl() {
		return url;
	}
	
	Map<String, String> getHeaders() {
		if (headers != null) {
			return headers;
		} else {
			return Collections.emptyMap();
		}
	}
}
