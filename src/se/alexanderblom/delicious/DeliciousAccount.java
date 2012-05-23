package se.alexanderblom.delicious;

import java.net.HttpURLConnection;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

public class DeliciousAccount {
	private static final String TAG = "DeliciousAccount";
	
	private AccountManager accountManager;
	
	public DeliciousAccount(Context context) {
		accountManager = AccountManager.get(context.getApplicationContext());
	}
	
	public String getUsername() {
		Account account = get();
		return account != null ? account.name : null;
	}
	
	public Account get() {
		Account accounts[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if (accounts.length > 0) {
			return accounts[0];
		} else {
			return null;
		}
	}
	
	public boolean exists() {
		return get() != null;
	}
	
	public void addAuth(HttpURLConnection urlConnection) {
		Account account = get();
		if (account == null) {
			Log.e(TAG, "No account");
			
			// TODO: Let application handle this
			throw new RuntimeException("No account");
		} else {
			String username = account.name;
			String password = accountManager.getPassword(account);
			
			addAuth(urlConnection, username, password);
		}
	}
	
	public static void addAuth(HttpURLConnection urlConnection, String username, String password) {
		// We do auth ourselves instead of using Authenticator because we don't
		// want to find ourselves in a loop if credentials are wrong
		String auth = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
		urlConnection.setRequestProperty("Authorization", auth);
	}
}
