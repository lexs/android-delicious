package se.alexanderblom.delicious.http;

import java.io.IOException;

public interface Resource<T> {
	T get(Response response) throws IOException;
}
