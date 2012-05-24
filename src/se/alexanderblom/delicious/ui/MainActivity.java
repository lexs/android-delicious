package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.ClipboardFragment;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public abstract class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	
	private static final String META_MENU_ID = "menu_id";
	
	private Menu menu;
	private boolean flyoutShown = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flyout_menu);
		
		getActionBar().setCustomView(R.layout.action_bar_navigation);
		getActionBar().setHomeButtonEnabled(true);
		setupNavigation();

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(new ClipboardFragment(), ClipboardFragment.TAG)
					.add(R.id.content, createFragment())
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
		
		updateUsername(getAccount());
	}
	
	protected abstract Fragment createFragment();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (!flyoutShown) {
					displayFlyoutMenu();
				} else {
					hideFlyoutMenu();
				}
				
				flyoutShown = !flyoutShown;
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
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		
		TextView titleView = (TextView) getActionBar().getCustomView().findViewById(R.id.page_title);
		titleView.setText(title);
	}
	
	@Override
	protected void accountChanged(DeliciousAccount account) {
		// Just replace our old fragment, this works when an error is shown too
		getFragmentManager().beginTransaction()
				.replace(R.id.content, createFragment())
				.commit();
		
		updateUsername(account);
	}
	
	private void displayFlyoutMenu() {
		final View flyoutView = findViewById(R.id.flyout_menu);
		int width = flyoutView.getWidth();
		
		flyoutView.setTranslationX(-width);
		flyoutView.setVisibility(View.VISIBLE);
		flyoutView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		final View containerView = findViewById(R.id.container);
		containerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		flyoutView.animate().translationX(0f).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				flyoutView.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
		
		containerView.animate().translationX(width).alpha(0.5f).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				containerView.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
		
		containerView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				containerView.setOnTouchListener(null);
				hideFlyoutMenu();
				return true;
			}
		});
		
		getActionBar().getCustomView().setVisibility(View.INVISIBLE);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setVisible(false);
		}
	}
	
	private void hideFlyoutMenu() {
		final View flyoutView = findViewById(R.id.flyout_menu);
		int width = flyoutView.getWidth();
		
		flyoutView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		final View containerView = findViewById(R.id.container);
		containerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		flyoutView.animate().translationX(-width).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				flyoutView.setLayerType(View.LAYER_TYPE_NONE, null);
				flyoutView.setVisibility(View.INVISIBLE);
			}
		});
		containerView.animate().translationX(0f).alpha(1f).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				containerView.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
		
		getActionBar().getCustomView().setVisibility(View.VISIBLE);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setVisible(true);
		}
	}
	
	private void setupNavigation() {
		View v = getActionBar().getCustomView();
		final PopupMenu menu = new PopupMenu(this, v);
		menu.inflate(R.menu.menu_navigation);
		
		// Check if we should hide any option
		int menuId = getMenuId();
		if (menuId != 0) {
			// Hide item
			menu.getMenu().findItem(menuId).setVisible(false);
		}
		
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				switch (id) {
					case R.id.menu_recent:
						Intent recent = new Intent(MainActivity.this, RecentPostsActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						
						startActivity(recent);
						break;
					case R.id.menu_tags:
						startActivity(new Intent(MainActivity.this, TagListActivity.class));
						break;
					default:
						return false;
				}
				
				return true;
			}
		});
		
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.show();
			}
		});
	}
	
	private int getMenuId() {
		try {
			ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
			
			if (ai.metaData != null) {
				return ai.metaData.getInt(META_MENU_ID, 0);
			} else {
				return 0;
			}
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}

	private void updateUsername(DeliciousAccount account) {
		TextView usernameView = (TextView) getActionBar().getCustomView().findViewById(R.id.username);
		usernameView.setText(account.getUsername());
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
}
