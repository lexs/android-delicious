package se.alexanderblom.delicious.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ListAdapter<T> extends BaseAdapter {
	private LayoutInflater inflater;
	
	private List<T> list;
	
	public ListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}
	
	public ListAdapter(Context context, List<T> list) {
		this(context);
		
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public T getItem(int position) {
		return list != null ? list.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public final View getView(int position, View v, ViewGroup parent) {
		if (v == null) {
			v = newView(inflater, parent, position);
		}
		
		bindView(v, list.get(position));
		
		return v;
	}
	
	@Override
	public final View getDropDownView(int position, View v, ViewGroup parent) {
		if (v == null) {
			v = newDropDownView(inflater, parent, position);
		}
		
		bindDropDownView(v, list.get(position));
		
		return v;
	}
	
	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
		notifyDataSetChanged();
	}
	
	public void clear() {
		setList(null);
	}

	public abstract View newView(LayoutInflater inflater, ViewGroup parent, int position);
	public abstract void bindView(View v, T item);
	
	public View newDropDownView(LayoutInflater inflater, ViewGroup parent, int position) { return newView(inflater, parent, position); }
	public void bindDropDownView(View v, T item) { bindView(v, item); }
}
