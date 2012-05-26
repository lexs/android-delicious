package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.PostListFragment;
import se.alexanderblom.delicious.fragments.TagListFragment;
import se.alexanderblom.delicious.view.InterceptLinearLayout;
import se.alexanderblom.delicious.view.InterceptLinearLayout.OnInterceptListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends ContainerActivity {
	private static final String TAG_LIST = "tag_list";
	private static final String META_MENU_ID = "menu_id";
	
	private ActionMode.Callback flyoutMenuCallback;
	private ActionMode actionMode = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		flyoutMenuCallback = new FlyoutActionModeCallback();

		setupFlyout();
	}
	
	@Override
	protected int getContentResource() {
		return R.layout.activity_main;
	}

	@Override
	protected Fragment createFragment(Bundle savedInstanceState) {
		return PostListFragment.newInstance(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				showFlyoutMenu();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		
		return true;
	}
	
	public void showFlyoutMenu() {
		if (actionMode == null) {
			actionMode = startActionMode(flyoutMenuCallback);
		}
	}

	public void hideFlyoutMenu() {
		if (actionMode != null) {
			actionMode.finish();
			actionMode = null;
		}
	}
	
	public void setSelectedItem(int id) {
		View flyoutView = findViewById(R.id.flyout_menu);
		TextView recentView = (TextView) flyoutView.findViewById(R.id.menu_recent);
		TextView tagsView = (TextView) flyoutView.findViewById(R.id.menu_tags);
		
		recentView.setCompoundDrawables(null, null, null, null);
		tagsView.setCompoundDrawables(null, null, null, null);
		
		if (id == R.id.menu_recent) {
			recentView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flyout_item_indicator, 0);
		} else if (id == R.id.menu_tags) {
			tagsView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flyout_item_indicator, 0);
		}
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
							getFragmentManager().popBackStack(TAG_LIST, FragmentManager.POP_BACK_STACK_INCLUSIVE);
							break;
						case R.id.menu_tags:
							FragmentManager fm = getFragmentManager();
							if (!fm.popBackStackImmediate(TAG_LIST, 0)
									&& fm.findFragmentByTag(TAG_LIST) == null) {
								// Only add tag list if we can't find it
								fm.beginTransaction()
										.replace(R.id.content, new TagListFragment(), TAG_LIST)
										.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
										.addToBackStack(TAG_LIST)
										.commit();
							}
							break;
					}
					}
				}, 300);
				
				if (actionMode != null) {
					actionMode.finish();
				}
			}
		};
		
		View flyoutView = findViewById(R.id.flyout_menu);
		TextView recentView = (TextView) flyoutView.findViewById(R.id.menu_recent);
		TextView tagsView = (TextView) flyoutView.findViewById(R.id.menu_tags);
		
		recentView.setOnClickListener(onClick);
		tagsView.setOnClickListener(onClick);

		final InterceptLinearLayout interceptView = (InterceptLinearLayout) findViewById(R.id.container);
		interceptView.setOnInterceptListener(new OnInterceptListener() {
			@Override
			public boolean isFlyoutOpen() {
				return actionMode != null;
			}
			
			@Override
			public void shouldOpen() {
				showFlyoutMenu();
			}
			
			@Override
			public void shouldClose() {
				hideFlyoutMenu();
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

	private class FlyoutActionModeCallback implements ActionMode.Callback {
		private static final float ALPHA = 0.6f;
		
		private Paint contentPaint;
		
		public FlyoutActionModeCallback() {
			contentPaint = new Paint();

			// This will desaturate colors
			float[] transform = {
				1, 0, 0, 0, 0, 
				0, 1, 0, 0, 0,
				0, 0, 1, 0, 0, 
				0, 0, 0, 1, 0
			};
			
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(transform);
			contentPaint.setColorFilter(filter);
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle(R.string.menu_title);

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
			
			containerView.animate().translationX(width).alpha(ALPHA).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					containerView.setLayerType(View.LAYER_TYPE_HARDWARE, contentPaint);
				}
			});
			
			return true;
		}
		
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
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
				public void onAnimationEnd(Animator animation) {
					containerView.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			});
			
			actionMode = null;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}
	};
}
