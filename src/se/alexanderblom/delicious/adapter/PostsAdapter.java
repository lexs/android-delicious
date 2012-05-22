package se.alexanderblom.delicious.adapter;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.model.Post;
import se.alexanderblom.delicious.util.ListAdapter;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PostsAdapter extends ListAdapter<Post> {
	private static final int RESOURCE = R.layout.item_post;
	
	public PostsAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(LayoutInflater inflater, ViewGroup parent, int position) {
		return inflater.inflate(RESOURCE, parent, false);
	}

	@Override
	public void bindView(View v, Post post) {
		TextView titleView = (TextView) v.findViewById(R.id.title);
		TextView timeView = (TextView) v.findViewById(R.id.time);
		TextView urlView = (TextView) v.findViewById(R.id.url);
		TextView tagsView = (TextView) v.findViewById(R.id.tags);
		
		titleView.setText(post.getTitle());
		timeView.setText(DateUtils.getRelativeTimeSpanString(post.getTime()));
		urlView.setText(post.getLink());
		tagsView.setText(post.getStyledTags(), TextView.BufferType.SPANNABLE);
	}
}
