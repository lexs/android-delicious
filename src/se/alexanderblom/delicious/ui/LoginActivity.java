package se.alexanderblom.delicious.ui;

import java.io.IOException;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.DeliciousApplication;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.ProgressDialogFragment;
import se.alexanderblom.delicious.http.BasicAuthentication;
import se.alexanderblom.delicious.http.Request;
import se.alexanderblom.delicious.http.Response;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LoginActivity extends AccountAuthenticatorActivity {
	private static final String TAG = "LoginActivity";
	
	private static final String DIALOG_TAG = "logging_in";
	
	private EditText usernameView;
	private EditText passwordView;
	
	private Drawable errorDrawable;
	
	private DeliciousAccount deliciousAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);
		
		// Enable user to press enter when done
		passwordView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					handleLogin();
					
					return true;
				} else {
					return false;
				}
			}
		});
		
		View loginButton = findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleLogin();
			}
		});
		
		errorDrawable = DeliciousApplication.getErrorDrawable();
		
		deliciousAccount = new DeliciousAccount(this);
		if (deliciousAccount.exists()) {
			Toast.makeText(this, R.string.toast_account_exists, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void handleLogin() {
		String username = usernameView.getText().toString();
		String password = passwordView.getText().toString();
		
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			// Validation
			if (TextUtils.isEmpty(username)) {
				usernameView.setError(getString(R.string.field_empty_error, getString(R.string.field_username)), errorDrawable);
			}
			
			if (TextUtils.isEmpty(password)) {
				passwordView.setError(getString(R.string.field_empty_error, getString(R.string.field_password)), errorDrawable);
			}
 		} else {
			new LoginTask(this, username, password).execute();
		}
	}
	
	private void finishLogin(Account account) {
		Bundle result = new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
		
		setAccountAuthenticatorResult(result);

		setResult(RESULT_OK);
		finish();
	}

	private class LoginTask extends AsyncTask<Void, Void, Account> {
		private AccountManager accountManager;
		
		private String username;
		private String password;
		
		public LoginTask(Context context, String username, String password) {
			accountManager = AccountManager.get(context);
			
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(getString(R.string.dialog_logging_in));
			dialog.show(getFragmentManager(), DIALOG_TAG);
		}
		
		@Override
		protected Account doInBackground(Void... params) {
			try {
				Response response = Request.get("https://api.del.icio.us/v1/posts/update")
						.addAuth(new BasicAuthentication(username, password))
						.execute();
				
				try {
					int code = response.getStatusCode();
					
					if (code == 200) {
						return createAccount();
					} else if (code == 401) {
						// Unauthorized
						Log.e(TAG, "401 Unauthorized");
						
						return null;
					} else {
						// Unknown response
						Log.e(TAG, "Unknown response code: " + response);
						
						return null;
					}
				} finally {
					response.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Login failed", e);
				
				return null;
			}
		}
		
		private Account createAccount() {
			// We do this on here (on a separate thread) because this will cause
			// disk access
			Account account = new Account(username, Constants.ACCOUNT_TYPE);
			accountManager.addAccountExplicitly(account, DeliciousAccount.encryptPassword(password), null);
			
			return account;
		}

		@Override
		protected void onPostExecute(Account account) {
			if (account != null) {
				finishLogin(account);
			} else {
				ProgressDialogFragment dialog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(DIALOG_TAG);
				dialog.dismiss();
				
				Toast.makeText(LoginActivity.this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
