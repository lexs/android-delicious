package se.alexanderblom.delicious.view;

import se.alexanderblom.delicious.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.TypedValue;
import android.widget.TextView;

public class ListTextView extends TextView {
	// Hard space
	private static final char SEPARATOR = '\u00a0';
	
	private CharSequence text;
	private BufferType type;
	
	private String moreText;
	private int moreColor;
	
	public ListTextView(Context context) {
		this(context, null);
	}

	public ListTextView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
	}

	public ListTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ListTextView);
		
		moreText = a.getString(R.styleable.ListTextView_moreText);
		if (moreText == null) {
			moreText = " + %d";
		}
		
		moreColor = a.getColor(R.styleable.ListTextView_moreTextColor, 0);
		if (moreColor == 0) {
			TypedValue outValue = new TypedValue();
			getContext().getTheme().resolveAttribute(android.R.attr.textColorTertiary, outValue, true);
			
			moreColor = getResources().getColor(outValue.resourceId);
		}
		
		a.recycle();
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(limitText(text), type);
		
		this.text = text;
		this.type = type;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		CharSequence shortened = limitText(text);
		super.setText(shortened, type);
	}

	private CharSequence limitText(CharSequence text) {
		// Don't do any work if it fits or if we are zero width
		if (!tooLong(text) || getWidth() == 0) {
			return text;
		}
		
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		
		int numRemoved = 0;
		
		for (int i = builder.length() - 1; i >= 0; i--) {
			char c = builder.charAt(i);
			
			if (c == SEPARATOR) {
				builder.delete(i, builder.length());
				numRemoved++;
				
				// Does it fit now?
				if (!tooLong(builder)) {
					String more = String.format(moreText, numRemoved);
					int moreLength = more.length() + 1;
					
					builder.append(' ');
					builder.append(more);
					
					builder.setSpan(new ForegroundColorSpan(moreColor), builder.length() - moreLength, builder.length(), 0);
					
					// Check if the more text actually fits
					if (!tooLong(builder)) {
						return builder;
					}
				}
			}
		}
		
		// Nothing seems to fit, we'll
		// just set the whole text then
		return text;
	}

	private boolean tooLong(CharSequence text) {
		TextPaint paint = getPaint();
		float width = availableWidth();
		
		return measure(paint, text) > width;
	}
	
	private int availableWidth() {
		return getWidth() - getPaddingLeft() - getPaddingRight();
	}
	
	private int measure(TextPaint p, CharSequence text) {
		return (int) FloatMath.ceil(p.measureText(text, 0, text.length()));
	}
}
