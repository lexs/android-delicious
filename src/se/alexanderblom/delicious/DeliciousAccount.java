package se.alexanderblom.delicious;

import se.alexanderblom.delicious.http.Authentication;
import se.alexanderblom.delicious.http.BasicAuthentication;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
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
	
	public Authentication getAuth() {
		Account account = get();
		if (account == null) {
			Log.e(TAG, "No account");
			// Don't add anything, simple let the request fail
			return null;
		} else {
			String username = account.name;
			String password = accountManager.getPassword(account);
			
			return new BasicAuthentication(username, password);
		}
	}
}
