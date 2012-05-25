package se.alexanderblom.delicious.util;

import android.os.Build;

public class Android {
	public static boolean isGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}
	
	public static boolean isHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
	
	public static boolean isICS() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}
	
	/**
	 * Check the api level of the device we're running on 
	 * @param level API level
	 * @return	true if same or higher
	 */
	public static boolean isAPI(int level) {
		return Build.VERSION.SDK_INT >= level;
	}
	
	private Android() {}
}
