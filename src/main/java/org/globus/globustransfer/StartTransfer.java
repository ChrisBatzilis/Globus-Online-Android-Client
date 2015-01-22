package org.globus.globustransfer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.globus.globustransfer.Client.JSONClient;
import org.globus.globustransfer.Client.JSONClient.Result;
import org.globus.globustransfer.services.RateService;
import org.globus.globustransfer.services.RateServiceImpl;
import org.globusonline.transfer.APIError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StartTransfer extends BaseActivity {

	private static long sInterval = 900;
	private static JSONClient sClient;
	private Context mContext;
	private String mEndpointACurrentPath = "/";
	private String mEndpointBCurrentPath = "/";
	private ProgressBar mEndpointLoadProgressBar;
	private ProgressBar mEndpointAProgressBar, mEndpointBProgressBar;
	private Dialog mSettingsDialog, mCreateFolderDialog, mDeleteFilesDialog,
			mMakeTransferDialog;
	private ToggleButton mEndpointsVisibleToggleButton, mHiddenToggleButton;
	private Button mCreateFolderOkButton, mDeleteFilesOkButton,
			mMakeTransferOkButton, mSettingOkButton;
	private Button mSelectEndpointAButton, mSelectEndpointBButton;
	private Button mDeleteAButton, mDeleteBButton;
	private Button mUpAButton, mUpBButton;
	private Button mTransferAButton, mTransferBButton;
	private Button mCreateAButton, mCreateBButton;
	private Button mRefreshAButton, mRefreshBButton;
	private ArrayList<String> mEndpoints;
	private ArrayList<String> mFileListA, mFileListB;
	private SharedPreferences mSharedPreferences;
	private ListView mAListView, mBListView;
	private TextView mATextView, mBTextView;
	private Button mPathAButton, mPathButton;
	private List<String> mSelectedFilesA, mSelectedFilesB;
	private EditText mCreateFolderEditText, mDeleteFilesEditText,
			mMakeTransferEditText;
	private String mFolderName, mTransferName, mDeletionName, mNode,
			mActivateNode, mActivateEndpoint;
	private Map<String, Long> mLastTimePressedA, mLastTimePressedB;
	private Map<String, Float> mFilesSizeA, mFilesSizeB;
	private Boolean mEndpointsTemp, mHiddenTemp;
	private JSONObject mActivationRequirements;
	private Dialog mActivateDialog;
	private RateService mRateService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_transfer);
		retrieveUsernameAndToken();
		initializeVariables();
		initializeView();
		initializeSettingsDialog();
		createJsonClient();
		retrieveData(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.start_transfer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.refresh_menu_item:

			new GetEndpointsList().execute();
			return true;
		case R.id.transfer_settings_menu_item:

			loadSettingsMenu();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadSettingsMenu() {

		mEndpointsTemp = mSharedPreferences.getBoolean("EndpointsActiveOnly",
				true);
		mHiddenTemp = mSharedPreferences.getBoolean("ShowHiddenFiles", false);
		mSettingsDialog.show();
	}

	/***
	 * Function which is called when an endpoint is selected from the list. The
	 * path is set to "/" , the endpoint's name is set on the corresponding text
	 * field and if some files on the other endpoint are selected, the
	 * corresponding transfer button is activated.
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (newEndpointIsSelected(data)) {
			if (isInListA(data)) {
				if (!mSelectedFilesB.isEmpty()) {
					enableTransferBToA();
				}
				fillFileListA(data);

			} else if (isInListB(data)) {
				if (!mSelectedFilesA.isEmpty()) {
					enableTransferAtoB();

				}
				fillFileListB(data);
			}

		} else if (data.getExtras().containsKey("stop")) {

		}
	}

	private boolean newEndpointIsSelected(Intent data) {
		return data.getExtras().containsKey("endpoint")
				&& data.getExtras().containsKey("endId");
	}

	private boolean isInListA(Intent data) {

		return data.getStringExtra("endId").contentEquals("a");
	}

	private void enableTransferBToA() {

		mTransferBButton.setBackgroundResource(R.drawable.transfer_up_active);

	}

	private void fillFileListA(Intent data) {

		mSelectEndpointAButton.setText(data.getStringExtra("endpoint"));
		mEndpointACurrentPath = "/";
		mPathAButton.setText(getString(R.string.path_placeholder));
		new GetEndpointsDirectory("a", data.getStringExtra("endpoint"))
				.execute();
	}

	private boolean isInListB(Intent data) {

		return data.getStringExtra("endId").contentEquals("b");
	}

	private void enableTransferAtoB() {

		mTransferAButton.setBackgroundResource(R.drawable.transfer_down_active);

	}

	private void fillFileListB(Intent data) {

		mSelectEndpointBButton.setText(data.getStringExtra("endpoint"));
		mEndpointBCurrentPath = "/";
		mPathButton.setText(getString(R.string.path_placeholder));
		new GetEndpointsDirectory("b", data.getStringExtra("endpoint"))
				.execute();

	}

	/**
	 * On click listener for List View A. If one file or folder is pressed its
	 * background color changes to blue if it was white before and it is added
	 * to the Selected Files list. If it was already on the list its background
	 * color turns white and it is deleted from the list. If a folder is pressed
	 * twice in less than 0.9 seconds the path is set to that folder.
	 */
	private OnItemClickListener mMessageClickedHandlerA = new OnItemClickListener() {

		public void onItemClick(AdapterView parent, View v, int position,
				long id) {

			long mPressTime = System.currentTimeMillis();
			long mTimeDifference;
			TextView mFileNameTextView = (TextView) v
					.findViewById(R.id.file_name_in_list_text_view);

			String mFilename = mFileNameTextView.getText().toString();

			if (mLastTimePressedA.containsKey(mFilename)) {
				mTimeDifference = mPressTime - mLastTimePressedA.get(mFilename);
			} else {
				mTimeDifference = sInterval + 1;
			}

			if (mTimeDifference <= sInterval && mFilename.endsWith("/")) {

				updateDirectoryA(mFilename);

			} else {

				if (!mSelectedFilesA.contains(mFilename)) {
					selectFileA(v, mFilename);

				} else {
					unselectFileA(v, mFilename);
				}
			}

			mLastTimePressedA.put(mFilename, mPressTime);
		}

		private void selectFileA(View v, String mFilename) {

			v.setBackgroundColor(getResources().getColor(
					R.color.android_light_blue));
			mSelectedFilesA.add(mFilename);
			if (mSelectEndpointBButton.getText().toString() != getString(R.string.select_endpoint_prompt))
				mTransferAButton
						.setBackgroundResource((R.drawable.transfer_down_active));

		}

		private void unselectFileA(View v, String mFilename) {

			v.setBackgroundColor(Color.WHITE);
			mSelectedFilesA.remove(mFilename);
			if (mSelectedFilesA.isEmpty()) {
				mTransferAButton
						.setBackgroundResource(R.drawable.transfer_down_inactive);
			}

		}

		private void updateDirectoryA(String mFilename) {

			if (isInternetConnectionAvailable()) {
				mEndpointACurrentPath = mEndpointACurrentPath.concat(mFilename);
				new GetEndpointsDirectory("a", mSelectEndpointAButton.getText()
						.toString()).execute();
			} else {
				makeToast(getString(R.string.offline_warning),
						Toast.LENGTH_SHORT);
			}
		}
	};

	/**
	 * On click listener for List View Β. If one file or folder is pressed its
	 * background color changes to blue if it was white before and it is added
	 * to the Selected Files list. If it was already on the list its background
	 * color turns white and it is deleted from the list. If a folder is pressed
	 * twice in less than 0.9 seconds the path is set to that folder.
	 */
	private OnItemClickListener mMessageClickedHandlerB = new OnItemClickListener() {

		public void onItemClick(AdapterView parent, View v, int position,
				long id) {
			long mPressTime = System.currentTimeMillis();
			long mTimeDifference;
			TextView mFilenameTextView = (TextView) v
					.findViewById(R.id.file_name_in_list_text_view);

			String mFilename = mFilenameTextView.getText().toString();

			if (mLastTimePressedB.containsKey(mFilename)) {
				mTimeDifference = mPressTime - mLastTimePressedB.get(mFilename);
			} else {
				mTimeDifference = sInterval + 1;
			}
			if (mTimeDifference <= sInterval && mFilename.endsWith("/")) {
				updateDirectoryB(mFilename);

			} else {
				if (!mSelectedFilesB.contains(mFilename)) {
					selectFileB(v, mFilename);

				} else {
					unselectFileB(v, mFilename);

				}
			}
			mLastTimePressedB.put(mFilename, mPressTime);

		}

		private void selectFileB(View v, String mFilename) {
			v.setBackgroundColor(getResources().getColor(
					R.color.android_light_blue));

			mSelectedFilesB.add(mFilename);
			if (mSelectEndpointAButton.getText().toString() != getString(R.string.select_endpoint_prompt))
				mTransferBButton
						.setBackgroundResource((R.drawable.transfer_up_active));

		}

		private void unselectFileB(View v, String mFilename) {
			v.setBackgroundColor(Color.WHITE);
			mSelectedFilesB.remove(mFilename);
			if (mSelectedFilesB.isEmpty()) {
				mTransferBButton
						.setBackgroundResource(R.drawable.transfer_up_inactive);
			}

		}

		private void updateDirectoryB(String mFilename) {
			mEndpointBCurrentPath = mEndpointBCurrentPath.concat(mFilename);
			new GetEndpointsDirectory("b", mSelectEndpointBButton.getText()
					.toString()).execute();

		}
	};

	/**
	 * Goes up one folder level. If the current path is the root path it
	 * notifies the user with a Toast that they can not go further up.
	 * 
	 * @param view
	 *            The ListView (representing an endpoint's directory) we want to
	 *            act on
	 */
	public void goBack(View view) {

		int mIdButton = ((Button) view).getId();
		if (mIdButton == R.id.back_A_button) {
			attemptGoBack(mSelectEndpointAButton, mEndpointACurrentPath, "a");
		} else if (mIdButton == R.id.back_B_button) {
			attemptGoBack(mSelectEndpointBButton, mEndpointBCurrentPath, "b");
		}
	}

	/**
	 * Creates a Deletion Dialog asking from the user for a label for deleting
	 * the files selected in the corresponding View (endpoint). It gives
	 * appropriate error messages with Toasts when there is no endpoint selected
	 * in the View or if no files are selected.If the user clicks on the "OK"
	 * Button of the Dialog -having typed a valid label or no label at all- it
	 * calls the execute method of the FileDeletion class.
	 * 
	 * @param view
	 *            The ListView (representing an endpoint's directory) we want to
	 *            act on
	 */
	public void deleteFiles(final View view) {

		if (deletionNotPossible(view)) {
			return;
		}
		initializeDeleteDialog();

	}

	public void setPath(final View view) {
		String node = null;

		if (setPathNotPossible(view)) {
			return;
		}

		if (view.getId() == mPathAButton.getId()) {
			node = "a";
		} else if (view.getId() == mPathButton.getId()) {
			node = "b";
		}

		initializeSetPathDialog(node);
	}

	private boolean setPathNotPossible(View view) {
		Button endpointSelectionButton = null;
		if (view.getId() == mPathAButton.getId()) {
			endpointSelectionButton = mSelectEndpointAButton;
		} else {
			endpointSelectionButton = mSelectEndpointBButton;
		}

		if (endpointSelectionButton.getText().toString()
				.contentEquals(getString(R.string.select_endpoint_prompt))) {
			makeToast(getString(R.string.no_endpoint_selected_warning),
					Toast.LENGTH_SHORT);
			return true;
		}
		return false;
	}

	private boolean deletionNotPossible(View view) {

		if (view.getId() == mDeleteAButton.getId()) {
			{
				mNode = "a";
				if (deletionNotPossible(mSelectedFilesA, mSelectEndpointAButton)) {
					return true;
				}

			}

		}

		else if (view.getId() == mDeleteBButton.getId()) {
			mNode = "b";
			if (deletionNotPossible(mSelectedFilesB, mSelectEndpointBButton)) {
				return true;
			}

		}
		if (!isInternetConnectionAvailable()) {

			makeToast(getString(R.string.offline_warning), Toast.LENGTH_SHORT);
			return true;
		}
		return false;
	}

	private boolean deletionNotPossible(List<String> selectedFiles,
			Button selectEndpointButton) {

		if (selectEndpointButton.getText().toString()
				.contentEquals(getString(R.string.select_endpoint_prompt))) {
			makeToast(getString(R.string.no_endpoint_selected_warning),
					Toast.LENGTH_SHORT);
			return true;
		}

		if (selectedFiles.isEmpty()) {
			makeToast(getString(R.string.no_files_selected_warning),
					Toast.LENGTH_SHORT);
			return true;
		}
		return false;
	}

	private void initializeDeleteDialog() {

		mDeleteFilesDialog = new Dialog(this);
		mDeleteFilesDialog.setCanceledOnTouchOutside(false);
		mDeleteFilesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDeleteFilesDialog.setContentView(R.layout.select_deletion_name);
		mDeleteFilesEditText = (EditText) mDeleteFilesDialog
				.findViewById(R.id.deletion_name_edit_text);
		mDeleteFilesOkButton = (Button) mDeleteFilesDialog
				.findViewById(R.id.ok_button);

		mDeleteFilesDialog.show();

		mDeleteFilesOkButton.setOnClickListener(new OnClickListener()

		{

			@Override
			public void onClick(View arg0) {

				mDeletionName = mDeleteFilesEditText.getText().toString();

				if (!mDeletionName.matches("[A-Z[a-z][0-9][ ]]*")) {
					makeToast(
							getString(R.string.no_escape_characters_allowed_warning),
							Toast.LENGTH_SHORT);
				} else if (!isInternetConnectionAvailable()) {
					makeToast(getString(R.string.offline_warning),
							Toast.LENGTH_SHORT);
				} else {
					mDeleteFilesDialog.dismiss();
					mDeleteFilesEditText.setText("");
					new FileDeletion(mNode, mDeletionName).execute();
				}
			}

		});
	}

	private void attemptGoBack(Button selectEndpointButton,
			String endpointCurrentPath, String node) {

		String mNoEndpoint = getString(R.string.select_endpoint_prompt);
		if (selectEndpointButton.getText() == mNoEndpoint) {
			makeToast(getString(R.string.no_endpoint_selected_warning),
					Toast.LENGTH_SHORT);
		} else if (!isInternetConnectionAvailable()) {
			makeToast(getString(R.string.offline_warning), Toast.LENGTH_SHORT);
		} else {

			if (!endpointCurrentPath.contentEquals("/")) {
				endpointCurrentPath = backPath(endpointCurrentPath);
				if (node.contentEquals("a")) {
					mEndpointACurrentPath = endpointCurrentPath;
				} else if (node.contentEquals("b")) {
					mEndpointBCurrentPath = endpointCurrentPath;
				}

				new GetEndpointsDirectory(node, selectEndpointButton.getText()
						.toString()).execute();
			} else {

				String text = getString(R.string.root_folder_warning)
						.toString();
				makeToast(text, Toast.LENGTH_SHORT);
			}
		}

	}

	/**
	 * Creates a folder creation Dialog asking from the user for the name of the
	 * folder to be created in the corresponding view (endpoint). It gives
	 * appropriate error messages with Toasts when there in no endpoint selected
	 * in the view. If the user clicks on the "OK" button of the dialog -having
	 * typed a valid folder name- it calls the execute method of the
	 * directoryCreation class.
	 * 
	 * @param view
	 *            The ListView (representing an endpoint's directory) we want to
	 *            act on
	 */
	public void createNewFolder(View view) {

		int mIdButton = ((Button) view).getId();
		Button selectEndpointButton = null;
		if (mIdButton == R.id.create_folder_A_button) {

			mNode = "a";
			selectEndpointButton = mSelectEndpointAButton;
		} else if (mIdButton == R.id.create_folder_B_button) {

			mNode = "b";
			selectEndpointButton = mSelectEndpointBButton;
		}

		if (folderCreationNotPossible(selectEndpointButton)) {
			return;
		}

		initializeCreateFolderDialog();

	}

	private void initializeCreateFolderDialog() {
		mCreateFolderDialog = new Dialog(this);

		mCreateFolderDialog.setCanceledOnTouchOutside(false);
		mCreateFolderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mCreateFolderDialog.setContentView(R.layout.select_folder_name);
		mCreateFolderEditText = (EditText) mCreateFolderDialog
				.findViewById(R.id.folder_name_edit_text);
		mCreateFolderOkButton = (Button) mCreateFolderDialog
				.findViewById(R.id.ok_button);
		mCreateFolderDialog.show();

		mCreateFolderOkButton.setOnClickListener(new OnClickListener()

		{

			@Override
			public void onClick(View arg0) {

				mFolderName = mCreateFolderEditText.getText().toString();
				if (mFolderName.isEmpty()) {
					makeToast(getString(R.string.no_folder_title_warning),
							Toast.LENGTH_SHORT);
				}

				else if (!mFolderName.matches("[A-Z[a-z][0-9][ ]]+")) {
					makeToast(
							getString(R.string.no_escape_characters_allowed_warning),
							Toast.LENGTH_SHORT);
				} else {

					mCreateFolderDialog.dismiss();
					new DirectoryCreation(mNode, mFolderName).execute();
				}
			}

		});

	}

	private boolean folderCreationNotPossible(Button selectEndpointButton) {
		String mNoEndpoint = getString(R.string.select_endpoint_prompt);
		if (selectEndpointButton.getText() == mNoEndpoint) {
			makeToast(getString(R.string.no_endpoint_selected_warning),
					Toast.LENGTH_SHORT);
			return true;
		}

		if (!isInternetConnectionAvailable()) {
			makeToast(getString(R.string.offline_warning), Toast.LENGTH_SHORT);
			return true;
		}
		return false;
	}

	/**
	 * It initiates with an Intent the Endpoint Activity, waiting to get back a
	 * result with the name of the Enpoint selected by the user.
	 * 
	 * @param view
	 *            The Endpoint Selection Button that was clicked
	 */
	public void startEndpointList(View view) {
		String mEndpoint = null;

		if (view.getId() == mSelectEndpointAButton.getId())
			mEndpoint = "a";
		else if (view.getId() == mSelectEndpointBButton.getId())
			mEndpoint = "b";

		Intent mIntent = getEndpointsListIntent(mEndpoint);
		goToEndpointsList(mIntent);

	}

	private Intent getEndpointsListIntent(String endpoint) {
		Intent intent = new Intent(mContext, EndpointActivity.class);
		intent.putStringArrayListExtra("endpointslist", mEndpoints);
		intent.putExtra("endId", endpoint);
		return intent;

	}

	private void goToEndpointsList(Intent intent) {
		try {

			startActivityForResult(intent, 1);
		} catch (Exception e) {
			makeToast(e.getMessage());
		}

	}

	/**
	 * Returns the path of the folder being one level up from the current
	 * folder.
	 * 
	 * @param path
	 *            The current path
	 * @return The path of the folder one level up
	 */
	public String backPath(String path) {
		String mDelims = "/";
		String[] mTokens = path.split(mDelims);
		String mResult = "/";
		int mTemp;
		if (path.endsWith("/"))
			mTemp = 1;
		else
			mTemp = 2;
		for (int i = 0; i < mTokens.length; i++) {

			if (i > 0 && i < (mTokens.length - mTemp)) {
				mResult = mResult.concat(mTokens[i] + "/");
			}
		}

		return mResult;

	}

	/**
	 * It refreshes the directory contents of the ListView selected.
	 * 
	 * @param view
	 *            The ListView (representing an endpoint's directory) we want to
	 *            act on
	 */
	public void refresh(View view) {
		if (!isInternetConnectionAvailable()) {
			makeToast(getString(R.string.offline_warning), Toast.LENGTH_SHORT);
			return;
		}
		if (view.getId() == R.id.refresh_A_button) {

			refreshDirectory(mSelectEndpointAButton, mAListView,
					mEndpointAProgressBar, "a");
		} else if (view.getId() == R.id.refresh_B_button) {

			refreshDirectory(mSelectEndpointBButton, mBListView,
					mEndpointBProgressBar, "b");
		}

	}

	private void refreshDirectory(Button selectEndpointButton,
			ListView listView, ProgressBar endpointProgressBar, String node) {
		if (!selectEndpointButton.getText().toString()
				.contentEquals(getString(R.string.select_endpoint_prompt))) {
			listView.setAdapter(null);
			listView.setVisibility(View.INVISIBLE);
			endpointProgressBar.setVisibility(View.VISIBLE);
			new GetEndpointsDirectory(node, selectEndpointButton.getText()
					.toString()).execute();
		} else {
			makeToast(getString(R.string.no_endpoint_selected_warning),
					Toast.LENGTH_SHORT);
		}

	}

	/**
	 * Creates a dialog asking from the user to type a label for the transfer to
	 * be initiated. If the source or the destination Endpoints have not been
	 * selected, or if the are no files selected in the source Endpoint
	 * directory List View, it warns the user with Toasts containing the
	 * corresponding warning messages. If the the user types a valid label or no
	 * label at all and clicks the "OK" button in the dialog, the execute method
	 * of the fileTransfer class is called.
	 * 
	 * @param view
	 *            The Transfer Button clicked
	 */
	public void makeTransfer(View view) {

		if (transferNotPossible(view)) {
			return;
		}
		initializeMakeTransferDialog();

	}

	private boolean transferNotPossible(View view) {
		int mIdButton = ((Button) view).getId();
		String mNoEndpoint = getString(R.string.select_endpoint_prompt);

		if (mSelectEndpointBButton.getText() == mNoEndpoint
				|| mSelectEndpointAButton.getText() == mNoEndpoint) {

			return true;
		}
		if (!isInternetConnectionAvailable()) {
			makeToast(getString(R.string.offline_warning), Toast.LENGTH_SHORT);
			return true;
		}
		if (mIdButton == R.id.transfer_A_to_B_button) {
			mNode = "a";
			if (mSelectedFilesA.isEmpty()) {

				return true;
			}
		} else if (mIdButton == R.id.transfer_B_to_A_button) {
			mNode = "b";
			if (mSelectedFilesB.isEmpty()) {

				return true;
			}
		}
		return false;
	}

	private void initializeMakeTransferDialog() {

		mMakeTransferDialog = new Dialog(this);
		mMakeTransferDialog.setCanceledOnTouchOutside(false);
		mMakeTransferDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mMakeTransferDialog.setContentView(R.layout.make_transfer_dialog);
		mMakeTransferEditText = (EditText) mMakeTransferDialog
				.findViewById(R.id.transfer_name_edit_text);
		mMakeTransferOkButton = (Button) mMakeTransferDialog
				.findViewById(R.id.ok_button);
		mMakeTransferDialog.show();
		mMakeTransferOkButton.setOnClickListener(new OnClickListener()

		{

			@Override
			public void onClick(View arg0) {

				mTransferName = mMakeTransferEditText.getText().toString();

				if (!mTransferName.matches("[A-Z[a-z][0-9][ ]]*")) {
					makeToast(
							getString(R.string.no_escape_characters_allowed_warning),
							Toast.LENGTH_SHORT);
				} else if (!isInternetConnectionAvailable()) {
					makeToast(getString(R.string.offline_warning),
							Toast.LENGTH_SHORT);
				} else {
					mMakeTransferDialog.dismiss();
					mMakeTransferEditText.setText("");
					new FilesTransfer(mNode, mTransferName).execute();
				}
			}

		});

	}

	/**
	 * 
	 * This adapter is a custom adapter used for populating the List View
	 * representing the endpoints' directories. In every row of the List it puts
	 * the name of the file/folder, along with its appropriate icon based on
	 * whether it is a file or a folder, its size if it is a file or the word
	 * "Folder" if it is a folder, as well as paints the background light blue
	 * or white, depending on whether the specific file or folder is selected by
	 * the user or not.
	 * 
	 * @author christos
	 * 
	 */
	public class CustomAdapter extends ArrayAdapter<String> {

		private Context mLocalContext;

		public CustomAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			this.mLocalContext = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String mFile = (String) this.getItem(position);
			View mRow = convertView;

			if (mRow == null) {
				LayoutInflater vi = (LayoutInflater) mLocalContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mRow = vi.inflate(R.layout.directory_list_view, null);
			}

			TextView mFilenameTextView = (TextView) mRow
					.findViewById(R.id.file_name_in_list_text_view);
			ImageView mFileTypeImageView = (ImageView) mRow
					.findViewById(R.id.file_type_in_list);
			TextView mFileSize = (TextView) mRow
					.findViewById(R.id.file_size_in_text_view);

			if (mFile.endsWith("/")) {
				mFileTypeImageView.setImageResource(R.drawable.folder);
				mFileSize.setText(getString(R.string.folder));
			} else {
				mFileTypeImageView.setImageResource(R.drawable.file);
				float mTemp = 0;

				// The file's size is retrieved
				if (parent.getId() == R.id.endpoint_A_list_view) {
					if (mFilesSizeA.containsKey(mFile))
						mTemp = mFilesSizeA.get(mFile);
				} else if (parent.getId() == R.id.endpoint_B_list_view) {
					if (mFilesSizeB.containsKey(mFile))
						mTemp = mFilesSizeB.get(mFile);
				}

				// The file's size is converted to the appropriate unit of size
				String mSizeString = mRateService.sizeFromBytes(mTemp);

				// The file's size is set
				mFileSize.setText(mSizeString);

			}

			// The file/folder's background color is set to either light blue or
			// white, depending on whether it is selected or not
			if (parent.getId() == R.id.endpoint_A_list_view) {
				if (mSelectedFilesA.contains(mFile)) {
					mRow.setBackgroundColor(getResources().getColor(
							R.color.android_light_blue));
				} else {
					mRow.setBackgroundColor(Color.WHITE);
				}
			} else if (parent.getId() == R.id.endpoint_B_list_view) {
				if (mSelectedFilesB.contains(mFile)) {
					mRow.setBackgroundColor(getResources().getColor(
							R.color.android_light_blue));
				} else {
					mRow.setBackgroundColor(Color.WHITE);

				}

			}

			// The file's name text view is set
			mFilenameTextView.setText(mFile);
			return mRow;
		}

	}

	/**
	 * This class attempts to create a new directory by POSTing a JSON object
	 * containing the new folder's name to endpoint/<canonical_name>/mkdir. It
	 * informs the user with a Toast for the request's result, and if it's
	 * successful it refreshes the corresponding endpoint's directory.
	 * 
	 * @author christos
	 * 
	 */
	public class DirectoryCreation extends AsyncTask<String, Void, String> {

		String mLocalNode, mLocalfolderName;

		public DirectoryCreation(String node, String folderName) {
			this.mLocalNode = node;
			this.mLocalfolderName = folderName;

		}

		@Override
		protected String doInBackground(String... args) {
			String mTargetPath = "";
			String mEndpoint = "";

			if (mLocalNode.contentEquals("a")) {

				mTargetPath = mEndpointACurrentPath;
				mEndpoint = mSelectEndpointAButton.getText().toString();
			} else if (mLocalNode.contentEquals("b")) {

				mTargetPath = mEndpointBCurrentPath;
				mEndpoint = mSelectEndpointBButton.getText().toString();
			}

			return createFolder(mTargetPath, mEndpoint);
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			makeToast(result, Toast.LENGTH_LONG);

			if (result.contentEquals("The directory was created successfully")) {
				if (mLocalNode.contentEquals("a")) {
					mAListView.setAdapter(null);
					mAListView.setVisibility(View.INVISIBLE);
					mEndpointAProgressBar.setVisibility(View.VISIBLE);
					new GetEndpointsDirectory(mLocalNode,
							mSelectEndpointAButton.getText().toString())
							.execute();
				} else if (mLocalNode.contentEquals("b")) {
					mBListView.setAdapter(null);
					mBListView.setVisibility(View.INVISIBLE);
					mEndpointBProgressBar.setVisibility(View.VISIBLE);
					new GetEndpointsDirectory(mLocalNode,
							mSelectEndpointBButton.getText().toString())
							.execute();

				}

			}

		}

		private String attemptToCreateFolder(String mTargetPath,
				String mEndpoint) throws JSONException, MalformedURLException,
				IOException, GeneralSecurityException, APIError {
			String mResult = getString(R.string.error);
			String mDelims = "#";
			String[] tokens = mEndpoint.split(mDelims);
			Result mQueryResult;
			JSONObject mSubmit = new JSONObject();
			mSubmit.put("path", mTargetPath + mLocalfolderName);
			mSubmit.put("DATA_TYPE", "mkdir");

			mQueryResult = sClient.postResult("/endpoint/" + tokens[0] + "%23"
					+ tokens[1] + "/mkdir", mSubmit);
			JSONObject mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("mkdir_result")) {
				return mResult;
			}

			mResult = mJsonObject.getString("message");
			return mResult;

		}

		private String createFolder(String mTargetPath, String mEndpoint) {
			String mResult;
			try {

				mResult = attemptToCreateFolder(mTargetPath, mEndpoint);
			} catch (IOException e) {
				mResult = getString(R.string.offline_warning);
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mResult = e.getMessage();
			} catch (APIError e) {
				e.printStackTrace();
				mResult = e.statusMessage;
			}
			return mResult;

		}

	}

	/**
	 * This class tries to delete the files selected by the user. It first gets
	 * a submission id by sending GET /submission_id. Once the submission id is
	 * acquired, it POSTs a JSON Object to /delete, containing the submission
	 * id, the deletion task's name, the endpoint's name, an JSONArray
	 * containing the paths of the files/folders that are to be deleted and a
	 * flag indicating whether there should be recursive file deletion or not.
	 * It informs the user for the request's result with a Toast.
	 * 
	 * @author christos
	 */
	public class FileDeletion extends AsyncTask<String, Void, List<String>> {

		String mLocalNode;
		String mLocalDeletionName;

		public FileDeletion(String node, String deletionName) {
			this.mLocalNode = node;
			this.mLocalDeletionName = deletionName;
		}

		@Override
		protected List<String> doInBackground(String... args) {

			return deleteFile();
		}

		@Override
		protected void onPostExecute(List<String> result) {

			super.onPostExecute(result);
			makeToast(result.get(0), Toast.LENGTH_LONG);
		}

		private List<String> deleteFile() {
			List<String> mMessage = new ArrayList<String>();

			try {

				mMessage = attemptDeleteFile();
			} catch (IOException e) {
				e.printStackTrace();
				mMessage.add(getString(R.string.offline_warning));
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mMessage.add(e.getMessage());
			} catch (APIError e) {
				e.printStackTrace();
				mMessage.add(e.statusMessage);
			}

			return mMessage;
		}

		private List<String> attemptDeleteFile() throws MalformedURLException,
				IOException, GeneralSecurityException, JSONException, APIError {
			String mId;
			List<String> mMessage = new ArrayList<String>();
			boolean mContainsFolders = false;
			List<String> mTargetPath = new ArrayList<String>();
			String mEndpoint = null;
			Result mQueryResult;
			JSONArray mJArray;
			JSONObject mSubmit;

			mQueryResult = sClient.getResult("/submission_id");
			JSONObject mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("submission_id")) {
				mMessage.add(getString(R.string.error));
				return mMessage;
			}

			mId = mJsonObject.getString("value");
			mContainsFolders = isContainFolders();
			mTargetPath = getTargetPath();
			mEndpoint = getEndpoint();
			mJArray = getJSONArray(mTargetPath);
			mSubmit = getJSONObject(mId, mEndpoint, mContainsFolders, mJArray);
			return sendDeletRequest(mSubmit);
		}

		private boolean isContainFolders() {
			if (mLocalNode.contentEquals("a")) {
				for (int i = 0; i < mSelectedFilesA.size(); i++) {
					if (mSelectedFilesA.get(i).endsWith("/"))
						return true;
				}
			} else if (mLocalNode.contentEquals("b")) {
				for (int i = 0; i < mSelectedFilesB.size(); i++) {
					if (mSelectedFilesB.get(i).endsWith("/"))
						return true;
				}
			}
			return false;
		}

		private List<String> getTargetPath() {
			List<String> mTargetPath = new ArrayList<>();
			if (mLocalNode.contentEquals("a")) {
				for (int i = 0; i < mSelectedFilesA.size(); i++) {

					mTargetPath.add(mEndpointACurrentPath
							.concat(mSelectedFilesA.get(i)));
				}
			} else if (mLocalNode.contentEquals("b")) {
				for (int i = 0; i < mSelectedFilesB.size(); i++) {
					mTargetPath.add(mEndpointBCurrentPath
							.concat(mSelectedFilesB.get(i)));
				}

			}
			return mTargetPath;
		}

		private String getEndpoint() {
			String mEndpoint = null;
			if (mLocalNode.contentEquals("a")) {

				mEndpoint = mSelectEndpointAButton.getText().toString();
			} else if (mLocalNode.contentEquals("b")) {

				mEndpoint = mSelectEndpointBButton.getText().toString();
			}
			return mEndpoint;
		}

		private JSONArray getJSONArray(List<String> mTargetPath)
				throws JSONException {
			JSONArray mJArray = new JSONArray();
			for (int i = 0; i < mTargetPath.size(); i++) {
				JSONObject mTemp = new JSONObject();
				mTemp.put("path", mTargetPath.get(i));
				mTemp.put("DATA_TYPE", "delete_item");
				mJArray.put(mTemp);
			}
			return mJArray;
		}

		private JSONObject getJSONObject(String mId, String mEndpoint,
				boolean mContainsFolders, JSONArray mJArray)
				throws JSONException {
			JSONObject mSubmit = new JSONObject();
			mSubmit.put("submission_id", mId);
			mSubmit.put("endpoint", mEndpoint);
			mSubmit.put("recursive", mContainsFolders);

			if (!mLocalDeletionName.matches("[ ]*")) {
				mSubmit.put("label", mLocalDeletionName);
			}

			mSubmit.put("DATA_TYPE", "delete");
			mSubmit.put("ingore_missing", false);
			mSubmit.put("DATA", mJArray);
			return mSubmit;
		}

		private List<String> sendDeletRequest(JSONObject mSubmit)
				throws JSONException, MalformedURLException, IOException,
				GeneralSecurityException, APIError {

			Result mResult;
			List<String> mMessage = new ArrayList();
			mResult = sClient.postResult("delete", mSubmit);
			JSONObject mJsonObject2 = mResult.document;
			mMessage.add(mJsonObject2.getString("message"));
			mMessage.add(mJsonObject2.getString("code"));
			return mMessage;
		}

	}

	/**
	 * This class attempts to get the contents of a specific endpoint's
	 * directory by sending a GET request to endpoint/<canonical_name>/ls along
	 * with request parameters containing the path we want to access. It makes
	 * the corresponding ListView invisible and puts a progress bar on its place
	 * while communicating with the Server. If it gets the directory's contents
	 * successfully it presents them through the corresponding List View.
	 * Otherwise it informs the user for any error that may have occurred, and
	 * if the case is that the Endpoint is not activated, it calls the execute
	 * function of the AutoActivate class.
	 * 
	 * 
	 * @author christos
	 * 
	 */
	public class GetEndpointsDirectory extends
			AsyncTask<String, Void, ArrayList<String>> {

		String mLocalNode;
		String mLocalEndpoint;

		public GetEndpointsDirectory(String node, String endpoint) {
			this.mLocalNode = node;
			this.mLocalEndpoint = endpoint;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			if (mLocalNode.contentEquals("a")) {

				prepareAForRefresh();
			} else if (mLocalNode.contentEquals("b")) {

				prepareBForRefresh();
			}
		}

		private void prepareAForRefresh() {
			mAListView.setAdapter(null);
			mTransferAButton
					.setBackgroundResource(R.drawable.transfer_down_inactive);
			mPathAButton.setText(mEndpointACurrentPath);
			mAListView.setVisibility(View.INVISIBLE);
			mEndpointAProgressBar.setVisibility(View.VISIBLE);
			mLastTimePressedA.clear();
			mFilesSizeA.clear();

		}

		private void prepareBForRefresh() {
			mBListView.setAdapter(null);
			mTransferBButton
					.setBackgroundResource(R.drawable.transfer_up_inactive);
			mPathButton.setText(mEndpointBCurrentPath);
			mBListView.setVisibility(View.INVISIBLE);
			mEndpointBProgressBar.setVisibility(View.VISIBLE);
			mLastTimePressedB.clear();
			mFilesSizeB.clear();

		}

		@Override
		protected ArrayList<String> doInBackground(String... args) {

			ArrayList<String> mDirectoryList = new ArrayList<String>();
			Map<String, String> mRequestParams = new HashMap<String, String>();

			getCurrentPath(mRequestParams);
			getShowHiddenFiles(mRequestParams);
			return getEndpointsDirectory(mRequestParams, mDirectoryList);

		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {

			super.onPostExecute(result);

			ArrayAdapter adapter = new CustomAdapter(mContext,
					android.R.layout.simple_list_item_1, result);
			String mCode = "";
			if (result.size() > 0) {
				mCode = result.get(0);
			}

			if (mLocalNode.contentEquals("a")) {

				if (!mCode.contentEquals("Error")) {
					mAListView.setAdapter(adapter);
					mFileListA = result;

				} else {

					mActivateNode = "a";
					try {
						makeToast(result.get(1), Toast.LENGTH_LONG);
						if (result.get(1).endsWith("Activation Required")) {
							mActivateEndpoint = mLocalEndpoint;
							new AutoActivate("a", mLocalEndpoint)
									.execute(mActivationRequirements);
						}
					} catch (NullPointerException ex) {
						makeToast(ex.getMessage());
					}
				}

				mAListView.setVisibility(View.VISIBLE);
				mEndpointAProgressBar.setVisibility(View.GONE);
				mSelectedFilesA.clear();

			} else if (mLocalNode.contentEquals("b")) {

				if (!mCode.contentEquals("Error")) {
					mBListView.setAdapter(adapter);
					mFileListB = result;
				} else {

					mActivateNode = "b";
					makeToast(result.get(1), Toast.LENGTH_LONG);
					if (result.get(1).endsWith("Activation Required")) {
						mActivateEndpoint = mLocalEndpoint;
						new AutoActivate("b", mLocalEndpoint)
								.execute(mActivationRequirements);
					}
				}
				mBListView.setVisibility(View.VISIBLE);
				mEndpointBProgressBar.setVisibility(View.GONE);
				mSelectedFilesB.clear();

			}

		}

		private void getCurrentPath(Map<String, String> mRequestParams) {
			if (mLocalNode.contentEquals("a")) {
				mRequestParams.put("path", mEndpointACurrentPath);
			} else if (mLocalNode.contentEquals("b")) {
				mRequestParams.put("path", mEndpointBCurrentPath);
			}

		}

		private void getShowHiddenFiles(Map<String, String> mRequestParams) {
			if (!mSharedPreferences.getBoolean("ShowHiddenFiles", false)) {
				mRequestParams.put("show_hidden", "False");
			}

		}

		private ArrayList<String> getEndpointsDirectory(
				Map<String, String> mRequestParams,
				ArrayList<String> mDirectoryList) {
			try {

				return attemptGetEndpointsDirectory(mRequestParams,
						mDirectoryList);
			} catch (IOException e) {
				e.printStackTrace();
				mDirectoryList.add("Error");
				mDirectoryList.add(getString(R.string.offline_warning));
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mDirectoryList.add("Error");
				mDirectoryList.add(e.getMessage());
			} catch (APIError e) {
				e.printStackTrace();
				mDirectoryList.add("Error");
				mDirectoryList.add(e.statusMessage);
			}

			return mDirectoryList;
		}

		private ArrayList<String> attemptGetEndpointsDirectory(
				Map<String, String> mRequestParams,
				ArrayList<String> mDirectoryList) throws MalformedURLException,
				IOException, GeneralSecurityException, JSONException, APIError {
			String mDelims = "#";
			String[] mTokens = mLocalEndpoint.split(mDelims);
			Result mQueryResult;

			mQueryResult = sClient.getResult("/endpoint/" + mTokens[0] + "%23"
					+ mTokens[1] + "/ls", mRequestParams);
			JSONObject jO = mQueryResult.document;

			if (!jO.getString("DATA_TYPE").equals("file_list")) {

				mDirectoryList.add("");
				return mDirectoryList;
			}
			JSONArray mJsonArray = jO.getJSONArray("DATA");

			return getDirectoryList(mJsonArray);
		}

		private ArrayList<String> getDirectoryList(JSONArray mJsonArray)
				throws JSONException {
			JSONObject mJsonObject;
			ArrayList<String> mDirectoryList = new ArrayList<String>();

			for (int i = 0; i < mJsonArray.length(); i++) {
				mJsonObject = mJsonArray.getJSONObject(i);
				String mLocalEndpoint;
				Float mSize;

				if (mJsonObject.getString("type").contentEquals("dir")) {

					mLocalEndpoint = mJsonObject.getString("name") + "/";
				} else {

					mLocalEndpoint = mJsonObject.getString("name");
					mSize = Float.parseFloat(mJsonObject.getString("size"));
					if (mLocalNode.contentEquals("a")) {
						mFilesSizeA.put(mLocalEndpoint, mSize);
					} else if (mLocalNode.contentEquals("b")) {
						mFilesSizeB.put(mLocalEndpoint, mSize);
					}
				}
				mDirectoryList.add(mLocalEndpoint);
			}
			return mDirectoryList;
		}

	}

	/**
	 * 
	 * 
	 * This class attempts to get the list of all the endpoints visible to the
	 * user by sending a GET request to endpoint_list. It hides all the view
	 * from the screen while communicating with the server and instead shows a
	 * progress bar.
	 * 
	 * @author christos
	 */
	public class GetEndpointsList extends
			AsyncTask<String, Void, ArrayList<String>> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			mEndpointLoadProgressBar.setProgress(0);
			mEndpointLoadProgressBar.setVisibility(View.VISIBLE);
			hideRelatedViews();
			TextView mLoadMessage = (TextView) findViewById(R.id.loading_endpoints_textview);
			mLoadMessage.setVisibility(View.VISIBLE);
		}

		@Override
		protected ArrayList<String> doInBackground(String... arg0) {

			List<String> mEndpoints = new ArrayList<String>();

			Map<String, String> mRequestParams = getRequestParameters();

			mEndpointLoadProgressBar.setProgress(1);

			mEndpoints = getEndpointList(mRequestParams);

			mEndpointLoadProgressBar.setProgress(4);

			return (ArrayList<String>) mEndpoints;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {

			super.onPostExecute(result);
			blendRelatedViews();

			if (!result.isEmpty()) {
				if (result.get(0).contentEquals(getString(R.string.error))) {
					makeToast(result.get(1), Toast.LENGTH_LONG);
					result.clear();
				}
			}
			mEndpoints = (ArrayList<String>) result.clone();

			TextView loadMessage = (TextView) findViewById(R.id.loading_endpoints_textview);
			loadMessage.setVisibility(View.GONE);
			mEndpointLoadProgressBar.setVisibility(View.GONE);

		}

		private Map<String, String> getRequestParameters() {
			Map<String, String> mRequestParams = new HashMap<String, String>();
			mRequestParams.put("limit", "0");
			mRequestParams.put("fields", "canonical_name");

			if (mSharedPreferences.getBoolean("EndpointsActiveOnly", true)) {
				mRequestParams.put("filter", "activated:true");
			}

			return mRequestParams;
		}

		private ArrayList<String> getEndpointList(
				Map<String, String> mRequestParams) {
			try {

				mEndpoints = attemptGetEndpointsList(mRequestParams);
			} catch (IOException e) {
				e.printStackTrace();
				mEndpoints.add(getString(R.string.error));
				mEndpoints.add(getString(R.string.offline_warning));
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mEndpoints.add(getString(R.string.error));
				mEndpoints.add(e.getMessage());
			} catch (APIError e) {
				e.printStackTrace();
				mEndpoints.add(getString(R.string.error));
				mEndpoints.add(e.statusMessage);
			}
			return mEndpoints;

		}

		private ArrayList<String> attemptGetEndpointsList(
				Map<String, String> mRequestParams) throws JSONException,
				MalformedURLException, IOException, GeneralSecurityException,
				APIError {
			Result mQueryResult = sClient.getResult("/endpoint_list",
					mRequestParams);
			mEndpointLoadProgressBar.setProgress(2);

			JSONObject mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("endpoint_list")) {
				mEndpoints.add(getString(R.string.error));
				return (ArrayList<String>) mEndpoints;
			}

			JSONArray mJsonArray = mJsonObject.getJSONArray("DATA");

			mEndpointLoadProgressBar.setProgress(3);

			for (int i = 0; i < mJsonArray.length(); i++) {

				String endpoint = mJsonArray.getJSONObject(i).getString(
						"canonical_name");
				mEndpoints.add(endpoint);

			}

			return mEndpoints;

		}

		private void hideRelatedViews() {
			makeInvisible(mSelectEndpointAButton, mSelectEndpointBButton,
					mAListView, mBListView, mATextView, mBTextView, mUpAButton,
					mUpBButton, mCreateAButton, mCreateBButton, mDeleteAButton,
					mDeleteBButton, mRefreshAButton, mRefreshBButton,
					mEndpointLoadProgressBar, mPathAButton, mPathButton,
					mTransferAButton, mTransferBButton);
		}

		private void blendRelatedViews() {
			makeVisible(mSelectEndpointAButton, mSelectEndpointBButton,
					mAListView, mBListView, mATextView, mBTextView, mUpAButton,
					mUpBButton, mCreateAButton, mCreateBButton, mDeleteAButton,
					mDeleteBButton, mRefreshAButton, mRefreshBButton,
					mEndpointLoadProgressBar, mPathAButton, mPathButton,
					mTransferAButton, mTransferBButton);
		}

	}

	/**
	 * This class attempts to transfer the files selected by the user from the
	 * one Endpoint to the other. It first gets a submission id by sending GET
	 * /submission_id. Once the submission id is acquired, it POSTs a JSON
	 * Object to /transfer, containing the submission id, the transfer task's
	 * name(if any), the source endpoint's name, the destination endpoint's name
	 * and a JSONArray containing the paths of the files/folders that are to be
	 * transfered along with the paths that they are bound to be transferred to.
	 * It informs the user for the request's result with a Toast.
	 * 
	 * 
	 * 
	 * @author christos
	 * 
	 */
	public class FilesTransfer extends AsyncTask<String, Void, List<String>> {

		String mLocalNode;
		String mLocaltransferName;
		String mId;
		Map<String, String> mPaths;
		String mSourceEndpoint;
		String mDestinationEndpoint;
		List<String> mMessage;
		JSONArray mJSONArray;
		JSONObject mSubmit;

		public FilesTransfer(String node, String transferName) {
			this.mLocalNode = node;
			this.mLocaltransferName = transferName;
			mPaths = new HashMap<String, String>();
			mMessage = new ArrayList<String>();
			mSourceEndpoint = null;
			mDestinationEndpoint = null;
			mJSONArray = new JSONArray();
			mSubmit = new JSONObject();
		}

		@Override
		protected List<String> doInBackground(String... arg0) {

			return transferFiles();
		}

		@Override
		protected void onPostExecute(List<String> result) {

			super.onPostExecute(result);
			makeToast(result.get(0), Toast.LENGTH_LONG);
		}

		private List<String> transferFiles() {
			List<String> mMessage = new ArrayList<String>();
			try {
				mMessage = attemptTranferFiles();
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mMessage.add(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mMessage.add(getString(R.string.offline_warning));
			} catch (APIError e) {
				e.printStackTrace();
				mMessage.add(e.statusMessage);
			}
			return mMessage;
		}

		private List<String> attemptTranferFiles()
				throws MalformedURLException, IOException,
				GeneralSecurityException, JSONException, APIError {

			if (!getSubmissionId()) {
				return mMessage;
			}

			getFilesToBeTransferred();
			getEndpoints();
			getJSONArray();
			getJSONObject();
			sendTransferRequest();

			return mMessage;
		}

		private void getFilesToBeTransferred() {
			if (mLocalNode.contentEquals("a")) {
				for (int i = 0; i < mSelectedFilesA.size(); i++) {

					mPaths.put(mEndpointACurrentPath.concat(mSelectedFilesA
							.get(i)), mEndpointBCurrentPath
							.concat(mSelectedFilesA.get(i)));

				}

			} else if (mLocalNode.contentEquals("b")) {
				for (int i = 0; i < mSelectedFilesB.size(); i++) {

					mPaths.put(mEndpointBCurrentPath.concat(mSelectedFilesB
							.get(i)), mEndpointACurrentPath
							.concat(mSelectedFilesB.get(i)));
				}
			}

		}

		private void getEndpoints() {

			if (mLocalNode.contentEquals("a")) {

				mSourceEndpoint = mSelectEndpointAButton.getText().toString();
				mDestinationEndpoint = mSelectEndpointBButton.getText()
						.toString();

			} else if (mLocalNode.contentEquals("b")) {

				mSourceEndpoint = mSelectEndpointBButton.getText().toString();
				mDestinationEndpoint = mSelectEndpointAButton.getText()
						.toString();

			}
		}

		private void getJSONArray() throws JSONException {

			for (Map.Entry<String, String> entry : mPaths.entrySet()) {
				JSONObject temp = new JSONObject();
				String mSourcePath = entry.getKey();
				String mDestinationPath = entry.getValue();
				temp.put("source_path", mSourcePath);
				temp.put("DATA_TYPE", "transfer_item");
				temp.put("destination_path", mDestinationPath);
				if (mSourcePath.endsWith("/"))
					temp.put("recursive", true);
				else
					temp.put("recursice", false);
				mJSONArray.put(temp);
			}

		}

		private void getJSONObject() throws JSONException {

			mSubmit.put("submission_id", mId);
			mSubmit.put("source_endpoint", mSourceEndpoint);
			mSubmit.put("destination_endpoint", mDestinationEndpoint);
			mSubmit.put("DATA_TYPE", "transfer");

			if (!mLocaltransferName.matches("[ ]*")) {
				mSubmit.put("label", mLocaltransferName);
			}

			mSubmit.put("DATA", mJSONArray);

		}

		private boolean getSubmissionId() throws MalformedURLException,
				IOException, GeneralSecurityException, JSONException, APIError {
			Result mQueryResult = sClient.getResult("/submission_id");

			JSONObject mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("submission_id")) {
				mMessage.add(getString(R.string.error));
				return false;
			}
			mId = mJsonObject.getString("value");
			return true;
		}

		private void sendTransferRequest() throws MalformedURLException,
				IOException, GeneralSecurityException, JSONException, APIError {

			Result mResult;
			mResult = sClient.postResult("transfer", mSubmit);
			JSONObject jsonObject = mResult.document;
			mMessage.add(jsonObject.getString("message"));
			mMessage.add(jsonObject.getString("code"));
		}

	}

	/**
	 * This class attempts to autoactivate an Endpoint, in case it is not
	 * activated, by POSTing and empty JSON Object to
	 * endpoint/<canonical_name>/autoactivate. In case the Endpoint can not be
	 * auto activated, it creates a dialog prompting the user to enter the
	 * credentials needed for the Endpoint to be activated. If the user types
	 * the necessary credentials and clicks the "Activate" button, the execution
	 * function of the Activate class is called.
	 * 
	 * @author christos
	 * 
	 */
	public class AutoActivate extends
			AsyncTask<JSONObject, Void, ArrayList<String>> {

		String mLocalNode;
		String mEndpoint;

		public AutoActivate(String node, String endpoint) {
			this.mLocalNode = node;
			this.mEndpoint = endpoint;
			mActivationRequirements = null;
		}

		@Override
		protected ArrayList<String> doInBackground(JSONObject... args) {

			List<String> mResult = new ArrayList<String>();

			try {

				mResult = autoActivate(args[0]);
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(getString(R.string.offline_warning));
			} catch (APIError e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(e.statusMessage);
			}

			return (ArrayList<String>) mResult;
		}

		private List<String> autoActivate(JSONObject emptyObject)
				throws JSONException, MalformedURLException, IOException,
				GeneralSecurityException, APIError {

			List<String> mResult = new ArrayList<>();
			JSONObject mEmptyJsonObject = emptyObject;
			Result mQueryResult;
			String mDelims = "#";
			String[] mTokens = mEndpoint.split(mDelims);
			mQueryResult = sClient.postResult("/endpoint/" + mTokens[0] + "%23"
					+ mTokens[1] + "/autoactivate", mEmptyJsonObject);
			JSONObject mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("activation_result")) {
				mResult.add(getString(R.string.error));
				mResult.add(getString(R.string.error));
				return mResult;
			}

			mResult.add(mJsonObject.getString("code"));
			mResult.add(mJsonObject.getString("message"));

			if (!mResult.get(0).startsWith("AutoActivated")) {
				mActivationRequirements = mJsonObject;
			}
			return mResult;

		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {

			super.onPostExecute(result);

			if (result.get(0).startsWith("AutoActivated")) {
				loadEndpointsDirectory(result.get(1));
			} else if (result.get(0).contentEquals(getString(R.string.error))) {
				makeToast(result.get(1), Toast.LENGTH_LONG);
			} else {
				createActivationDialog();
			}

		}

		private void loadEndpointsDirectory(String message) {
			new GetEndpointsDirectory(mLocalNode, mEndpoint).execute();
			makeToast(message, Toast.LENGTH_LONG);
		}

		private void createActivationDialog() {
			String title = "";
			final JSONArray mJSONArray;
			mActivateDialog = new Dialog(mContext);
			mActivateDialog.setCanceledOnTouchOutside(false);
			mActivateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mActivateDialog.setContentView(R.layout.activate_dialog);
			TextView mTitleTextView = (TextView) mActivateDialog
					.findViewById(R.id.activate_title_id);

			LinearLayout layout = (LinearLayout) mActivateDialog
					.findViewById(R.id.linear);

			final Map<Integer, EditText> mEditTextMap = new HashMap<Integer, EditText>();
			final Map<Integer, TextView> mTextViewMap = new HashMap<Integer, TextView>();
			try {
				mJSONArray = mActivationRequirements.getJSONArray("DATA");
				int size = mJSONArray.length();
				title = mActivationRequirements.getString("endpoint");
				for (int i = 0; i < size; i++) {
					JSONObject mTempObject = mJSONArray.getJSONObject(i);
					if (!mTempObject.getString("type").contentEquals(
							"delegate_proxy")) {

						TextView mTextView = new TextView(mContext);
						EditText mEditText = new EditText(mContext);

						// If the field is marked as "required" a * is put
						// next to the field's name
						if (!mTempObject.getBoolean("required")) {
							mTextView.setText(mTempObject.getString("ui_name"));
						} else {
							mTextView.setText("*"
									+ mTempObject.getString("ui_name"));
						}
						String mValue = mTempObject.getString("value");

						if (!mValue.contentEquals("null")) {
							mEditText.setText(mValue);

						}
						mTextView.setLayoutParams(new LayoutParams(
								ViewGroup.LayoutParams.WRAP_CONTENT,
								ViewGroup.LayoutParams.WRAP_CONTENT));
						mEditText.setLayoutParams(new LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT));
						if (mTempObject.getBoolean("private")) {
							mEditText.setInputType(InputType.TYPE_CLASS_TEXT
									| InputType.TYPE_TEXT_VARIATION_PASSWORD);
						}

						mEditTextMap.put(i, mEditText);

						mTextViewMap.put(i, mTextView);
						layout.addView(mTextView);
						layout.addView(mEditText);

					}
				}

				// If the endpoint can not be activated through the
				// application the user is informed
				// with an appropriate message on the Activation Window
				mTitleTextView.setText("Activate Endpoint:" + title);
				if (mEditTextMap.isEmpty()) {
					TextView mTextView = new TextView(mContext);
					mTextView.setText(getString(R.string.oauth));
					mTextView.setLayoutParams(new LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT));
					layout.addView(mTextView);

				}

				Button mOkButton = (Button) mActivateDialog
						.findViewById(R.id.ok_button);

				mOkButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						// If the Window contains nothing but a message
						// informing the user that the endpoint
						// can not be activated, when the OK Button is
						// pressed the Dialog disappears nothing happens
						if (mEditTextMap.isEmpty()) {
							mActivateDialog.dismiss();
						} else {
							boolean mReady = true;

							for (Entry<Integer, EditText> entry : mEditTextMap
									.entrySet()) {

								String mEditTextValue = entry.getValue()
										.getText().toString();
								int key = entry.getKey();
								String mTextViewValue = mTextViewMap.get(key)
										.getText().toString();

								if (mTextViewValue.startsWith("*")
										&& mEditTextValue.isEmpty()) {
									mReady = false;

								}

							}
							// If the user has filled all the necessary
							// fields, the Activate Dialog
							// is dismissed and the execute method of the
							// Activate class is called
							if (mReady && isInternetConnectionAvailable()) {

								try {
									for (Entry<Integer, EditText> entry : mEditTextMap
											.entrySet()) {
										int key = entry.getKey();
										JSONObject mJSONObject = mJSONArray
												.getJSONObject(key);
										mJSONObject.put("value", entry
												.getValue().getText()
												.toString());
										mJSONArray.put(key, mJSONObject);

									}

									mActivationRequirements.remove("DATA");
									mActivationRequirements.put("DATA",
											mJSONArray);

								} catch (JSONException e) {

									e.printStackTrace();
								}

								mActivateDialog.dismiss();
								new Activate(mLocalNode, mEndpoint)
										.execute(mActivationRequirements);
							}
							// If some of the required fields have not been
							// filled, the user is informed
							// with a Toast message
							else if (!mReady) {

								makeToast(
										getString(R.string.fields_not_filled_warning),
										Toast.LENGTH_SHORT);
							} else if (!isInternetConnectionAvailable()) {
								makeToast(getString(R.string.offline_warning),
										Toast.LENGTH_SHORT);
							}

						}

					}

				});

				mActivateDialog.show();

			} catch (JSONException e) {

				e.printStackTrace();
			}

		}

	}

	/**
	 * This class attempts to activate an endpoint by POSTing a JSON Object
	 * containing the Credentials Required as they were filled by the user to
	 * endpoint/<canonical_name>/activate. It informs the user of the result
	 * with a Toast message.
	 * 
	 * @author christos
	 * 
	 */
	public class Activate extends
			AsyncTask<JSONObject, Void, ArrayList<String>> {

		String mLocalNode;
		String mEndpoint;

		public Activate(String node, String endpoint) {
			this.mLocalNode = node;
			this.mEndpoint = endpoint;

		}

		@Override
		protected ArrayList<String> doInBackground(JSONObject... args) {

			return (ArrayList<String>) activate(args[0]);
		}

		private List<String> activate(JSONObject requirments) {
			List<String> mResult = new ArrayList<>();

			try {
				mResult = attemptActivate(requirments);
			} catch (GeneralSecurityException | JSONException e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(getString(R.string.offline_warning));
			} catch (APIError e) {
				e.printStackTrace();
				mResult.add(getString(R.string.error));
				mResult.add(e.statusMessage);
			}

			return mResult;
		}

		private List<String> attemptActivate(JSONObject requirments)
				throws MalformedURLException, IOException,
				GeneralSecurityException, JSONException, APIError {
			List<String> mResult = new ArrayList<>();
			String mDelims = "#";
			String[] mTokens = mEndpoint.split(mDelims);

			Result mQueryResult;

			JSONObject mRequirementsJsonObject = requirments;

			mQueryResult = sClient.postResult("/endpoint/" + mTokens[0] + "%23"
					+ mTokens[1] + "/activate", mRequirementsJsonObject);
			JSONObject mJsonObject = mQueryResult.document;
			if (!mJsonObject.getString("DATA_TYPE").equals("activation_result")) {
				mResult.add(getString(R.string.error));
				mResult.add(getString(R.string.error));
				return mResult;
			}
			mResult.add(mJsonObject.getString("code"));
			mResult.add(mJsonObject.getString("message"));
			return mResult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {

			super.onPostExecute(result);
			makeToast(result.get(1), Toast.LENGTH_LONG);
		}

	}

	@Override
	protected void onStop() {

		super.onStop();
		mSettingsDialog.dismiss();
		if (mCreateFolderDialog != null) {
			mCreateFolderDialog.dismiss();
		}
		if (mDeleteFilesDialog != null) {
			mDeleteFilesDialog.dismiss();
		}
		if (mActivateDialog != null) {
			mActivateDialog.dismiss();
		}
		if (mMakeTransferDialog != null) {
			mMakeTransferDialog.dismiss();
		}

	}

	/**
	 * Android build-in function that we override in order to save data when the
	 * activity is temporarily destroyed
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putStringArrayList("endpoints_list", mEndpoints);
		outState.putString("path_a", mEndpointACurrentPath);
		outState.putString("path_b", mEndpointBCurrentPath);
		outState.putString("path_a_text_view", mPathAButton.getText()
				.toString());
		outState.putString("path_b_text_view", mPathButton.getText().toString());
		outState.putStringArrayList("selected_files_a",
				(ArrayList<String>) mSelectedFilesA);
		outState.putStringArrayList("selected_files_b",
				(ArrayList<String>) mSelectedFilesB);
		outState.putString("endpoint_a", mSelectEndpointAButton.getText()
				.toString());
		outState.putString("endpoint_b", mSelectEndpointBButton.getText()
				.toString());
		outState.putStringArrayList("dir_a", mFileListA);
		outState.putStringArrayList("dir_b", mFileListB);
		outState.putSerializable("filesizeA", (Serializable) mFilesSizeA);
		outState.putSerializable("filesizeB", (Serializable) mFilesSizeB);
		outState.putBoolean("Settings", mSettingsDialog.isShowing());
		if (mSettingsDialog.isShowing()) {
			outState.putBoolean("hiddenTemp", mHiddenTemp);
			outState.putBoolean("endpointsTemp", mEndpointsTemp);
		}
		if (mCreateFolderDialog != null) {
			outState.putBoolean("createFolder", mCreateFolderDialog.isShowing());
			if (mCreateFolderDialog.isShowing()) {
				outState.putString("node", mNode);
				outState.putString("createFolderName", mCreateFolderEditText
						.getText().toString());
			}
		}
		if (mDeleteFilesDialog != null) {
			outState.putBoolean("deleteFiles", mDeleteFilesDialog.isShowing());
			if (mDeleteFilesDialog.isShowing()) {
				outState.putString("node", mNode);
				outState.putString("deleteFolderName", mDeleteFilesEditText
						.getText().toString());
			}
		}

		outState.putBoolean("activating", false);
		if (mActivateDialog != null) {
			if (mActivateDialog.isShowing()) {
				outState.putBoolean("activating", true);
				outState.putString("node", mActivateNode);
				outState.putString("endpoint", mActivateEndpoint);
			}

		}
		outState.putBoolean("transfer", false);
		if (mMakeTransferDialog != null) {
			if (mMakeTransferDialog.isShowing()) {
				outState.putBoolean("transfer", true);
				outState.putString("node", mNode);
				outState.putString("transferName", mMakeTransferEditText
						.getText().toString());
			}

		}
	}

	private void initializeVariables() {
		mActivationRequirements = null;
		mEndpoints = new ArrayList<String>();
		mLastTimePressedA = new HashMap<String, Long>();
		mLastTimePressedB = new HashMap<String, Long>();
		mFilesSizeA = new HashMap<String, Float>();
		mFilesSizeB = new HashMap<String, Float>();
		mSelectedFilesA = new ArrayList<String>();
		mSelectedFilesB = new ArrayList<String>();
		mFileListA = new ArrayList<String>();
		mFileListB = new ArrayList<String>();
		mRateService = new RateServiceImpl();
	}

	private void initializeSettingsDialog() {

		initializeComponents();
		setListenerForEndpointsVisibleButton();
		setListenerForHiddenBUtton();
		setListenerForOkButton();
	}

	private void initializeComponents() {

		mSettingsDialog = new Dialog(this);
		mSettingsDialog.setCanceledOnTouchOutside(false);
		mSettingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSettingsDialog.setContentView(R.layout.settings_dialog);
		mAListView.setOnItemClickListener(mMessageClickedHandlerA);
		mBListView.setOnItemClickListener(mMessageClickedHandlerB);
		mSettingOkButton = (Button) mSettingsDialog
				.findViewById(R.id.ok_button);
		mCreateAButton = (Button) findViewById(R.id.create_folder_A_button);
		mCreateBButton = (Button) findViewById(R.id.create_folder_B_button);
		mSharedPreferences = getSharedPreferences(
				getString(R.string.preferences_name), MODE_PRIVATE);
		mEndpointsVisibleToggleButton = (ToggleButton) mSettingsDialog
				.findViewById(R.id.toggleButton1);
		mHiddenToggleButton = (ToggleButton) mSettingsDialog
				.findViewById(R.id.toggleButton2);
		mEndpointsVisibleToggleButton.setChecked(mSharedPreferences.getBoolean(
				"EndpointsActiveOnly", true));
		mHiddenToggleButton.setChecked(mSharedPreferences.getBoolean(
				"ShowHiddenFiles", false));
	}

	private void setListenerForEndpointsVisibleButton() {
		mEndpointsVisibleToggleButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						SharedPreferences.Editor mEditor = mSharedPreferences
								.edit();
						if (isChecked) {
							mEditor.putBoolean("EndpointsActiveOnly", true);
						} else {
							mEditor.putBoolean("EndpointsActiveOnly", false);
						}
						mEditor.commit();
					}
				});
	}

	private void setListenerForHiddenBUtton() {
		mHiddenToggleButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {

						SharedPreferences.Editor mEditor = mSharedPreferences
								.edit();
						if (isChecked) {
							mEditor.putBoolean("ShowHiddenFiles", true);
						} else {
							mEditor.putBoolean("ShowHiddenFiles", false);
						}
						mEditor.commit();
					}
				});
	}

	private void setListenerForOkButton() {
		mSettingOkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSettingsDialog.dismiss();
				if (!isInternetConnectionAvailable()) {
					makeToast(getString(R.string.offline_warning),
							Toast.LENGTH_SHORT);
					return;
				}
				if (mSharedPreferences.getBoolean("EndpointsActiveOnly", true) != mEndpointsTemp) {
					new GetEndpointsList().execute();

				} else if (mSharedPreferences.getBoolean("ShowHiddenFiles",
						false) != mHiddenTemp) {
					if (mSelectEndpointAButton.getText().toString() != getString(R.string.select_endpoint_prompt)) {
						new GetEndpointsDirectory("a", mSelectEndpointAButton
								.getText().toString()).execute();
					}
					if (mSelectEndpointBButton.getText().toString() != getString(R.string.select_endpoint_prompt)) {
						new GetEndpointsDirectory("b", mSelectEndpointBButton
								.getText().toString()).execute();
					}

				}

			}

		});

	}

	private void initializeSetPathDialog(final String node) {
		final Dialog mSetPathDialog = new Dialog(this);
		mSetPathDialog.setCanceledOnTouchOutside(false);
		mSetPathDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSetPathDialog.setContentView(R.layout.set_path);
		final EditText mPathEditText = (EditText) mSetPathDialog
				.findViewById(R.id.path_name_edit_text);
		Button mOkButton = (Button) mSetPathDialog.findViewById(R.id.ok_button);

		mSetPathDialog.show();

		mOkButton.setOnClickListener(new OnClickListener()

		{

			@Override
			public void onClick(View arg0) {
				
				String endpoint;
				if (node.contentEquals("a")) {
					endpoint = mSelectEndpointAButton.getText().toString();
					mEndpointACurrentPath=mPathEditText.getText().toString();
				} else {
					endpoint = mSelectEndpointBButton.getText().toString();
					mEndpointBCurrentPath=mPathEditText.getText().toString();
				}
				new GetEndpointsDirectory(node, endpoint).execute();
				mSetPathDialog.dismiss();
			}

		});

	}

	private void initializeView() {

		mContext = this;
		mEndpointLoadProgressBar = (ProgressBar) findViewById(R.id.endpoints_progress_bar);
		mEndpointLoadProgressBar.setMax(4);
		mAListView = (ListView) findViewById(R.id.endpoint_A_list_view);
		mBListView = (ListView) findViewById(R.id.endpoint_B_list_view);
		mRefreshAButton = (Button) findViewById(R.id.refresh_A_button);
		mRefreshBButton = (Button) findViewById(R.id.refresh_B_button);
		mTransferAButton = (Button) findViewById(R.id.transfer_A_to_B_button);
		mTransferBButton = (Button) findViewById(R.id.transfer_B_to_A_button);
		mSelectEndpointAButton = (Button) findViewById(R.id.select_endpoint_A_button);
		mSelectEndpointBButton = (Button) findViewById(R.id.select_endpoint_B_button);
		mUpAButton = (Button) findViewById(R.id.back_A_button);
		mUpBButton = (Button) findViewById(R.id.back_B_button);
		mATextView = (TextView) findViewById(R.id.textView1);
		mBTextView = (TextView) findViewById(R.id.textView2);
		mPathAButton = (Button) findViewById(R.id.path_A);
		mPathButton = (Button) findViewById(R.id.path_B);
		mEndpointAProgressBar = (ProgressBar) findViewById(R.id.endpoints_A_directory_progress);
		mEndpointBProgressBar = (ProgressBar) findViewById(R.id.endpoints_B_directory_progress);
		mDeleteAButton = (Button) findViewById(R.id.delete_A_button);
		mDeleteBButton = (Button) findViewById(R.id.delete_B_button);
	}

	protected void createJsonClient() {
		try {

			sClient = new JSONClient(mUsername, mAuthToken);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void retrieveData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {

			reloadData(savedInstanceState);
		} else {
			new GetEndpointsList().execute();
		}

	}

	private void reloadData(Bundle savedInstanceState) {

		reloadEndpointsAndPaths(savedInstanceState);
		reloadFilesLists(savedInstanceState);
		reloadTransferButtons(savedInstanceState);
		reloadSettings(savedInstanceState);
		reloadNavigationButtons(savedInstanceState);
		reactivateNode(savedInstanceState);
		reloadTransferDialogs(savedInstanceState);
	}

	private void reloadEndpointsAndPaths(Bundle savedInstanceState) {

		mEndpoints = savedInstanceState.getStringArrayList("endpoints_list");
		mPathAButton.setText(savedInstanceState.getString("path_a_text_view"));
		mPathButton.setText(savedInstanceState.getString("path_b_text_view"));
		mEndpointACurrentPath = savedInstanceState.getString("path_a");
		mEndpointBCurrentPath = savedInstanceState.getString("path_b");
		mSelectEndpointAButton.setText(savedInstanceState
				.getString("endpoint_a"));
		mSelectEndpointBButton.setText(savedInstanceState
				.getString("endpoint_b"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void reloadFilesLists(Bundle savedInstanceState) {
		mSelectedFilesA = savedInstanceState
				.getStringArrayList("selected_files_a");
		mSelectedFilesB = savedInstanceState
				.getStringArrayList("selected_files_b");

		mFileListA = savedInstanceState.getStringArrayList("dir_a");

		mFilesSizeA = (Map<String, Float>) savedInstanceState
				.getSerializable("filesizeA");

		if (!mFileListA.isEmpty()) {
			ArrayAdapter mAdapterA = new CustomAdapter(mContext,
					android.R.layout.simple_list_item_1, mFileListA);

			mAListView.setAdapter(mAdapterA);
		}

		mFileListB = savedInstanceState.getStringArrayList("dir_b");

		mFilesSizeB = (Map<String, Float>) savedInstanceState
				.getSerializable("filesizeB");

		if (!mFileListB.isEmpty()) {
			ArrayAdapter mAdapterB = new CustomAdapter(mContext,
					android.R.layout.simple_list_item_1, mFileListB);

			mBListView.setAdapter(mAdapterB);
		}

	}

	private void reloadTransferButtons(Bundle savedInstanceState) {

		if (!mSelectedFilesB.isEmpty()
				&& mSelectEndpointAButton.getText().toString() != getString(R.string.select_endpoint_prompt)) {
			mTransferBButton
					.setBackgroundResource(R.drawable.transfer_up_active);
		}
		if (!mSelectedFilesA.isEmpty()
				&& mSelectEndpointBButton.getText().toString() != getString(R.string.select_endpoint_prompt)) {
			mTransferAButton
					.setBackgroundResource(R.drawable.transfer_down_active);
		}
	}

	private void reloadSettings(Bundle savedInstanceState) {
		if (savedInstanceState.getBoolean("Settings")) {
			mEndpointsTemp = savedInstanceState.getBoolean("endpointsTemp");
			mHiddenTemp = savedInstanceState.getBoolean("hiddenTemp");
			mSettingsDialog.show();
		}
	}

	private void reloadNavigationButtons(Bundle savedInstanceState) {

		if (savedInstanceState.getBoolean("createFolder")) {
			mNode = savedInstanceState.getString("node");
			if (mNode.contentEquals("a")) {
				createNewFolder(findViewById(R.id.create_folder_A_button));
			} else if (mNode.contentEquals("b")) {
				createNewFolder(findViewById(R.id.create_folder_B_button));
			}
			mCreateFolderEditText.setText(savedInstanceState
					.getString("createFolderName"));
		}
		if (savedInstanceState.getBoolean("deleteFiles")) {
			mNode = savedInstanceState.getString("node");
			if (mNode.contentEquals("a")) {
				deleteFiles(findViewById(R.id.delete_A_button));
			} else if (mNode.contentEquals("b")) {
				deleteFiles(findViewById(R.id.delete_B_button));
			}

			mDeleteFilesEditText.setText(savedInstanceState
					.getString("deleteFolderName"));
		}
	}

	private void reactivateNode(Bundle savedInstanceState) {
		if (savedInstanceState.getBoolean("activating")) {

			mActivateNode = savedInstanceState.getString("node");
			mActivateEndpoint = savedInstanceState.getString("endpoint");
			new AutoActivate(mActivateNode, mActivateEndpoint)
					.execute(mActivationRequirements);
		}
	}

	private void reloadTransferDialogs(Bundle savedInstanceState) {

		if (savedInstanceState.getBoolean("transfer")) {
			mNode = savedInstanceState.getString("node");
			if (mNode.contentEquals("a")) {
				makeTransfer(findViewById(R.id.transfer_A_to_B_button));
			} else if (mNode.contentEquals("b")) {
				makeTransfer(findViewById(R.id.transfer_B_to_A_button));
			}
			mMakeTransferEditText.setText(savedInstanceState
					.getString("transferName"));
		}
	}

}
