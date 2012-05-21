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
	
	private static final String RECENTS_URL = "https://api.del.icio.us/v1/json/posts/recent";
	
	private DeliciousAccount deliciousAccount;
	
	private PostsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deliciousAccount = new DeliciousAccount(getActivity());
		adapter = new PostsAdapter(getActivity());
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		BaseActivity activity = (BaseActivity) getActivity();
		if (activity.hasAccount()) {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.menu_post_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			Log.d(TAG, "Refreshing posts");
			reloadPosts();
			
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
		return new PostsLoader(getActivity(), deliciousAccount, RECENTS_URL);
	}

	@Override
	public void onLoadFinished(Loader<List<Post>> loader, List<Post> posts) {
		adapter.clear();
		adapter.addAll(posts);
		setListAdapter(adapter);
	}

	@Override
	public void onLoaderReset(Loader<List<Post>> loader) {
		adapter.clear();
	}
	
	public void reloadPosts() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	public void loadPosts() {
		getLoaderManager().initLoader(0, null, this);
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
