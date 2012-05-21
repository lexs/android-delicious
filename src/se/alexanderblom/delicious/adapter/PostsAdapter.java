package se.alexanderblom.delicious.adapter;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.model.Post;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PostsAdapter extends ArrayAdapter<Post> {
	private static final int RESOURCE = R.layout.item_post;
	
	private LayoutInflater inflater;
	
	public PostsAdapter(Context context) {
		super(context, 0);
		
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if (v == null) {
			v = inflater.inflate(RESOURCE, parent, false);
		}
		
		TextView titleView = (TextView) v.findViewById(R.id.title);
		TextView timeView = (TextView) v.findViewById(R.id.time);
		TextView urlView = (TextView) v.findViewById(R.id.url);
		TextView tagsView = (TextView) v.findViewById(R.id.tags);
		
		Post post = getItem(position);
		titleView.setText(post.getTitle());
		timeView.setText(DateUtils.getRelativeTimeSpanString(post.getTime()));
		urlView.setText(post.getLink());
		tagsView.setText(post.getStyledTags(), TextView.BufferType.SPANNABLE);
		
		return v;
	}
}
