package se.alexanderblom.delicious;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
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
	
	public static final String EXTRA_LAUNCH = "launch";
	
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
	
	@Override
	public void finish() {
		super.finish();
		
		// Should we start an activity?
		Intent intent = getIntent().getParcelableExtra(EXTRA_LAUNCH);
		if (intent != null) {
			startActivity(intent);
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
			new LoginTask(username, password).execute();
		}
	}
	
	private void finishLogin(String username, String password) {
		Account account = new Account(username, Constants.ACCOUNT_TYPE);
		
		AccountManager manager = AccountManager.get(this);
		manager.addAccountExplicitly(account, password, null);
		
		Bundle result = new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
		
		setAccountAuthenticatorResult(result);
		finish();
	}
	
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {
		private String username;
		private String password;
		
		public LoginTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(getString(R.string.dialog_logging_in));
			dialog.show(getFragmentManager(), DIALOG_TAG);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				HttpURLConnection request = (HttpURLConnection) new URL("https://api.del.icio.us/v1/posts/update").openConnection();
				DeliciousAccount.addAuth(request, username, password);
				
				try {
					int response = request.getResponseCode();
					
					if (response == 200) {
						return true;
					} else if (response == 401) {
						// Unauthorized
						Log.e(TAG, "401 Unauthorized");
						
						return false;
					} else {
						// Unknown response
						Log.e(TAG, "Unknown response code: " + response);
						
						return false;
					}
				} finally {
					request.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Login failed", e);
				
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				finishLogin(username, password);
			} else {
				ProgressDialogFragment dialog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(DIALOG_TAG);
				dialog.dismiss();
			}
		}
	}
}
