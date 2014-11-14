package org.globus.globustransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import org.globus.globustransfer.R;

public class MenuActivity extends Activity {

	protected String mUsername;
	protected String mSamlCookie;
	private AlertDialog mLogOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		// The username and cookie are retrieved from the Intent
		Intent mIntent = getIntent();
		Bundle mInfo = mIntent.getExtras();
		mUsername = mInfo.getString("username");
		mSamlCookie = mInfo.getString("samlCookie");

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("isMenuActive"))
				logout();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Creates a Dialog asking the user if they really want to log out. If user
	 * selects "YES", the activity is terminated, if they select "NO" the dialog
	 * disappears.
	 */
	private void logout() {
		mLogOut = new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.log_out_question)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								MenuActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();

	}

	@Override
	public void onBackPressed() {
		logout();
	}

	/**
	 * It initiates an instance of the Monitor Activity, putting as extra
	 * information within a Bundle the users' credentials.
	 * 
	 * 
	 * @param view
	 *            The current view
	 */
	public void startMonitorActivity(View view) {

		// An Intent is created in order to call the Monitor Activity
		Intent mIntent = new Intent(this, MonitorActivity.class);

		// A Bundle is created in order to send information to the Monitor
		// Activity
		if (isInternetConnectionAvailable()) {
			Bundle bundle = new Bundle();
			bundle.putString("username", mUsername);
			bundle.putString("samlCookie", mSamlCookie);
			mIntent.putExtras(bundle);
			startActivity(mIntent);
		} else {
			Toast.makeText(this, getString(R.string.offline_warning),
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * It initiates an instance of the StartTransfer Activity, putting as extra
	 * information within a Bundle the users' credentials.
	 * 
	 * 
	 * @param view
	 *            The current view
	 */
	public void startTransferActivity(View view) {

		// An intent is created in order to call the StartTransfer Activity
		Intent mIntent = new Intent(this, StartTransfer.class);

		// A bundle is created in order to send information to the StartTransfer
		// Activity
		if (isInternetConnectionAvailable()) {
			Bundle mBundle = new Bundle();
			mBundle.putString("username", mUsername);
			mBundle.putString("samlCookie", mSamlCookie);
			mIntent.putExtras(mBundle);
			startActivity(mIntent);
		} else {
			Toast.makeText(this, getString(R.string.offline_warning),
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * Checks if there is Internet Connection available.
	 * 
	 * @return The Internet Connection availability
	 */
	private boolean isInternetConnectionAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mLogOut != null) {
			outState.putBoolean("isMenuActive", mLogOut.isShowing());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mLogOut != null) {
			mLogOut.dismiss();
		}

	}
}
