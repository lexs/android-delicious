package se.alexanderblom.delicious.http.resource;

import java.io.IOException;
import java.nio.charset.Charset;

import se.alexanderblom.delicious.http.Resource;
import se.alexanderblom.delicious.http.Response;

import com.google.common.io.CharStreams;

public class StringResource implements Resource<String> {
	@Override
	public String get(Response response) throws IOException {
		String charset = response.getCharset();
		
		try {
			return CharStreams.toString(CharStreams.newReaderSupplier(response, Charset.forName(charset)));
		} finally {
			response.disconnect();
		}
	}
}
