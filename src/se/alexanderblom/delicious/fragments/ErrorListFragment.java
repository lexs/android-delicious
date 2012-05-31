package se.alexanderblom.delicious.fragments;

import se.alexanderblom.delicious.R;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ErrorListFragment extends ListFragment {
	private View errorView;
	private View listView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list_error, container, false);
		
		errorView = v.findViewById(R.id.error_container);
		listView = v.findViewById(R.id.list_container);
		
		Button retryButton = (Button) v.findViewById(R.id.button_retry);
		retryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				retry();
			}
		});
		
		return v;
	}
	
	private void retry() {
		errorView.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		
		onRetry();
	}
	
	protected void showError() {
		errorView.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
	}
	
	protected void onRetry() {
	}
}
