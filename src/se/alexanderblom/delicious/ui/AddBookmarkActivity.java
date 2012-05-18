package se.alexanderblom.delicious.ui;

import se.alexanderblom.delicious.BookmarkService;
import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.DeliciousApplication;
import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.helpers.TitleFetcher;
import se.alexanderblom.delicious.util.DetachableResultReceiver;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddBookmarkActivity extends Activity implements DetachableResultReceiver.Receiver {
	private static final String DIALOG_TAG = "saving_link";
	
	private EditText urlView;
	private EditText titleView;
	private EditText notesView;
	private MultiAutoCompleteTextView tagsView;
	private CheckBox privateView;
	
	private DeliciousAccount deliciousAccount;
	private DetachableResultReceiver receiver;
	
	private TitleFetcher titleFetcher;
	
	private Drawable errorDrawable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_add_bookmark);
        
        setTitle(R.string.activity_add_bookmark_title);
        
        errorDrawable = DeliciousApplication.getErrorDrawable();
        
        titleFetcher = new TitleFetcher(this);
        deliciousAccount = new DeliciousAccount(this);
        
        receiver = new DetachableResultReceiver(new Handler());
        
        urlView = (EditText) findViewById(R.id.url);
        titleView = (EditText) findViewById(R.id.title);
        notesView = (EditText) findViewById(R.id.notes);
        tagsView = (MultiAutoCompleteTextView) findViewById(R.id.tags);
        privateView = (CheckBox) findViewById(R.id.mark_private);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, TAGS);
        tagsView.setAdapter(adapter);
        tagsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        // Enable user to press enter when done
        tagsView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					saveBookmark();
					
					return true;
				} else {
					return false;
				}
			}
		});
        
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
        	String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        	String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        	
        	urlView.setText(url);
        	titleView.setText(title);
        	
        	// Check were focus should go
        	if (TextUtils.isEmpty(url)) {
        		urlView.requestFocus();
        	} else if (TextUtils.isEmpty(title)) {
        		titleView.requestFocus();
        	} else {
        		// Focus tags because it can't be prefilled
                tagsView.requestFocus();
        	}
        }
        
        // Fetch title if necessary
        titleFetcher.maybeFetchTitle();
        
        checkAccount();
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		
		// Clear receiver so no leaks
		receiver.setReceiver(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		receiver.setReceiver(this);
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_add_bookmark, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
				finish();
			case R.id.menu_cancel:
				finish();
				break;
			case R.id.menu_save:
				saveBookmark();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		
		return true;
	}
	
	private void checkAccount() {
		if (!deliciousAccount.exists()) {
			// Ask user to add an account
			Intent intent = new Intent(this, LoginActivity.class)
				.putExtra(LoginActivity.EXTRA_LAUNCH, getIntent());
			
			startActivity(intent);
			finish();
		}
	}

	private boolean isValidBookmark() {
		boolean valid = true;
		
		String url = urlView.getText().toString();
		String title = titleView.getText().toString();
		
		if (TextUtils.isEmpty(url)) {
			valid = false;
			
			urlView.setError(getString(R.string.field_empty_error, getString(R.string.field_url)), errorDrawable);
		} else if (!Patterns.WEB_URL.matcher(url).find()) {
			valid = false;
				
			urlView.setError(getString(R.string.field_url_error), errorDrawable);
		}
			
		if (TextUtils.isEmpty(title)) {
			valid = false;
			
			titleView.setError(getString(R.string.field_empty_error, getString(R.string.field_title)), errorDrawable);
		}
		
		return valid;
	}
	
	private void saveBookmark() {
		if (!isValidBookmark()) {
			return;
		}
		
		// Hide keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
		
		FragmentManager fm = getFragmentManager();
		ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(getString(R.string.dialog_saving_message));
		dialog.setCancelable(false);
		dialog.show(fm, DIALOG_TAG);
		
		String url = urlView.getText().toString();
		String title = titleView.getText().toString();
		String notes = notesView.getText().toString();
		String tags = tagsView.getText().toString();
		boolean markedPrivate = privateView.isChecked();
		
		Intent intent = new Intent(BookmarkService.ACTION_SAVE_BOOKMARK, null, this, BookmarkService.class)
			.putExtra(BookmarkService.EXTRA_URL, url)
			.putExtra(BookmarkService.EXTRA_TITLE, title)
			.putExtra(BookmarkService.EXTRA_NOTES, notes)
			.putExtra(BookmarkService.EXTRA_TAGS, tags)
			.putExtra(BookmarkService.EXTRA_SHARED, !markedPrivate)
			.putExtra(BookmarkService.EXTRA_RECEIVER, receiver);
		
		startService(intent);
	}
	
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		if (resultCode == BookmarkService.RESULT_SUCCESSFULL) {
			// Bookmark was successfully saved, we can safely exit
			finish();
		} else {
			ProgressDialogFragment dialog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(DIALOG_TAG);
			dialog.dismiss();
		}
	}
	
	private static final String[] TAGS = new String[] {
        "android", "reddit", "design", "sweden", "sweddit", "sweet", "python"
    };
}