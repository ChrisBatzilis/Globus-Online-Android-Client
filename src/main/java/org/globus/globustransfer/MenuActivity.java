package org.globus.globustransfer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MenuActivity extends BaseActivity {

	private AlertDialog mLogOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		retrieveUsernameAndToken();
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("isMenuActive")) {
				logout();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

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

	public void startMonitorActivity(View view) {

		Intent mIntent = new Intent(this, MonitorActivity.class);

		if (isInternetConnectionAvailable()) {
			goToMonitor(mIntent);
		} else {
			makeOfflineToast();
		}
	}

	private void goToMonitor(Intent intent) {

		Bundle bundle = new Bundle();
		bundle.putString("username", mUsername);
		bundle.putString("authToken", mAuthToken);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void startTransferActivity(View view) {

		Intent mIntent = new Intent(this, StartTransferActivity.class);

		if (isInternetConnectionAvailable()) {
			goToTransfer(mIntent);
		} else {
			makeOfflineToast();
		}

	}

	private void goToTransfer(Intent intent) {
	
		Bundle mBundle = new Bundle();
		mBundle.putString("username", mUsername);
		mBundle.putString("authToken", mAuthToken);
		intent.putExtras(mBundle);
		startActivity(intent);
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
