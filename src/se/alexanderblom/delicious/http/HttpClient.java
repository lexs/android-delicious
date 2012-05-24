package se.alexanderblom.delicious.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpClient {
	public static Response get(String url) throws IOException {
		Request request = new Request(Request.Method.GET, url);
		return execute(request);
	}
	
	public static Response execute(Request request) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
		connection.setRequestMethod(request.getMethod().name());
		
		// Set headers
		for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		return new Response(connection);
	}
}