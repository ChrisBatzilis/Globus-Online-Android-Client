package org.globus.globustransfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.globus.globustransfer.Client.JSONClient;

public class BaseActivity extends Activity {

	protected String mUsername;
	protected String mAuthToken;
	protected JSONClient sClient;

	protected boolean isInternetConnectionAvailable() {

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Creates a short-lived message on the screen.
	 */
	protected void makeToast(String text) {

		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	protected void makeToast(String text, int duration) {

		Toast.makeText(this, text, duration).show();
	}

	protected void makeOfflineToast() {

		Toast.makeText(this, getString(R.string.offline_warning),
				Toast.LENGTH_SHORT).show();
	}

	protected void retrieveUsernameAndToken() {
		
		Intent mIntent = getIntent();
		Bundle mInfo = mIntent.getExtras();
		mUsername = mInfo.getString("username");
		mAuthToken = mInfo.getString("authToken");
	}

	protected void createJsonClient() {
	
		try {
			sClient = new JSONClient(mUsername, mAuthToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void makeVisible(View... views) {
		
		for (View view : views) {
			view.setVisibility(View.VISIBLE);
		}
	}

	protected void makeInvisible(View... views) {
		
		for (View view : views) {
			view.setVisibility(View.INVISIBLE);
		}
	}

}
