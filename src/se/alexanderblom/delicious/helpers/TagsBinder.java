package se.alexanderblom.delicious.helpers;

import java.util.List;

import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.widget.TextView;

public class TagsBinder {
	private static final float SATURATION = 0.7f;
	private static final float LIGHTNESS = 0.9f;
	
	public void bind(TextView v, List<String> tags) {
		SpannableStringBuilder builder = createStringBuilder(v);
		CharSequence tagList = buildTagList(tags, builder);
		
		v.setText(tagList, TextView.BufferType.SPANNABLE);
	}
	
	private SpannableStringBuilder createStringBuilder(TextView v) {
		CharSequence text = v.getText();
		
		if (text != null && text instanceof SpannableStringBuilder) {
			Log.d("Hej", "reusing builder");
			
			SpannableStringBuilder builder = (SpannableStringBuilder) text;
			builder.clear();
			
			return builder;
		} else {
			return new SpannableStringBuilder();
		}
	}
	
	private CharSequence buildTagList(List<String> tags, SpannableStringBuilder builder) {
		for (String tag : tags) {
			int pos = builder.length();
			
			// Use spaces around tag
			builder.append(' ');
			builder.append(tag);
			builder.append(' ');
			
			int color = generateColor(tag);
			builder.setSpan(new BackgroundColorSpan(color), pos, builder.length(), 0);
			
			builder.append(' ');
		}
		
		return builder;
	}
	
	private int generateColor(String text) {
		float old = text.hashCode();
		float old_min = Integer.MIN_VALUE;
		float old_max = Integer.MAX_VALUE;
		float hue = ((old - old_min) / (old_max - old_min)) * (1f - 0f);
		
		hue = Math.max(0f, Math.min(hue, 1f));
		
		//return Color.HSVToColor(new float[] { hue, SATURATION, LIGHTNESS });
		return HSBtoColor(hue, SATURATION, LIGHTNESS);
	}
	
	/**
	 * Adapted from Color.java in Android
	 * 
	 * See https://github.com/android/platform_frameworks_base/blob/master/graphics/java/android/graphics/Color.java
	 */
	private static int HSBtoColor(float h, float s, float b) {
		//h = MathUtils.constrain(h, 0.0f, 1.0f);
		//s = MathUtils.constrain(s, 0.0f, 1.0f);
		//b = MathUtils.constrain(b, 0.0f, 1.0f);

		float red = 0.0f;
		float green = 0.0f;
		float blue = 0.0f;

		final float hf = (h - (int) h) * 6.0f;
		final int ihf = (int) hf;
		final float f = hf - ihf;
		final float pv = b * (1.0f - s);
		final float qv = b * (1.0f - s * f);
		final float tv = b * (1.0f - s * (1.0f - f));

		switch (ihf) {
			case 0: // Red is the dominant color
				red = b;
				green = tv;
				blue = pv;
				break;
			case 1: // Green is the dominant color
				red = qv;
				green = b;
				blue = pv;
				break;
			case 2:
				red = pv;
				green = b;
				blue = tv;
				break;
			case 3: // Blue is the dominant color
				red = pv;
				green = qv;
				blue = b;
				break;
			case 4:
				red = tv;
				green = pv;
				blue = b;
				break;
			case 5: // Red is the dominant color
				red = b;
				green = pv;
				blue = qv;
				break;
		}

		return 0xFF000000 | (((int) (red * 255.0f)) << 16)
				| (((int) (green * 255.0f)) << 8) | ((int) (blue * 255.0f));
	}
/*
	private int HSLToRGB(float h, float s, float l) {
		 float c = (1 - Math.abs(2.f * l - 1.f)) * s;
		 float h_ = h / 60.f;
		 float h_mod2 = h_;
		 if (h_mod2 >= 4.f) h_mod2 -= 4.f;
		 else if (h_mod2 >= 2.f) h_mod2 -= 2.f;
		 
		 float x = c * (1 - Math.abs(h_mod2 - 1));
		 float r_, g_, b_;
		 if (h_ < 1)      { r_ = c; g_ = x; b_ = 0; }
		 else if (h_ < 2) { r_ = x; g_ = c; b_ = 0; }
		 else if (h_ < 3) { r_ = 0; g_ = c; b_ = x; }
		 else if (h_ < 4) { r_ = 0; g_ = x; b_ = c; }
		 else if (h_ < 5) { r_ = x; g_ = 0; b_ = c; }
		 else             { r_ = c; g_ = 0; b_ = x; }
		 
		 float m = l - (0.5f * c); 
		 int r = (int)((r_ + m) * (255.f) + 0.5f);
		 int g = (int)((g_ + m) * (255.f) + 0.5f);
		 int b = (int)((b_ + m) * (255.f) + 0.5f);
		 int rgb = r << 16 | g << 8 | b;
		 
		 return rgb | 0xff000000; // ensure alpha 1
	}
*/
}
