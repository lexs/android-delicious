package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.DeliciousAccount;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base a activity which handles making sure that the user
 * has added an account and if necessary asking the user to
 * do so.
 */
public class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
	
	private static final int REQUEST_LOGIN = 1;
	
	private DeliciousAccount deliciousAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deliciousAccount = new DeliciousAccount(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Check if account is still there
		checkAccount();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "Login okay");
				accountChanged(deliciousAccount);
			} else {
				// Login task may have been reset, check if account exists
				if (deliciousAccount.exists()) {
					Log.d(TAG, "Login reset");
					accountChanged(deliciousAccount);
				} else {
					Log.d(TAG, "Login aborted");
					finish();
				}

			}
		}
	}
	
	/**
	 * Called if the account changes after launch, for example if a user
	 * is asked to login and does so.
	 */
	protected void accountChanged(DeliciousAccount account) {
	}
	
	protected void checkAccount() {
		if (!deliciousAccount.exists()) {
			Log.d(TAG, "Account missing, asking user to add one");
			
			Intent intent = new Intent(this, LoginActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivityForResult(intent, REQUEST_LOGIN);
		}
	}
	
	public boolean hasAccount() {
		return deliciousAccount.exists();
	}
	
	public DeliciousAccount getAccount() {
		return deliciousAccount;
	}
}
