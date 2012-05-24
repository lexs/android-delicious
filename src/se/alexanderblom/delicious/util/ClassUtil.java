package se.alexanderblom.delicious.util;

public class ClassUtil {
	public static <T> T newInstance(Class<T> cl) {
		try {
			return cl.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private ClassUtil() {}
}
