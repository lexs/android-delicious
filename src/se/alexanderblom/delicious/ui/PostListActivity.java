package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.PostListFragment;
import android.app.Fragment;
import android.os.Bundle;

public class PostListActivity extends ContainerActivity {
	public static final String EXTRA_TAG = "tag";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String tag = getIntent().getStringExtra(EXTRA_TAG);
		if (tag != null) {
			setTitle(getString(R.string.page_tag, tag));
		} else {
			setTitle(R.string.page_recent);
		}
	}

	@Override
	protected Fragment createFragment(Bundle savedInstanceState) {
		String tag = getIntent().getStringExtra(EXTRA_TAG);
		return PostListFragment.newInstance(tag);
	}
}
