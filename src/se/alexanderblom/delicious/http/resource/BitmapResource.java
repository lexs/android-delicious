package se.alexanderblom.delicious.http.resource;

import java.io.IOException;
import java.io.InputStream;

import se.alexanderblom.delicious.http.Resource;
import se.alexanderblom.delicious.http.Response;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapResource implements Resource<Bitmap> {

	@Override
	public Bitmap get(Response response) throws IOException {
		InputStream is = response.getInput();
		
		try {
			return BitmapFactory.decodeStream(is);
		} finally {
			is.close();
			response.disconnect();
		}
	}

}
