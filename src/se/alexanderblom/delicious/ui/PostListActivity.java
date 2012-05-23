package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.fragments.PostListFragment;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;

public class PostListActivity extends MainActivity {
	public static final String EXTRA_TAG = "tag";
	
	private static final String RECENTS_URL = "https://api.del.icio.us/v1/json/posts/recent";
	
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
	protected Fragment createFragment() {
		String url = RECENTS_URL;
		
		String tag = getIntent().getStringExtra(EXTRA_TAG);
		if (tag != null) {
			url = Uri.parse("https://api.del.icio.us/v1/json/posts/all").buildUpon()
					.appendQueryParameter("tag", tag)
					.build()
					.toString();
		}
		
		return PostListFragment.newInstance(url);
	}
}
