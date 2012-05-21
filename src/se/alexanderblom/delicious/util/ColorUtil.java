package se.alexanderblom.delicious.util;

public class ColorUtil {
	public static float[] LABtoXYZ(Float L, float a, float b) {
		float y = (L + 16f) / 116f;
		float x = a / 500f + y;
		float z = y - b / 200f;

		if (Math.pow(y, 3f) > 0.008856f) {
			y = (float) Math.pow(y, 3f);
		} else {
			y = (y - 16f / 116f) / 7.787f;
		}
			
		if (Math.pow(x, 3f) > 0.008856f) {
			x = (float) Math.pow(x, 3f);
		} else {
			x = (x - 16f / 116f) / 7.787f;
		}
			
		if (Math.pow(z, 3f) > 0.008856f) {
			z = (float) Math.pow(z, 3f);
		} else {
			z = (z - 16f / 116f) / 7.787f;
		}
		
		x = 95.047f * x;
		y = 100.000f * y;
		z = 108.883f * z;
		
		return new float[] { x, y, z };
	}
	
	public static int XYZtoRGB(float[] xyz) {
		return XYZtoRGB(xyz[0], xyz[1], xyz[2]);
	}
	
	public static int XYZtoRGB(float x, float y, float z) {
		x /= 100f;
		y /= 100f;
		z /= 100f;
		
		float r = x *  3.2406f + y * -1.5372f + z * -0.4986f;
		float g = x * -0.9689f + y *  1.8758f + z *  0.0415f;
		float b = x *  0.0557f + y * -0.2040f + z *  1.0570f;
		
		if (r > 0.0031308) {
			r = 1.055f * (float) Math.pow(r, 1/2.4f) - 0.055f;
		} else {
			r = 12.92f * r;
		}
		
		if (g > 0.0031308) {
			g = 1.055f * (float) Math.pow(g, 1/2.4f) - 0.055f;
		} else {
			g = 12.92f * g;
		}
		
		if (b > 0.0031308) {
			b = 1.055f * (float) Math.pow(b, 1/2.4f) - 0.055f;
		} else {
			b = 12.92f * b;
		}
		
		return 0xFF000000 | (((int) (r * 255.0f)) << 16)
				| (((int) (g * 255.0f)) << 8) | ((int) (b * 255.0f));
	}
	
	public static int LABtoRGB(Float L, float a, float b) {
		float[] xyz = LABtoXYZ(L, a, b);
		return XYZtoRGB(xyz);
	}

	private ColorUtil() {
	}
}
