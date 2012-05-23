package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.fragments.TagListFragment;
import android.app.Fragment;

public class TagListActivity extends MainActivity {
	@Override
	protected Fragment createFragment() {
		return new TagListFragment();
	}
}
