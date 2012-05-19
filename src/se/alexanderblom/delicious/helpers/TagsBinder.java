package se.alexanderblom.delicious.helpers;

import java.util.List;

import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class TagsBinder {
	private static final float SATURATION = 0.7f;
	private static final float LIGHTNESS = 0.9f;
	
	// This gives nice looking colors and is consistent
	private static final int HASH_SEED = 1337;
	private static final HashFunction HASH_FUNCTION = Hashing.murmur3_32(HASH_SEED);
	
	public void bind(TextView v, List<String> tags) {
		CharSequence tagList = buildTagList(tags);
		v.setText(tagList, TextView.BufferType.SPANNABLE);
	}

	private CharSequence buildTagList(List<String> tags) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		
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
		float hue = calculateHue(text);
		return HSBtoColor(hue, SATURATION, LIGHTNESS);
	}
	
	private float calculateHue(String text) {
		int hash = HASH_FUNCTION.hashString(text, Charsets.UTF_8).asInt();
		
		float old_min = Integer.MIN_VALUE;
		float old_max = Integer.MAX_VALUE;
		float hue = ((hash - old_min) / (old_max - old_min)) * (1f - 0f);
		
		// Return limited in range (1f, 0f)
		return Math.max(0f, Math.min(hue, 1f));
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
}
