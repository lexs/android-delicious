package se.alexanderblom.delicious.fragments;

import java.io.IOException;
import java.util.List;

import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.adapter.PostsAdapter;
import se.alexanderblom.delicious.http.Request;
import se.alexanderblom.delicious.http.Response;
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
	
	private static final String RECENTS_URL = "https://api.del.icio.us/v1/json/posts/recent";

	private static final String ARG_TAG = "tag";
	private static final int POSTS_LOADER = 1;

	private DeliciousAccount deliciousAccount;
	private PostsAdapter adapter;
	
	private String tag;
	
	public static PostListFragment newInstance(String tag) {
		PostListFragment f = new PostListFragment();
		f.setArguments(createArgs(tag));
		
		return f;
	}
	
	public static Bundle createArgs(String tag) {
		Bundle args = new Bundle();
		args.putString(ARG_TAG, tag);
		
		return args;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments() != null ? getArguments() : Bundle.EMPTY;
		tag = args.getString(ARG_TAG);
		
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
	public void onStart() {
		super.onStart();
		
		if (tag != null) {
			getActivity().setTitle(getString(R.string.page_tag, tag));
		} else {
			getActivity().setTitle(R.string.page_recent);
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
		String url = RECENTS_URL;
		if (tag != null) {
			url = Uri.parse("https://api.del.icio.us/v1/json/posts/all").buildUpon()
					.appendQueryParameter("tag", tag)
					.build()
					.toString();
		} 
		
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
				Response response = Request.get(url)
						.addAuth(account.getAuth())
						.execute();

				try {
					return new PostsParser(response.getReader()).getPosts();
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
