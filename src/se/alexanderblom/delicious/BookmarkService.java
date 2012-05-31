package se.alexanderblom.delicious;

import java.io.IOException;

import se.alexanderblom.delicious.http.Request;
import se.alexanderblom.delicious.http.Response;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public class BookmarkService extends IntentService {
	private static final String TAG = "BookmarkService";

	public static final String ACTION_SAVE_BOOKMARK = "save_bookmark";
	
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_NOTES = "notes";
	public static final String EXTRA_TAGS = "tags";
	public static final String EXTRA_SHARED = "shared";
	
	public static final String EXTRA_RECEIVER = "receiver";
	
	public static final int RESULT_SUCCESSFULL = 1;
	public static final int RESULT_FAILED = 2;
	
	private Handler handler;
	
	public BookmarkService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
		
		if (ACTION_SAVE_BOOKMARK.equals(intent.getAction())) {
			String url = intent.getStringExtra(EXTRA_URL);
			String title = intent.getStringExtra(EXTRA_TITLE);
			String notes = intent.getStringExtra(EXTRA_NOTES);
			String tags = intent.getStringExtra(EXTRA_TAGS);
			boolean shared = intent.getBooleanExtra(EXTRA_SHARED, true);

			boolean success = saveBookmark(url, title, notes, tags, shared);
			if (success) {
				handler.post(new DisplayToast(this, R.string.toast_bookmark_saved, Toast.LENGTH_SHORT));
				receiver.send(RESULT_SUCCESSFULL, null);
			} else {
				handler.post(new DisplayToast(this, R.string.toast_bookmark_saved_failed, Toast.LENGTH_SHORT));
				receiver.send(RESULT_FAILED, null);
			}
		}
	}
	
	private boolean saveBookmark(String link, String title, String notes, String tags, boolean shared) {
		String url = Uri.parse("https://api.del.icio.us/v1/posts/add").buildUpon()
				.appendQueryParameter("url", link)
				.appendQueryParameter("description", title)
				.appendQueryParameter("extended", notes)
				.appendQueryParameter("tags", tags)
				.appendQueryParameter("shared", shared ? "yes" : "no")
				.build()
				.toString();
		
		
		try {
			Response response = Request.get(url)
					.addAuth(DeliciousAccount.get(this))
					.execute();
			
			try {
				int code = response.getStatusCode();
				
				if (code == 200) {
					return true;
				} else if (code == 401) {
					// Unauthorized
					Log.e(TAG, "401 Unauthorized");
					
					return false;
				} else {
					// Unknown response
					Log.e(TAG, "Unknown response code: " + response);
					
					return false;
				}
			} finally {
				response.disconnect();
			}
		} catch (IOException e) {
			Log.e(TAG, "Error saving bookmark", e);
			
			return false;
		}
	}

	private static class DisplayToast implements Runnable {
		private String text;
		private Context context;
		private int duration;
		
		public DisplayToast(Context context, int text, int duration) {
			this(context, context.getString(text), duration);
		}

		public DisplayToast(Context context, String text, int duration) {
			this.context = context;
			this.text = text;
			this.duration = duration;
		}

		@Override
		public void run() {
			Toast.makeText(context, text, duration).show();
		}
	}
}
