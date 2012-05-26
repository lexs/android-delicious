package se.alexanderblom.delicious.fragments;

import java.io.IOException;
import java.util.List;

import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.adapter.TagsAdapter;
import se.alexanderblom.delicious.http.Request;
import se.alexanderblom.delicious.http.Response;
import se.alexanderblom.delicious.model.Tag;
import se.alexanderblom.delicious.model.TagsParser;
import se.alexanderblom.delicious.ui.BaseActivity;
import se.alexanderblom.delicious.ui.MainActivity;
import se.alexanderblom.delicious.ui.PostListActivity;
import se.alexanderblom.delicious.util.AsyncLoader;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class TagListFragment extends ListFragment implements LoaderCallbacks<List<Tag>> {
	private static final String TAG = "TagListFragment";
	
	private static final String TAGS_URL = "https://api.del.icio.us/v1/json/tags/get";
	private static final int TAGS_LOADER = 1;
	
	private DeliciousAccount account;
	private TagsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		account = new DeliciousAccount(getActivity());
		adapter = new TagsAdapter(getActivity());
		
		setListAdapter(adapter);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		BaseActivity activity = (BaseActivity) getActivity();
		if (activity.hasAccount()) {
			setListShown(false);
			getLoaderManager().initLoader(TAGS_LOADER, null, this);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		Activity activity = getActivity();
		activity.setTitle(R.string.page_tags);
		
		if (activity instanceof MainActivity) {
			MainActivity main = (MainActivity) activity;
			main.setSelectedPage(R.id.page_tags);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_refresh, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			Log.d(TAG, "Refreshing tags");
			getLoaderManager().restartLoader(TAGS_LOADER, null, this);

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Tag tag = adapter.getItem(position);
		String tagName = tag.getName();
		
		Intent intent = new Intent(getActivity(), PostListActivity.class)
				.putExtra(PostListActivity.EXTRA_TAG, tagName);
		
		startActivity(intent);
	}
	
	@Override
	public Loader<List<Tag>> onCreateLoader(int id, Bundle args) {
		setListShown(false);
		return new TagsLoader(getActivity(), account, TAGS_URL);
	}

	@Override
	public void onLoadFinished(Loader<List<Tag>> loader, List<Tag> tags) {
		if (tags != null) {
			adapter.setList(tags);
			setListShown(true);
		} else {
			// There was an error
			showLoadingError();
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Tag>> loader) {
		adapter.clear();
	}
	
	private void showLoadingError() {
		getLoaderManager().destroyLoader(TAGS_LOADER);
		
		ConnectionErrorFragment f = ConnectionErrorFragment.newInlineError(this);
		getFragmentManager().beginTransaction()
				.detach(this)
				.add(getId(), f)
				.commitAllowingStateLoss();
	}

	private static class TagsLoader extends AsyncLoader<List<Tag>> {
		private DeliciousAccount account;
		private String url;
	
		public TagsLoader(Context context, DeliciousAccount account, String url) {
			super(context);
			
			this.account = account;
			this.url = url;
		}

		@Override
		public List<Tag> loadInBackground() {
			try {
				Response response = Request.get(url)
						.addAuth(account.getAuth())
						.execute();

				try {
					return new TagsParser(response.getReader()).getTags();
				} finally {
					response.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to fetch posts", e);

				return null;
			}
		}
	}
}
