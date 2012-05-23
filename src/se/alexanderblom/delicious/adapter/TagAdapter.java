package se.alexanderblom.delicious.adapter;

import se.alexanderblom.delicious.R;
import se.alexanderblom.delicious.helpers.TagsBinder;
import se.alexanderblom.delicious.model.Tag;
import se.alexanderblom.delicious.util.ListAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TagAdapter extends ListAdapter<Tag> {
	private static final int RESOURCE = R.layout.item_tag;
	
	public TagAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(LayoutInflater inflater, ViewGroup parent, int position) {
		View v = inflater.inflate(RESOURCE, parent, false);
		
		ViewHolder holder = new ViewHolder();
		holder.colorView = v.findViewById(R.id.tagColor);
		holder.nameView = (TextView) v.findViewById(R.id.name);
		holder.countView = (TextView) v.findViewById(R.id.count);
		v.setTag(holder);
		
		return v;
	}

	@Override
	public void bindView(View v, Tag tag) {
		ViewHolder holder = (ViewHolder) v.getTag();
		
		holder.colorView.setBackgroundColor(TagsBinder.generateColor(tag.getName()));
		holder.nameView.setText(tag.getName());
		holder.countView.setText(String.valueOf(tag.getCount()));
	}

	private static class ViewHolder {
		public View colorView;
		public TextView nameView;
		public TextView countView;
	}
}
