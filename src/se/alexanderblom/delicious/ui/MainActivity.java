package se.alexanderblom.delicious.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.adapter.PostsAdapter;
import se.alexanderblom.delicious.helpers.ClipboardHandler;
import se.alexanderblom.delicious.model.Post;
import se.alexanderblom.delicious.model.PostsParser;
import se.alexanderblom.delicious.util.AsyncLoader;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ListActivity implements LoaderCallbacks<List<Post>> {
	private static final String TAG = "MainActivity";
	
	private ClipboardHandler clipboarHandler;
	
	private DeliciousAccount deliciousAccount;
	
	private PostsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		clipboarHandler = new ClipboardHandler(this);
		deliciousAccount = new DeliciousAccount(this);
		
		adapter = new PostsAdapter(this);
		setListAdapter(adapter);
		
		checkAccount();
		
		getLoaderManager().initLoader(0, null, this);
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
	
	@Override
	public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
		return new RecentsLoader(this, deliciousAccount);
	}

	@Override
	public void onLoadFinished(Loader<List<Post>> loader, List<Post> posts) {
		adapter.addAll(posts);
	}

	@Override
	public void onLoaderReset(Loader<List<Post>> loader) {
		adapter.clear();
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

	private static class RecentsLoader extends AsyncLoader<List<Post>> {
		private DeliciousAccount account;
		
		public RecentsLoader(Context context, DeliciousAccount account) {
			super(context);
			
			this.account = account;
		}

		@Override
		public List<Post> loadInBackground() {
			try {
				HttpURLConnection request = (HttpURLConnection) new URL("https://api.del.icio.us/v1/json/posts/recent").openConnection();
				account.addAuth(request);
				
				try {
					InputStream is = request.getInputStream();
					
					return new PostsParser(new BufferedInputStream(is)).getPosts();
				} finally {
					request.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to fetch recent", e);
				
				return null;
			}
		}
	}
}
