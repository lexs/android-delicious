package se.alexanderblom.delicious.util;

import android.text.Editable;
import android.text.TextWatcher;

public class AbstractTextWatcher implements TextWatcher {
	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
