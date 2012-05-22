package se.alexanderblom.delicious.fragments;

import se.alexanderblom.delicious.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Dialog fragment suitable for communicating a connection error to the user.
 * 
 * Can operate in two modes:
 * 
 * <b>Dialog:</b>
 * Shows a dialog which allows the user to retry or cancel, the choice will be sent
 * back via onActivityResult().
 *   
 * <b>View:</b>
 * Detach the current Fragment and if the user retries attach it again
 */
public class ConnectionErrorFragment extends DialogFragment implements OnCancelListener {
	public static final int RESULT_RETRY = 1;
	public static final int RESULT_CANCEL = 1;
	
	private static final String ARG_CANCELABLE = "cancleable";
	
	private boolean cancelable = false;
	
	public static ConnectionErrorFragment newInlineError(Fragment parent) {
		if (parent.getId() == 0) {
			throw new IllegalArgumentException("parent does not show a view");
		}
		
		ConnectionErrorFragment fragment = new ConnectionErrorFragment();
		fragment.setTargetFragment(parent, 0);
		
		return fragment;
	}
	
	public static ConnectionErrorFragment newDialogError(Fragment parent, int requestCode, boolean cancelable) {
		Bundle args = new Bundle();
		args.putBoolean(ARG_CANCELABLE, cancelable);
		
		ConnectionErrorFragment fragment = new ConnectionErrorFragment();
		fragment.setTargetFragment(parent, requestCode);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if (args != null) {
			cancelable = args.getBoolean(ARG_CANCELABLE, cancelable);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.dialog_connection_error_title)
				.setMessage(R.string.dialog_connection_error_message)
				.setCancelable(true)
				.setPositiveButton(R.string.dialog_button_retry, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						retry();
					}
				});
			
		if (cancelable) {
			builder.setCancelable(true)
					.setNegativeButton(R.string.dialog_button_cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel();
						}
					});
		}
		
		return builder.create();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getShowsDialog()) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
		View v = inflater.inflate(R.layout.dialog_connection_error, container, false);

		Button retryButton = (Button) v.findViewById(R.id.button_retry);
		retryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				retry();
			}
		});
		
		return v;
	}
	
	private void cancel() {
		getFragmentManager().beginTransaction()
				.remove(this)
				.show(getTargetFragment())
				.commit();
		
		getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CANCEL, null);
	}
	
	private void retry() {
		if (!getShowsDialog()) {
			// Inline
			getFragmentManager().beginTransaction()
					.remove(this)
					.attach(getTargetFragment())
					.commit();
		} else {
			// Dialog
			getFragmentManager().beginTransaction()
					.remove(this)
					.show(getTargetFragment())
					.commit();
			
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_RETRY, null);
		}
	}
}
