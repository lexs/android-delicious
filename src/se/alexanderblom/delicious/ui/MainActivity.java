package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.helpers.ClipboardHandler;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private ClipboardHandler clipboarHandler;
	private DeliciousAccount deliciousAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		clipboarHandler = new ClipboardHandler(this);
		deliciousAccount = new DeliciousAccount(this);
		
		checkAccount();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		clipboarHandler.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		clipboarHandler.onResume();
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
