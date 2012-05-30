package se.alexanderblom.delicious.fragments;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.ui.AddBookmarkActivity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ClipboardFragment extends Fragment implements ClipboardManager.OnPrimaryClipChangedListener {
	private static final String TAG = "ClipboardFragment";
	
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	private static final int LINK_TIMEOUT = 6 * 1000; // 6 seconds
	
	private static String lastUrl = null;
	
	private Handler handler;
	private ClipboardManager clipboard;
	
	private TextView clipboardLinkView;
	private String currentUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new Handler();
		clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

		// Retain so we can keep our handler
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_clipboard, container, false);
		clipboardLinkView = (TextView) v.findViewById(R.id.clipboard_link);
		
		View clipboardButton = v.findViewById(R.id.clipboard_save_button);
		clipboardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveClipboardLink();
			}
		});
		
		return v;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		clipboard.removePrimaryClipChangedListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		clipboard.addPrimaryClipChangedListener(this);
		checkClipboard();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Avoid being called after we're gone
		handler.removeCallbacks(hideRunnable);
	}

	@Override
	public void onPrimaryClipChanged() {
		Log.d(TAG, "Clipboard changed");
		
		checkClipboard();
	}
	
	private void checkClipboard() {
		if (currentUrl != null) {
			clipboardLinkView.setText(currentUrl);
		} else if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			// Check that the clip is plain text
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			CharSequence text = item.getText();
			
			// Check if it's a web url and that we have not previously seen it
			if (!text.equals(lastUrl) && Patterns.WEB_URL.matcher(text).find() && !text.equals(currentUrl)) {
				Log.d(TAG, "New web url found: " + text);
				
				lastUrl = currentUrl = text.toString();
				
				displayClipboard(text);
			}
		} else {
			hideClipboard();
		}
	}
	
	private void displayClipboard(CharSequence url) {
		clipboardLinkView.setText(url);
		showClipboard();
		
		// Hide link after a while
		handler.removeCallbacks(hideRunnable);
		handler.postDelayed(hideRunnable, LINK_TIMEOUT);
	}
	
	private void showClipboard() {
		getFragmentManager().beginTransaction()
				.show(this)
				.commit();
	}
	
	private void hideClipboard() {
		currentUrl = null;
		getFragmentManager().beginTransaction()
				.hide(this)
				.commit();
	}
	
	private Runnable hideRunnable = new Runnable() {
		@Override
		public void run() {
			hideClipboard();
		}
	};
/*
	private void showClipboard() {
		// Animate transition
		final ViewGroup parent = (ViewGroup) clipboardDisplay.getParent();
		parent.startViewTransition(clipboardDisplay);
		parent.removeView(clipboardDisplay);
		
		clipboardDisplay.setAlpha(0f);
		clipboardDisplay.setVisibility(View.VISIBLE);
		clipboardDisplay.animate()
			.alpha(1f)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					parent.endViewTransition(clipboardDisplay);
					parent.addView(clipboardDisplay);
				}
			});
	}
	
	private void hideClipboard() {
		// Animate transition
		final ViewGroup parent = (ViewGroup) clipboardDisplay.getParent();
		parent.startViewTransition(clipboardDisplay);
		parent.removeView(clipboardDisplay);
		
		clipboardDisplay.animate()
			.alpha(0f)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					clipboardDisplay.setVisibility(View.GONE);
					parent.endViewTransition(clipboardDisplay);
					parent.addView(clipboardDisplay);
				}
			});
	}
*/
	private void saveClipboardLink() {
		Intent intent = new Intent(Intent.ACTION_SEND, null, getActivity(), AddBookmarkActivity.class)
				.putExtra(Intent.EXTRA_TEXT, currentUrl);
		
		startActivity(intent);
	}
}
