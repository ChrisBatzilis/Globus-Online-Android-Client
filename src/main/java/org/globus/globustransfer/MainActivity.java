package org.globus.globustransfer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.globus.globustransfer.exceptions.SignInException;
import org.globus.globustransfer.services.SignInService;
import org.globus.globustransfer.services.SignInServiceImpl;

public class MainActivity extends BaseActivity {

	private static final String sGlobusOnlineSignUpURI = "http://www.globus.org/SignUp";
	private String mNetworkNotAvailable;
	private TextView mUsernameTextView = null;
	private TextView mPasswordTextView = null;
	private Context mContext;
	private ProgressBar mAttemptLogInProgressBar;
	private Button mLogInButton;
	private boolean mSwitch = true;
	private SharedPreferences mSharedPreferences;
	private CheckBox mRememberMeCheckBox;
	private TextView mRememberMeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mNetworkNotAvailable = getString(R.string.offline_warning);
		mAttemptLogInProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mUsernameTextView = (TextView) findViewById(R.id.username);
		mPasswordTextView = (TextView) findViewById(R.id.password);
		mSharedPreferences = getSharedPreferences(
				getString(R.string.preferences_name), MODE_PRIVATE);
		mRememberMeCheckBox = (CheckBox) findViewById(R.id.remember_me_checkbox);
		mRememberMeTextView = (TextView) findViewById(R.id.remember_me_text_view);
		mContext = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Do nothing when the menu button is pressed
		return true;
	}

	/**
	 * Checks if there is an Internet connection available. If there is, it
	 * redirects the user to Globus Online's website to sign up for a new user
	 * account.
	 * 
	 * @param view
	 *            The current view
	 */
	public void goToGlobusOnlineSignUp(View view) {
		if (isInternetConnectionAvailable()) {

			goToSignUp();
		} else {

			makeToast(mNetworkNotAvailable);
		}
	}

	private void goToSignUp() {
		Uri mUri = Uri.parse(sGlobusOnlineSignUpURI);
		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
		startActivity(mIntent);
	}

	/**
	 * Tries to Sign the user In to Globus Online Transfer Service. If there is
	 * no Internet Connection, the username field is empty or the password field
	 * is empty, the user is informed with Toast warning messages. If everything
	 * is ok it calls the execute function of the signInAttempt class.
	 * 
	 * @param view
	 */
	public void signIn(View view) {

		String username = mUsernameTextView.getText().toString();
		String password = mPasswordTextView.getText().toString();

		if (!isSigninPrerequisitesSatisfied(username, password)) {
			return;
		}
		new signInAttempt().execute(username, password);

	}

	/**
	 * Sets in the Edit Text fields the saved username and password (if any) of
	 * the user.
	 * 
	 * @param view
	 *            The current view
	 */
	public void setSavedCredentials(View view) {
		if (mSwitch) {
			writeSavedCredentialsToFields();
		} else {
			clearCredentialsFromFields();
		}
		mSwitch = !mSwitch;
	}

	private void writeSavedCredentialsToFields() {
		mUsernameTextView.setText(mSharedPreferences.getString("username", ""));
		mPasswordTextView.setText(mSharedPreferences.getString("password", ""));
	}

	private void clearCredentialsFromFields() {
		mUsernameTextView.setText("");
		mPasswordTextView.setText("");

	}

	/**
	 * Checks whether the user name and the password fields are filled and there
	 * is a connection to the Internet
	 */
	private boolean isSigninPrerequisitesSatisfied(String username,
			String password) {
		if (!isInternetConnectionAvailable()) {
			makeToast(mNetworkNotAvailable);
			return false;
		}
		if (username.length() == 0 && password.length() == 0) {
			makeToast(getString(R.string.no_username_and_password_warning));
			return false;
		}
		if (username.length() == 0) {
			makeToast(getString(R.string.no_username_warning));
			return false;
		}
		if (password.length() == 0) {
			makeToast(getString(R.string.no_password_warning));
			return false;
		}
		return true;
	}

	/**
	 * This class attempts to sign in to Globus Online by POSTing to the Globus
	 * Online Authenticate URL the username and password provided by the user
	 * through the Edit Text fields. If the user's credentials are valid,
	 * signing in succeeds and a cookie is returned from Globus Online
	 * authentication service, which along with the username will allow us to
	 * contact the API and, if the "Remember me" checkbox is selected, the
	 * username and password are stored on the devices memory. If the signing in
	 * process fails, the user is informed with a Toast message. (This class is
	 * partially based on Peter Hadlaw's corresponding GOUserLogInAttempt class)
	 * 
	 * @author christos
	 */
	private class signInAttempt extends AsyncTask<String, Void, String> {

		boolean success = true;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			hideSignInRelatedViewItems();
			mAttemptLogInProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... credentials) {
			SignInService signInService = new SignInServiceImpl();

			try {
				return signInService.signIn(credentials[0], credentials[1]);
			} catch (SignInException e) {
				success = false;
				return getString(R.string.login_fail_warning);
			}

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (success) {

				goToMenu(result);
			} else {

				makeToast(result);
			}

			blendSignInRelatedViewItems();
			mAttemptLogInProgressBar.setVisibility(View.GONE);

		}

		private void goToMenu(String authToken) {
			Intent intent = new Intent(mContext, MenuActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString("username", mUsernameTextView.getText().toString());
			bundle.putString("authToken", authToken);
			intent.putExtras(bundle);
			if (mRememberMeCheckBox.isChecked()) {

				saveCredentials();
			}
			startActivity(intent);

		}

		private void saveCredentials() {
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString("username", mUsernameTextView.getText().toString());
			editor.putString("password", mPasswordTextView.getText().toString());
			editor.commit();

		}

		private void hideSignInRelatedViewItems() {
			mLogInButton = (Button) findViewById(R.id.log_in_button);
			makeInvisible(mUsernameTextView, mPasswordTextView, mLogInButton,
					mRememberMeCheckBox, mRememberMeTextView);
			

		}

		private void blendSignInRelatedViewItems() {
			makeVisible(mUsernameTextView, mPasswordTextView, mLogInButton,
					mRememberMeCheckBox, mRememberMeTextView);
	
		}

	}

}
