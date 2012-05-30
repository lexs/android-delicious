package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.ClipboardFragment;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class ContainerActivity extends BaseActivity {
	private static final String TAG = "ContainerActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getContentResource());
		
		getActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			ClipboardFragment clipboard = new ClipboardFragment();
			
			getFragmentManager().beginTransaction()
					.add(R.id.content, createFragment(savedInstanceState))
					.add(R.id.clipboard_display, clipboard)
					.hide(clipboard)
					.commit();
		}
		
		ViewGroup container = (ViewGroup) findViewById(R.id.container);
		LayoutTransition transition = new LayoutTransition();

		transition.setStartDelay(LayoutTransition.APPEARING, 0);
		transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
		transition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
		
		ObjectAnimator animator = ObjectAnimator.ofFloat(null, View.ALPHA, 1f, 0f);
		transition.setAnimator(LayoutTransition.DISAPPEARING, animator);
		
		container.setLayoutTransition(transition);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = new Intent(this, MainActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
				startActivity(intent);
				finish();
				break;
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
	protected void accountChanged(DeliciousAccount account) {
		// Just replace our old fragment, this works when an error is shown too
		// TODO: Will this work?
		getFragmentManager().beginTransaction()
				.replace(R.id.content, createFragment(null))
				.commit();
	}
	
	protected int getContentResource() {
		return R.layout.activity_container;
	}
	
	protected abstract Fragment createFragment(Bundle savedInstanceState);
	
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
}
