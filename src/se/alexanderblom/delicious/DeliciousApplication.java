package se.alexanderblom.delicious;

import android.app.Application;
import android.graphics.drawable.Drawable;

public class DeliciousApplication extends Application {
	private static Drawable errorDrawable;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		int errorDimen = getResources().getDimensionPixelSize(R.dimen.ic_editext_error_dimen);
		
		errorDrawable = getResources().getDrawable(R.drawable.ic_editext_error);
        errorDrawable.setBounds(0, 0, errorDimen, errorDimen);
	}

	public static Drawable getErrorDrawable() {
		return errorDrawable;
	}
}
