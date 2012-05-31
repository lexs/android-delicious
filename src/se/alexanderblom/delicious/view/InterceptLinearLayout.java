 package se.alexanderblom.delicious.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class InterceptLinearLayout extends LinearLayout {
	public interface OnInterceptListener {
		boolean isFlyoutOpen();
		void shouldClose();
		void shouldOpen();
	}
	
	private OnInterceptListener interceptListener;
	private int edgeSlop;
	private int touchSlop;
	
	private int ignoreIndex = -1;
	private boolean watchingGesture = false;
	private float startX;
	
	public InterceptLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}

	public InterceptLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	public InterceptLinearLayout(Context context) {
		super(context);
		
		init();
	}
	
	private void init() {
		ViewConfiguration config = ViewConfiguration.get(getContext());
		
		edgeSlop = config.getScaledEdgeSlop();
		touchSlop = config.getScaledTouchSlop();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (interceptListener == null) {
			return false;
		}
		
		if (event.findPointerIndex(ignoreIndex) != -1) {
			return false;
		}
		
		int action = event.getActionMasked();
		if (interceptListener.isFlyoutOpen()) {
			// It's open, close it when the user releases his finger
			if (action == MotionEvent.ACTION_UP) {
				interceptListener.shouldClose();
			}
		} else if (action == MotionEvent.ACTION_MOVE && watchingGesture) {
			// It's closed, open if we drag right
			float distance = event.getRawX() - startX;
			if (distance > touchSlop) {
				if (interceptListener != null) {
					interceptListener.shouldOpen();
					
					// Make sure we don't handle this events up later
					ignoreIndex = event.getPointerId(0);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// New touch event, don't ignore it
		ignoreIndex = -1;
		watchingGesture = false;
		
		if (interceptListener == null) {
			return false;
		}
		
		if (interceptListener.isFlyoutOpen()) {
			// Steal all the events
			return true;
		} else if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && ev.getX() < edgeSlop) {
			// If it's closed, only steal events close to the left edge
			startX = ev.getRawX();
			watchingGesture = true;
			
			return true;
		} else {
			return false;
		}
	}

	public void setOnInterceptListener(OnInterceptListener listener) {
		interceptListener = listener;
	}
}
