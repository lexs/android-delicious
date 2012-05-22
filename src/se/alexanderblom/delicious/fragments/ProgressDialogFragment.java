package se.alexanderblom.delicious.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class ProgressDialogFragment extends DialogFragment {
	private static final String EXTRA_MESSAGE = "message";
	
	public static ProgressDialogFragment newInstance(String message) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_MESSAGE, message);
		
		ProgressDialogFragment f = new ProgressDialogFragment();
		f.setArguments(bundle);
		
		return f;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString(EXTRA_MESSAGE);
		
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setIndeterminate(true);
		dialog.setMessage(message);
		
		return dialog;
	}
}