package se.alexanderblom.delicious;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.alexanderblom.delicious.util.AbstractTextWatcher;
import se.alexanderblom.delicious.util.AsyncLoader;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TitleFetcher implements LoaderCallbacks<String> {
	private static final String TAG = "TitleFetcher";
	
	private Activity activity;
	
	private EditText urlView;
	private EditText titleView;
	
	public TitleFetcher(Activity activity) {
		this.activity = activity;
		
		urlView = (EditText) activity.findViewById(R.id.url);
        titleView = (EditText) activity.findViewById(R.id.title);
		
        urlView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					maybeFetchTitle();
				}
				
				// Return false to have the keyboard to the the next field
				return false;
			}
		});
	}
	
	
	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		titleView.setHint(R.string.field_title_fetching);
		
		titleView.addTextChangedListener(new AbstractTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// If title is changed when we are fetching it, abort
				activity.getLoaderManager().destroyLoader(0);
				
				Log.d(TAG, "Got input, cancelling title fetch");
			}
		});
		
		String url = urlView.getText().toString();
		return new TitleLoader(activity, url);
	}

	@Override
	public void onLoadFinished(Loader<String> loader, String title) {
		titleView.setHint(R.string.field_title);
		
		titleView.setText(title);
		
		// Select text so the user can easily change it
		titleView.selectAll();
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {
		// TODO Auto-generated method stub
	}
	
	public void maybeFetchTitle() {
		if (TextUtils.isEmpty(titleView.getText().toString())
				&& Patterns.WEB_URL.matcher(urlView.getText().toString()).find()) {
			fetchTitle();
		}
	}
	
	private void fetchTitle() {
		activity.getLoaderManager().restartLoader(0, null, this);
	}
	
	private static class TitleLoader extends AsyncLoader<String> {
		private static final String TAG = "TitleLoader";
		
		private static final Pattern PATTERN_TITLE = Pattern.compile("<title>(.*?)</title>");
		
		private String url;
		
		public TitleLoader(Context context, String url) {
			super(context);
			
			this.url = url;
		}

		@Override
		public String loadInBackground() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
				
				try {
					String html = toString(new BufferedInputStream(request.getInputStream()));

					Matcher m = PATTERN_TITLE.matcher(html);
					if (m.find()) {
						return m.group(1).trim();
					} else {
						return null;
					}
				} finally {
					request.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to fetch title for: " + url, e);
				
				return null;
			}
		}
		
		private String toString(InputStream is) {
		    try {
		        return new java.util.Scanner(is).useDelimiter("\\A").next();
		    } catch (java.util.NoSuchElementException e) {
		        return "";
		    }
		}
	}
}
