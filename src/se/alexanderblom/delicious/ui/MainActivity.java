package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.R.id;
import se.alexanderblom.delicious.R.layout;
import se.alexanderblom.delicious.R.menu;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity implements ClipboardManager.OnPrimaryClipChangedListener {
	private static final String TAG = "MainActivity";
	
	private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	
	private ClipboardManager clipboard;
	
	private View clipboardDisplay;
	private TextView clipboardLinkView;
	
	private DeliciousAccount deliciousAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		clipboardDisplay = findViewById(R.id.clipboard_display);
		clipboardLinkView = (TextView) findViewById(R.id.clipboard_link);
		
		View clipboardButton = findViewById(R.id.clipboard_save_button);
		clipboardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveClipboardLink();
			}
		});
		
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		
		deliciousAccount = new DeliciousAccount(this);
		
		checkAccount();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		clipboard.removePrimaryClipChangedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		clipboard.addPrimaryClipChangedListener(this);
		
		checkClipboard();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				startActivity(new Intent(this, AddBookmarkActivity.class));
				break;
			case R.id.menu_logout:
				logout();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		
		return true;
	}
	
	@Override
	public void onPrimaryClipChanged() {
		Log.d(TAG, "Clipboard changed");
		
		checkClipboard();
	}
	
	private void logout() {
		Log.d(TAG, "Removing account");
		
		AccountManager accountManager = AccountManager.get(this);
		Account accounts[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		Account account = accounts[0];
		
		// Callback to wait for the account to actually be removed
		AccountManagerCallback<Boolean> callback = new AccountManagerCallback<Boolean>() {
			@Override
			public void run(AccountManagerFuture<Boolean> future) {
				try {
					if (future.getResult()) {
						checkAccount();
					} else {
						// Could not remove account, should not happen
						Log.e(TAG, "Could not remove account");
					}
				} catch (Exception e) {
					Log.e(TAG, "Error fetching remove account result", e);
				}
			}
		};
		
		accountManager.removeAccount(account, callback, null);
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
		
		Intent intent = new Intent(Intent.ACTION_SEND, null, this, AddBookmarkActivity.class)
			.putExtra(Intent.EXTRA_TEXT, url);
		
		startActivity(intent);
	}
	
	private void checkAccount() {
		if (!deliciousAccount.exists()) {
			// Ask user to add an account
			Intent intent = new Intent(this, LoginActivity.class)
			.putExtra(LoginActivity.EXTRA_LAUNCH, new Intent(this, MainActivity.class));
		
			startActivity(intent);
			finish();
		}
	}
}
