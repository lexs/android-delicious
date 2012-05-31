package se.alexanderblom.delicious;

import se.alexanderblom.delicious.http.Authentication;
import se.alexanderblom.delicious.http.BasicAuthentication;
import se.alexanderblom.delicious.util.Crypto;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.google.common.base.Charsets;

public class DeliciousAccount {
	private static final String TAG = "DeliciousAccount";
	
	private AccountManager accountManager;
	
	private static final Crypto crypto = new Crypto(SecureConstants.ENCRYPTION_KEY);

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
			String password = getPassword(account);
			
			return new BasicAuthentication(username, password);
		}
	}
	
	public static String encryptPassword(String password) {
		byte[] output = crypto.encryptAsBase64(password.getBytes(Charsets.UTF_8));
		return new String(output, Charsets.UTF_8);
	}
	
	private String getPassword(Account account) {
		String encrypted = accountManager.getPassword(account);
		byte[] output = crypto.decryptAsBase64(encrypted.getBytes(Charsets.UTF_8));
		return new String(output, Charsets.UTF_8);
	}
}
