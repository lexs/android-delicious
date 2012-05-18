package se.alexanderblom.delicious;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

class CallbackReceiver extends ResultReceiver {
	public interface Callback {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}
	
	private Callback receiver;

	public CallbackReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Callback receiver) {
		this.receiver = receiver;
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (receiver != null) {
			receiver.onReceiveResult(resultCode, resultData);
		}
	}
}