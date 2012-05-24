package se.alexanderblom.delicious.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.LineBackgroundSpan;

public class BackgroundSpan implements LineBackgroundSpan {
	private Drawable d;
	
	public BackgroundSpan(Drawable d) {
		this.d = d;
	}
	
	@Override
	public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline,
			int bottom, CharSequence text, int start, int end, int lnum) {
		Rect bounds = new Rect(left, top, right, bottom);
		d.setBounds(bounds);
		
		d.draw(c);
	}

}
