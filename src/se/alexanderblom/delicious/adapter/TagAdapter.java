package se.alexanderblom.delicious.adapter;

import se.alexanderblom.delicious.model.Tag;
import se.alexanderblom.delicious.util.ListAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TagAdapter extends ListAdapter<Tag> {
	public TagAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(LayoutInflater inflater, ViewGroup parent, int position) {
		return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
	}

	@Override
	public void bindView(View v, Tag tag) {
		TextView nameView = (TextView) v.findViewById(android.R.id.text1);
		nameView.setText(tag.getName());
	}

}
