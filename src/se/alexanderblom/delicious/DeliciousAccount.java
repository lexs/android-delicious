package se.alexanderblom.delicious;

import se.alexanderblom.delicious.http.Authentication;
import se.alexanderblom.delicious.http.BasicAuthentication;
import se.alexanderblom.delicious.http.Request;
import se.alexanderblom.delicious.util.Crypto;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.provider.Settings.Secure;

import com.google.common.base.Charsets;

public class DeliciousAccount implements Authentication {
	private AccountManager accountManager;
	private Account account;
	
	private Crypto crypto;
	
	public static DeliciousAccount get(Context context) {
		AccountManager accountManager = AccountManager.get(context.getApplicationContext());
		Account accounts[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if (accounts.length > 0) {
			return new DeliciousAccount(context, accountManager, accounts[0]);
		} else {
			return null;
		}
	}
	
	public static DeliciousAccount create(Context context, String username, String password) {
		AccountManager accountManager = AccountManager.get(context.getApplicationContext());
		
		Account account = new Account(username, Constants.ACCOUNT_TYPE);
		DeliciousAccount deliciousAccount = new DeliciousAccount(context, accountManager, account);
		
		accountManager.addAccountExplicitly(account, deliciousAccount.encryptPassword(password), null);
		
		return deliciousAccount;
	}
	
	private DeliciousAccount(Context context, AccountManager accountManager, Account account) {
		this.accountManager = accountManager;
		this.account = account;
		
		// Use ANDROID_ID as key
		String key = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		crypto = new Crypto(key);
	}

	public String getUsername() {
		return account.name;
	}
	
	public Account get() {
		return account;
	}


	
	private String encryptPassword(String password) {
		byte[] output = crypto.encryptAsBase64(password.getBytes(Charsets.UTF_8));
		return new String(output, Charsets.UTF_8);
	}
	
	private String getPassword() {
		String encrypted = accountManager.getPassword(account);
		byte[] output = crypto.decryptAsBase64(encrypted.getBytes(Charsets.UTF_8));
		return new String(output, Charsets.UTF_8);
	}

	@Override
	public void authenticate(Request request) {
		getAuth().authenticate(request);
	}
	
	private Authentication getAuth() {
		String username = account.name;
		String password = getPassword();

		return new BasicAuthentication(username, password);
	}
}
