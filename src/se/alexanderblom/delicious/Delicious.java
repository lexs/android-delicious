package se.alexanderblom.delicious;

import java.net.HttpURLConnection;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

public class Delicious {
	private static final String TAG = "Delicious";
	
	public static boolean hasAccount(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		
		Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			Log.d(TAG, "No account");
			
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean addAuth(Context context, HttpURLConnection urlConnection) {
		AccountManager accountManager = AccountManager.get(context);
		
		Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			Log.e(TAG, "No account");
			
			// TODO: Use exception
			return false;
		} else {
			Account account = accounts[0];
			
			String username = account.name;
			String password = accountManager.getPassword(account);
			
			addAuth(urlConnection, username, password);
			
			return true;
		}
	}
	
	public static void addAuth(HttpURLConnection urlConnection, String username, String password) {
		// We do auth ourselves instead of using Authenticator because we don't
		// want to find ourselves in a loop if credentials are wrong
		String auth = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
		urlConnection.setRequestProperty("Authorization", auth);
	}
}
