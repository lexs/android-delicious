package se.alexanderblom.delicious.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InterceptLinearLayout extends LinearLayout {
	private OnTouchListener interceptListener;
	
	public InterceptLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public InterceptLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InterceptLinearLayout(Context context) {
		super(context);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (interceptListener != null) {
			return interceptListener.onTouch(this, ev);
		} else {
			return false;
		}
	}
	
	public void setOnInterceptListener(OnTouchListener listener) {
		interceptListener = listener;
	}
}
