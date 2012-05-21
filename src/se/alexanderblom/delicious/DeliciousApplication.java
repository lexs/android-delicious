package se.alexanderblom.delicious;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;

public class DeliciousApplication extends Application {
	private static Drawable errorDrawable;

	@Override
	public void onCreate() {
		super.onCreate();
		
		System.setProperty("http.agent", Constants.HTTP_AGENT);

		int errorDimen = getResources().getDimensionPixelSize(R.dimen.ic_editext_error_dimen);

		errorDrawable = getResources().getDrawable(R.drawable.ic_editext_error);
		errorDrawable.setBounds(0, 0, errorDimen, errorDimen);
		
		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
		}
	}

	public static Drawable getErrorDrawable() {
		return errorDrawable;
	}
}
