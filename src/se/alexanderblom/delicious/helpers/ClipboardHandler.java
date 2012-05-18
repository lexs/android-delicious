package se.alexanderblom.delicious.helpers;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.ui.AddBookmarkActivity;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ClipboardHandler implements ClipboardManager.OnPrimaryClipChangedListener {
	private static final String TAG = "ClipboardHandler";
	
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	
	private Activity activity;
	
	private ClipboardManager clipboard;
	
	private View clipboardDisplay;
	private TextView clipboardLinkView;
	
	public ClipboardHandler(Activity activity) {
		this.activity = activity;
		
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
		if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			CharSequence text = item.getText();
			
			// Check if it's a web url
			if (Patterns.WEB_URL.matcher(text).find()) {
				Log.d(TAG, "Web url found: " + text);
				
				displayClipboard(text);
			}
		} else {
			clipboardDisplay.setVisibility(View.GONE);
		}
	}
	
	private void displayClipboard(CharSequence url) {
		clipboardDisplay.setVisibility(View.VISIBLE);
		
		clipboardLinkView.setText(url);
	}
	
	private void saveClipboardLink() {
		CharSequence url = clipboardLinkView.getText();
		
		Intent intent = new Intent(Intent.ACTION_SEND, null, activity, AddBookmarkActivity.class)
			.putExtra(Intent.EXTRA_TEXT, url);
		
		activity.startActivity(intent);
	}
}
