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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ClipboardFragment extends Fragment implements ClipboardManager.OnPrimaryClipChangedListener {
	public static final String TAG = "ClipboardFragment";
	
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	private static final int LINK_TIMEOUT = 6 * 1000; // 6 seconds
	
	private static String lastUrl = null;
	
	private Handler handler;
	
	private ClipboardManager clipboard;
	
	private View clipboardDisplay;
	private TextView clipboardLinkView;
	
	private String url;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		handler = new Handler();
		
		clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		
		clipboardDisplay = getActivity().findViewById(R.id.clipboard_display);
		clipboardLinkView = (TextView) getActivity().findViewById(R.id.clipboard_link);
		
		View clipboardButton = getActivity().findViewById(R.id.clipboard_save_button);
		clipboardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveClipboardLink();
			}
		});
		
		if (savedInstanceState == null) {
			// Just launched, check clipboard
			checkClipboard();
		}
		
		// We have no ui
		setUserVisibleHint(false);
		
		// We don't have any heavy state
		setRetainInstance(true);
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
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Odd bug which requires this for savedInstanceState to not be null
		// See http://code.google.com/p/android/issues/detail?id=31732
		outState.putString("", "");
	}
	
	@Override
	public void onPrimaryClipChanged() {
		Log.d(TAG, "Clipboard changed");
		
		checkClipboard();
	}
	
	private void checkClipboard() {
		// Check that the clip is plain text
		if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			CharSequence text = item.getText();
			
			// Check if it's a web url and that we have not previously seen it
			if (!text.equals(lastUrl) && Patterns.WEB_URL.matcher(text).find() && !text.equals(url)) {
				Log.d(TAG, "New web url found: " + text);
				
				lastUrl = url = text.toString();
				
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
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hideClipboard();
			}
		}, LINK_TIMEOUT);
	}
	
	private void showClipboard() {
		clipboardDisplay.setVisibility(View.VISIBLE);
	}
	
	private void hideClipboard() {
		clipboardDisplay.setVisibility(View.GONE);
	}
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
				.putExtra(Intent.EXTRA_TEXT, url);
		
		startActivity(intent);
	}
}
