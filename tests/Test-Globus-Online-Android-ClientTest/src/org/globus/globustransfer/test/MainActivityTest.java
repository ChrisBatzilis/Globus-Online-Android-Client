package org.globus.globustransfer.test;

import org.globus.globustransfer.MainActivity;
import org.globus.globustransfer.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	private MainActivity mMainActivity;
	private SharedPreferences mSharedPreferences;
	private TextView mUsernameTextView;
	private TextView mPasswordTextView;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		mMainActivity = getActivity();
		mUsernameTextView = (TextView) mMainActivity
				.findViewById(R.id.username);
		mPasswordTextView = (TextView) mMainActivity
				.findViewById(R.id.password);
		mSharedPreferences = mMainActivity.getSharedPreferences(
				mMainActivity.getString(R.string.preferences_name),
				Context.MODE_PRIVATE);
	}

	public void testUsernameAndPasswordInitiallyEmpty() {
		assertEquals("", mUsernameTextView.getText().toString());
		assertEquals("", mPasswordTextView.getText().toString());
	}

	public void testSavedUsernameAndPasswordLoadedOnLogoButtonClick() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.clear();
		editor.putString("username", "johnDoe");
		editor.putString("password", "1234");
		editor.commit();
		ImageButton mLogoButton = (ImageButton) mMainActivity
				.findViewById(R.id.logo_image_button_id);
		TouchUtils.clickView(this, mLogoButton);
		assertEquals("johnDoe", mUsernameTextView.getText().toString());
		assertEquals("1234", mPasswordTextView.getText().toString());
	}
}
