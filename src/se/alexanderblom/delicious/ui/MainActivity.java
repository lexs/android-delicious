package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.Constants;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.ClipboardFragment;
import se.alexanderblom.delicious.fragments.TagListFragment;
import se.alexanderblom.delicious.view.InterceptLinearLayout;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class MainActivity extends BaseActivity {
	private static final String TAG = "MainActivity";
	
	private static final String META_MENU_ID = "menu_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flyout_menu);
		
		//getActionBar().setCustomView(R.layout.action_bar_navigation);
		getActionBar().setHomeButtonEnabled(true);
		//setupNavigation();
		setupFlyout();

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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// TODO: Should be in a separate activity
				if (getMenuId() != 0) {
					displayFlyoutMenu();
				} else {
					Intent intent = new Intent(this, RecentPostsActivity.class)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
					startActivity(intent);
					finish();
				}
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
				.replace(R.id.content, createFragment())
				.commit();
		
		updateUsername(account);
	}
	
	private ActionMode actionMode = null;
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			hideFlyoutMenu();
			actionMode = null;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Menu");
			return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private void displayFlyoutMenu() {
		final View flyoutView = findViewById(R.id.flyout_menu);
		final View containerView = findViewById(R.id.container);
		
		int width = flyoutView.getWidth();
		flyoutView.animate().translationX(0f).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				flyoutView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				flyoutView.setVisibility(View.VISIBLE);
			}
			
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
		
		actionMode = startActionMode(actionModeCallback);
	}
	
	private void hideFlyoutMenu() {
		final View flyoutView = findViewById(R.id.flyout_menu);
		final View containerView = findViewById(R.id.container);

		int width = flyoutView.getWidth();
		flyoutView.animate().translationX(-width).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				flyoutView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				flyoutView.setLayerType(View.LAYER_TYPE_NONE, null);
				flyoutView.setVisibility(View.INVISIBLE);
			}
		});
		containerView.animate().translationX(0f).alpha(1f).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				containerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				containerView.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
	}
	
	private void setupFlyout() {
		int menuId = getMenuId();
		
		if (menuId == 0) {
			// No flyout menu
			return;
		}
		
		OnClickListener onClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int id = v.getId();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						switch (id) {
						case R.id.menu_recent:
							getFragmentManager().popBackStack("tag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
							/*Intent recent = new Intent(MainActivity.this, RecentPostsActivity.class)
									.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							
							startActivity(recent);*/
							break;
						case R.id.menu_tags:
							/*startActivity(new Intent(MainActivity.this, TagListActivity.class)
									.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));*/
							getFragmentManager().beginTransaction()
									.replace(R.id.content, new TagListFragment())
									.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
									.addToBackStack("tag")
									.commit();
							break;
					}
					}
				}, 300);
				
				if (actionMode != null) {
					actionMode.finish();
				}
				//hideFlyoutMenu();
			}
		};
		
		View flyoutView = findViewById(R.id.flyout_menu);
		TextView recentView = (TextView) flyoutView.findViewById(R.id.menu_recent);
		TextView tagsView = (TextView) flyoutView.findViewById(R.id.menu_tags);
		
		recentView.setOnClickListener(onClick);
		tagsView.setOnClickListener(onClick);
		
		if (menuId == R.id.menu_recent) {
			recentView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flyout_item_indicator, 0);
		} else if (menuId == R.id.menu_tags) {
			tagsView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flyout_item_indicator, 0);
		}
		
		final InterceptLinearLayout interceptView = (InterceptLinearLayout) findViewById(R.id.container);
		interceptView.setOnInterceptListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (actionMode != null) {
					actionMode.finish();
					
					return true;
				} else {
					return false;
				}
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
		getActionBar().setSubtitle(account.getUsername());
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
