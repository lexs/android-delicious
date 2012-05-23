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
		return inflater.inflate(RESOURCE, parent, false);
	}

	@Override
	public void bindView(View v, Tag tag) {
		View colorView = v.findViewById(R.id.tagColor);
		TextView nameView = (TextView) v.findViewById(R.id.name);
		TextView countView = (TextView) v.findViewById(R.id.count);
		
		colorView.setBackgroundColor(TagsBinder.generateColor(tag.getName()));
		nameView.setText(tag.getName());
		countView.setText(String.valueOf(tag.getCount()));
	}

}
