package se.alexanderblom.delicious.helpers;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.ui.AddBookmarkActivity;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ClipboardHandler implements ClipboardManager.OnPrimaryClipChangedListener {
	private static final String TAG = "ClipboardHandler";
	
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	private static final int LINK_TIMEOUT = 10 * 1000; // 10 seconds
	
	private Activity activity;
	private Handler handler;
	
	private ClipboardManager clipboard;
	
	private View clipboardDisplay;
	private TextView clipboardLinkView;
	
	private String url;
	
	public ClipboardHandler(Activity activity) {
		this.activity = activity;
		
		handler = new Handler();
		
		clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		
		clipboardDisplay = activity.findViewById(R.id.clipboard_display);
		clipboardLinkView = (TextView) activity.findViewById(R.id.clipboard_link);
		
		View clipboardButton = activity.findViewById(R.id.clipboard_save_button);
		clipboardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveClipboardLink();
			}
		});
	}
	
	public void onPause() {
		clipboard.removePrimaryClipChangedListener(this);
	}
	
	public void onResume() {
		clipboard.addPrimaryClipChangedListener(this);
		
		checkClipboard();
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
			if (Patterns.WEB_URL.matcher(text).find() && !text.equals(url)) {
				Log.d(TAG, "New web url found: " + text);
				
				url = text.toString();
				
				displayClipboard(text);
			}
		} else {
			clipboardDisplay.setVisibility(View.GONE);
		}
	}
	
	private void displayClipboard(CharSequence url) {
		clipboardDisplay.setVisibility(View.VISIBLE);
		clipboardLinkView.setText(url);
		
		// Hide link in after a while
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				clipboardDisplay.setVisibility(View.GONE);
			}
		}, LINK_TIMEOUT);
		
	}
	
	private void saveClipboardLink() {
		Intent intent = new Intent(Intent.ACTION_SEND, null, activity, AddBookmarkActivity.class)
			.putExtra(Intent.EXTRA_TEXT, url);
		
		activity.startActivity(intent);
	}
}
