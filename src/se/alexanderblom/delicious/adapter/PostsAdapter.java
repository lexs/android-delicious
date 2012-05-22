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
		View v = inflater.inflate(RESOURCE, parent, false);
		
		ViewHolder holder = new ViewHolder();
		holder.titleView = (TextView) v.findViewById(R.id.title);
		holder.timeView = (TextView) v.findViewById(R.id.time);
		holder.urlView = (TextView) v.findViewById(R.id.url);
		holder.tagsView = (TextView) v.findViewById(R.id.tags);
		v.setTag(holder);
		
		return v;
	}

	@Override
	public void bindView(View v, Post post) {
		ViewHolder holder = (ViewHolder) v.getTag();
		
		holder.titleView.setText(post.getTitle());
		holder.timeView.setText(DateUtils.getRelativeTimeSpanString(post.getTime()));
		holder.urlView.setText(post.getLink());
		holder.tagsView.setText(post.getStyledTags(), TextView.BufferType.SPANNABLE);
	}
	
	private static class ViewHolder {
		public TextView titleView;
		public TextView timeView;
		public TextView urlView;
		public TextView tagsView;
	}
}
