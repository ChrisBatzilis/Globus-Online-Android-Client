package org.globus.globustransfer.listeners;

import org.globus.globustransfer.R;

import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * This class is used to monitor the selection status of the checkboxes
 * determining the tasks with statuses that we are interested in. When the
 * checkboxes are checked or unchecked the boolean selection value is saved on
 * the device's memory as SharedPreferences.
 * 
 * 
 * @author christos
 * 
 */
public class CheckBoxListener implements OnCheckedChangeListener {

	SharedPreferences sharedPreferences;
	String name;

	public CheckBoxListener(SharedPreferences sharedPreferences, String name) {
		this.sharedPreferences = sharedPreferences;
		this.name = name;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		SharedPreferences.Editor mEditor = sharedPreferences.edit();
		if (isChecked) {
			mEditor.putBoolean(name, true);
		} else {
			mEditor.putBoolean(name, false);
		}
		mEditor.commit();
	}
}