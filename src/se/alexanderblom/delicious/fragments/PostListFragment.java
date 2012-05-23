package se.alexanderblom.delicious.fragments;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.adapter.PostsAdapter;
import se.alexanderblom.delicious.model.Post;
import se.alexanderblom.delicious.model.PostsParser;
import se.alexanderblom.delicious.ui.BaseActivity;
import se.alexanderblom.delicious.util.AsyncLoader;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class PostListFragment extends ListFragment implements LoaderCallbacks<List<Post>> {
	private static final String TAG = "PostListFragment";

	private static final String ARG_URL = "url";
	private static final int POSTS_LOADER = 1;

	private DeliciousAccount deliciousAccount;

	private PostsAdapter adapter;
	
	public static PostListFragment newInstance(String url) {
		PostListFragment f = new PostListFragment();
		f.setArguments(createArgs(url));
		
		return f;
	}
	
	public static Bundle createArgs(String url) {
		Bundle args = new Bundle();
		args.putString(ARG_URL, url);
		
		return args;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deliciousAccount = new DeliciousAccount(getActivity());
		adapter = new PostsAdapter(getActivity());
		
		setListAdapter(adapter);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		BaseActivity activity = (BaseActivity) getActivity();
		if (activity.hasAccount()) {
			setListShown(false);
			getLoaderManager().initLoader(POSTS_LOADER, null, this);
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
			Log.d(TAG, "Refreshing posts");
			getLoaderManager().restartLoader(POSTS_LOADER, null, this);

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Post post = adapter.getItem(position);

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getLink()));
		startActivity(intent);
	}

	@Override
	public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
		String url = getArguments().getString(ARG_URL);
		
		setListShown(false);
		return new PostsLoader(getActivity(), deliciousAccount, url);
	}

	@Override
	public void onLoadFinished(Loader<List<Post>> loader, List<Post> posts) {
		if (posts != null) {
			adapter.setList(posts);
			setListShown(true);
		} else {
			// There was an error
			showLoadingError();
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Post>> loader) {
		adapter.clear();
	}

	private void showLoadingError() {
		getLoaderManager().destroyLoader(POSTS_LOADER);
		
		ConnectionErrorFragment f = ConnectionErrorFragment.newInlineError(this);
		getFragmentManager().beginTransaction()
				.detach(this)
				.add(getId(), f)
				.commitAllowingStateLoss();
	}

	private static class PostsLoader extends AsyncLoader<List<Post>> {
		private static final String TAG = "PostsLoader";

		private DeliciousAccount account;
		private String url;

		public PostsLoader(Context context, DeliciousAccount account, String url) {
			super(context);

			this.account = account;
			this.url = url;
		}

		@Override
		public List<Post> loadInBackground() {
			try {
				HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
				account.addAuth(request);

				try {
					InputStream is = request.getInputStream();

					return new PostsParser(new BufferedInputStream(is)).getPosts();
				} finally {
					request.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to fetch posts", e);

				return null;
			}
		}
	}
}
