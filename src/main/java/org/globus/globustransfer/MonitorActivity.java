package org.globus.globustransfer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.globus.globustransfer.R.color;
import org.globus.globustransfer.Client.JSONClient.Result;
import org.globus.globustransfer.adapters.TaskListAdapter;
import org.globus.globustransfer.comparators.TransferRateComparator;
import org.globus.globustransfer.listeners.CheckBoxListener;
import org.globus.globustransfer.services.RateService;
import org.globus.globustransfer.services.RateServiceImpl;
import org.globusonline.transfer.APIError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorActivity extends BaseActivity {

	private static JSONArray sTasksJsonArray;
	private static RateService sRateService = new RateServiceImpl();
	private static int sListSortFlag;
	private TextView mTitleTextView;
	private ListView mTasksListView;
	private List<String> mTasksList;
	private Context mContext;
	private TextView mLoadingTextView;
	private ProgressBar mLoadingProgressBar;
	private Dialog mSettingsDialog, mUpdateLabelDialog;
	private RadioButton mTenRadioButton, mTwentyRadioButton, mFiftyRadioButton,
			mHundredRadioButton;
	private RadioGroup mNumberOfTasksRadioGroup;
	private Button mSettingsButtonOk, mUpdateLabelOkButton, mUpdateLabelButton;
	private EditText mUpdateLabelEditText;
	private SharedPreferences mSharedPreferences;
	private CheckBox mActiveCheckBox, mInactiveCheckBox, mFailedCheckBox,
			mSucceededCheckBox;
	private Dialog mTransferDetailsDialog;
	private Button mCancelTaskButton;
	private String mTempNumberOfTasks, mNewLabelName;
	private Boolean mTempSucceeded, mTempFailed, mTempActive, mTempInactive,
			mTempTransfersAndDeletions;
	private ToggleButton mDeletionsToggleButton;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		mSharedPreferences = getSharedPreferences(
				getString(R.string.preferences_name), MODE_PRIVATE);

		retrieveUsernameAndToken();
		initializeView();
		initializeSettingsDialog();
		loadStoredPreferences();
		createSettinsMenuListeners();
		createJsonClient();
		retrieveTasksList(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.monitor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.monitor_settings:
			retrieveSavedSettings();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void retrieveSavedSettings() {

		mTempActive = mSharedPreferences.getBoolean("active", true);
		mTempInactive = mSharedPreferences.getBoolean("inactive", true);
		mTempSucceeded = mSharedPreferences.getBoolean("succeeded", true);
		mTempFailed = mSharedPreferences.getBoolean("failed", true);
		mTempNumberOfTasks = mSharedPreferences.getString("Number_of_tasks",
				"10");
		mTempTransfersAndDeletions = mSharedPreferences.getBoolean(
				"deletionsAndTransfers", true);
		mSettingsDialog.show();
	}

	public void refresh(View view) {

		if (isInternetConnectionAvailable()) {
			new GetTasksList().execute();
		} else {
			makeToast(getString(R.string.offline_warning));
		}
	}

	private void updateLabel(JSONObject mTask) {

		final JSONObject m_Task = mTask;
		mUpdateLabelDialog = new Dialog(this);
		mUpdateLabelDialog.setCanceledOnTouchOutside(false);
		mUpdateLabelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mUpdateLabelDialog.setContentView(R.layout.update_label_dialog);
		mUpdateLabelEditText = (EditText) mUpdateLabelDialog
				.findViewById(R.id.update_label_edit_text);
		mUpdateLabelOkButton = (Button) mUpdateLabelDialog
				.findViewById(R.id.ok_button);
		mUpdateLabelDialog.show();
		mUpdateLabelOkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				mNewLabelName = mUpdateLabelEditText.getText().toString();
				if (!mNewLabelName.matches("[A-Z[a-z][0-9][ ]]+")
						|| mNewLabelName.matches("[ ]*")) {
					makeToast(getString(R.string.invalid_name_warning));
				} else {
					if (isInternetConnectionAvailable()) {
						mUpdateLabelDialog.dismiss();
						mUpdateLabelEditText.setText("");
						new UpdateLabel(m_Task).execute(mNewLabelName);
					} else {
						makeToast(getString(R.string.offline_warning));
					}
				}
			}
		});
	}

	private void cancelTask() {
		
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(R.string.cancel_task_question)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
					
							public void onClick(DialogInterface dialog,
									int which) {
								mCancelTaskButton
										.setBackgroundResource(color.android_dark_red);
								TextView tv = (TextView) mTransferDetailsDialog
										.findViewById(R.id.id_value_1);
								String id = tv.getText().toString();
								if (isInternetConnectionAvailable()) {
									new CancelTask().execute(id);
								} else {
									makeToast(getString(R.string.offline_warning));
								}
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mCancelTaskButton
										.setBackgroundResource(color.android_dark_red);
							}
						}).show();
	}

	public void sortList(View view) {

		if (!mTasksList.isEmpty()) {
			Collections.sort(mTasksList, new TransferRateComparator(
					sTasksJsonArray, sListSortFlag));
			adapter.notifyDataSetChanged();
			sListSortFlag = sListSortFlag * (-1);
		}
	}

	private void initializeSettingsDialog() {

		mSettingsDialog = new Dialog(this);
		mSettingsDialog.setCanceledOnTouchOutside(false);
		mSettingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSettingsDialog.setContentView(R.layout.monitor_settings);
		mTenRadioButton = (RadioButton) mSettingsDialog
				.findViewById(R.id.radio_10);
		mTwentyRadioButton = (RadioButton) mSettingsDialog
				.findViewById(R.id.radio_20);
		mFiftyRadioButton = (RadioButton) mSettingsDialog
				.findViewById(R.id.radio_50);
		mHundredRadioButton = (RadioButton) mSettingsDialog
				.findViewById(R.id.radio_100);
		mNumberOfTasksRadioGroup = (RadioGroup) mSettingsDialog
				.findViewById(R.id.radioGroup1);
		mActiveCheckBox = (CheckBox) mSettingsDialog
				.findViewById(R.id.checkBox_active);
		mInactiveCheckBox = (CheckBox) mSettingsDialog
				.findViewById(R.id.checkBox_inactive);
		mSucceededCheckBox = (CheckBox) mSettingsDialog
				.findViewById(R.id.checkBox_succeeded);
		mFailedCheckBox = (CheckBox) mSettingsDialog
				.findViewById(R.id.checkBox_failed);
		mDeletionsToggleButton = (ToggleButton) mSettingsDialog
				.findViewById(R.id.deletions_toggle_button);
		mSettingsButtonOk = (Button) mSettingsDialog
				.findViewById(R.id.ok_button);
		mActiveCheckBox.setOnCheckedChangeListener(new CheckBoxListener(
				mSharedPreferences, "active"));
		mInactiveCheckBox.setOnCheckedChangeListener(new CheckBoxListener(
				mSharedPreferences, "inactive"));
		mFailedCheckBox.setOnCheckedChangeListener(new CheckBoxListener(
				mSharedPreferences, "failed"));
		mSucceededCheckBox.setOnCheckedChangeListener(new CheckBoxListener(
				mSharedPreferences, "suceeded"));
	}

	private void initializeView() {

		mTitleTextView = (TextView) findViewById(R.id.monitor_title);
		mTitleTextView.setText(String.format("%s", mUsername));
		mTasksListView = (ListView) findViewById(R.id.tasks_list);
		mTasksList = new ArrayList<String>();
		mLoadingTextView = (TextView) findViewById(R.id.loading_tasks_text_view);
		mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading_tasks_progress_bar);
		mContext = this;
		mTasksListView.setOnItemClickListener(new ListListener());
		sListSortFlag = 1;
	}

	private void loadStoredPreferences() {

		mDeletionsToggleButton.setChecked(mSharedPreferences.getBoolean(
				"deletionsAndTransfers", true));

		String selection = mSharedPreferences
				.getString("Number_of_tasks", "10");

		if (selection.contentEquals("10")) {
			mTenRadioButton.setChecked(true);
		} else if (selection.contentEquals("20")) {
			mTwentyRadioButton.setChecked(true);
		} else if (selection.contentEquals("50")) {
			mFiftyRadioButton.setChecked(true);
		} else if (selection.contentEquals("100")) {
			mHundredRadioButton.setChecked(true);
		}

		if (mSharedPreferences.getBoolean("active", true)) {
			mActiveCheckBox.setChecked(true);
		} else {
			mActiveCheckBox.setChecked(false);
		}
		if (mSharedPreferences.getBoolean("inactive", true)) {
			mInactiveCheckBox.setChecked(true);
		} else {
			mInactiveCheckBox.setChecked(false);
		}

		if (mSharedPreferences.getBoolean("failed", true)) {
			mFailedCheckBox.setChecked(true);
		} else {
			mFailedCheckBox.setChecked(false);
		}
		if (mSharedPreferences.getBoolean("succeeded", true)) {
			mSucceededCheckBox.setChecked(true);
		} else {
			mSucceededCheckBox.setChecked(false);
		}

	}

	private void createSettinsMenuListeners() {

		mNumberOfTasksRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						SharedPreferences.Editor editor = mSharedPreferences
								.edit();

						switch (checkedId) {
						case R.id.radio_10:
							editor.putString("Number_of_tasks", "10");
							break;
						case R.id.radio_20:
							editor.putString("Number_of_tasks", "20");
							break;
						case R.id.radio_50:
							editor.putString("Number_of_tasks", "50");
							break;
						case R.id.radio_100:
							editor.putString("Number_of_tasks", "100");
							break;
						default:
							break;
						}
						editor.commit();
					}
				});
		mDeletionsToggleButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {

						SharedPreferences.Editor mEditor = mSharedPreferences
								.edit();
						if (isChecked) {

							mEditor.putBoolean("deletionsAndTransfers", true);

						} else {

							mEditor.putBoolean("deletionsAndTransfers", false);

						}
						mEditor.commit();
					}

				});

		mSettingsButtonOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSettingsDialog.dismiss();

				String mNewNumberOfTasks = mSharedPreferences.getString(
						"Number_of_tasks", "10");
				Boolean mNewTasksActive = mSharedPreferences.getBoolean(
						"active", true);
				Boolean mNewTaskInactive = mSharedPreferences.getBoolean(
						"inactive", true);
				Boolean mNewTaskFailed = mSharedPreferences.getBoolean(
						"failed", true);
				Boolean mNewTaskSucceeded = mSharedPreferences.getBoolean(
						"succeeded", true);
				Boolean mNewTransfersAndDeletions = mSharedPreferences
						.getBoolean("deletionsAndTransfers", true);

				// If any of the Settings has changed, the Tasks Lists is
				// reloaded
				if ((mNewNumberOfTasks != mTempNumberOfTasks
						|| mNewTasksActive != mTempActive
						|| mNewTaskInactive != mTempInactive
						|| mNewTaskFailed != mTempFailed
						|| mNewTaskSucceeded != mTempSucceeded || mNewTransfersAndDeletions != mTempTransfersAndDeletions)
						&& isInternetConnectionAvailable()) {
					new GetTasksList().execute();
				}
			}

		});

	}

	private void loadSavedTasks(Bundle savedInstanceState) {

		mTasksList = savedInstanceState.getStringArrayList("tasksList");
		try {
			sTasksJsonArray = new JSONArray(
					savedInstanceState.getString("tasksJSONArray"));
			adapter = new TaskListAdapter(mContext,
					android.R.layout.simple_list_item_1, mTasksList,
					sTasksJsonArray);
			mTasksListView.setAdapter(adapter);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void retrieveTasksList(Bundle savedInstanceState) {
	
		if (savedInstanceState != null) {
			loadSavedTasks(savedInstanceState);
		} else {
			new GetTasksList().execute();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putStringArrayList("tasksList", (ArrayList<String>) mTasksList);
		outState.putString("tasksJSONArray", sTasksJsonArray.toString());
	}

	private class GetTasksList extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			mTasksListView.setAdapter(null);
			mTasksList.clear();
			mLoadingProgressBar.setVisibility(View.VISIBLE);
			mLoadingTextView.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... arg0) {
			String mResult = "OK";
			Result result;
			try {

				Map<String, String> requestParams = getRequestParameters();
				result = sClient.getResult("task_list", requestParams);
				JSONObject jO = result.document;

				if (!jO.getString("DATA_TYPE").equals("task_list")) {
					return null;
				}
				populateTaskList(jO);

			} catch (IOException | GeneralSecurityException | APIError
					| JSONException e) {
				mResult = e.getMessage();
			}

			return mResult;
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			if (!result.contentEquals("OK")) {

				makeToast(result);
			} else {

				adapter = new TaskListAdapter(mContext,
						android.R.layout.simple_list_item_1, mTasksList,
						sTasksJsonArray);
				mTasksListView.setAdapter(adapter);

			}

			mLoadingProgressBar.setVisibility(View.GONE);
			mLoadingTextView.setVisibility(View.GONE);

		}

		private void populateTaskList(JSONObject jO) throws JSONException {

			sTasksJsonArray = jO.getJSONArray("DATA");
			for (int i = 0; i < sTasksJsonArray.length(); i++) {

				String taskId = sTasksJsonArray.getJSONObject(i).getString(
						"task_id");
				mTasksList.add(taskId);
			}
		}

		private Map<String, String> getRequestParameters() {

			Map<String, String> mRequestParams = new HashMap<>();
			String mNumberOfTasks;
			String mFilter;
			if (mSharedPreferences.getBoolean("deletionsAndTransfers", true)) {
				mFilter = "type:TRANSFER,DELETE/status:";
			} else {
				mFilter = "type:TRANSFER/status:";
			}

			mNumberOfTasks = mSharedPreferences.getString("Number_of_tasks",
					"10");

			if (mSharedPreferences.getBoolean("active", true)) {
				mFilter = mFilter.concat("ACTIVE,");
			}

			if (mSharedPreferences.getBoolean("inactive", true)) {
				mFilter = mFilter.concat("INACTIVE,");
			}

			if (mSharedPreferences.getBoolean("succeeded", true)) {
				mFilter = mFilter.concat("SUCCEEDED,");
			}
			if (mSharedPreferences.getBoolean("failed", true)) {
				mFilter = mFilter.concat("FAILED,");
			}

			if (mFilter.endsWith(",")) {
				mFilter = mFilter.substring(0, mFilter.length() - 1);
				mRequestParams.put("filter", mFilter);
			} else {
				// If no category of tasks is selected, this trick is used
				// in
				// order to always get zero number of tasks
				mRequestParams.put("filter", "username:NOT" + mUsername);
			}
			mRequestParams.put("limit", mNumberOfTasks);
			return mRequestParams;
		}
	}

	private class ListListener implements OnItemClickListener {

		JSONObject mTask;
		String mId = null;
		String mStatus = null;
		String mLabel = null;
		String mNiceStatus = null;
		String mType = null;
		String mRequestTime = null;
		String mDeadline = null;
		String mCompletionTime = null;
		String mSubtasksSucceeded = null;
		String mSubtasksFailed = null;
		String mSubtasksPending = null;
		String mSubtasksRetrying = null;
		String mSubtasksExpired = null;
		String mSubtasksSkipped = null;
		String mSubtasksCancelled = null;
		String mSourceEndpoint = null;
		String mDestinationEndpoint = null;
		Float mTotalSize = null;
		long mTotalTime;
		DateFormat mInputDateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd' 'HH:mm:ss+SS:SS");
		Date mDateStart, mDateFinish;
		Float mTransferRate;

		Button mOkButton;
		ImageView mStatusImageView;
		TextView mStatusTextView, mIdTextView, mOriginEndpointTextView,
				mDestinationEndpointTextView, mRequestTimeTextView,
				mDeadlineTextView, mCompletionTimeTextView, mTypeTextView,
				mTotalSizeTextView, mTotalTimeTextView, mTransferRateTextView;
		TextView mFailedSubtasksTextView, mPendingSubtasksTextView,
				mRetryingSubtasksTextView, mExpiredSubtasksTextView,
				mSkippedSubtasksTextView, mCancelledSubtasksTextView,
				mSucceededSubtasksTextView;

		@Override
		public void onItemClick(AdapterView parent, View arg1, int position,
				long id_of_task) {

			try {
				// The task with the nth position on the JSONArray is retrieved
				mId = mTasksList.get(position);
				mTask = sTasksJsonArray.getJSONObject(position);

				for (int i = 0; i < sTasksJsonArray.length(); i++) {

					JSONObject JSONtemp = sTasksJsonArray.getJSONObject(i);
					String taskId = JSONtemp.getString("task_id");
					if (mId.contentEquals(taskId))
						mTask = JSONtemp;

				}
				// All the relevant information about the task is retrieved
				// mId = mTask.getString("task_id");
				mStatus = mTask.getString("status");
				mLabel = mTask.getString("label");
				mSourceEndpoint = mTask.getString("source_endpoint");
				mDestinationEndpoint = mTask.getString("destination_endpoint");

				mRequestTime = mTask.getString("request_time");
				mDeadline = mTask.getString("deadline");
				mCompletionTime = mTask.getString("completion_time");

				mSubtasksSucceeded = mTask.getString("subtasks_succeeded");
				mSubtasksFailed = mTask.getString("subtasks_failed");
				mSubtasksPending = mTask.getString("subtasks_pending");
				mSubtasksRetrying = mTask.getString("subtasks_retrying");
				mSubtasksExpired = mTask.getString("subtasks_expired");
				mSubtasksSkipped = mTask.getString("files_skipped");
				mSubtasksCancelled = mTask.getString("subtasks_canceled");
				mNiceStatus = mTask.getString("nice_status");
				mType = mTask.getString("type");
				if (mType.contentEquals("TRANSFER")) {
					mTotalSize = Float.parseFloat(mTask
							.getString("bytes_transferred"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				makeToast(e.getMessage());
			}

			// The dialog window which contains all the details of the task is
			// created
			mTransferDetailsDialog = new Dialog(mContext);
			mTransferDetailsDialog.setCanceledOnTouchOutside(false);
			mTransferDetailsDialog
					.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mTransferDetailsDialog.setContentView(R.layout.task_details);
			mOkButton = (Button) mTransferDetailsDialog
					.findViewById(R.id.ok_button);

			mStatusTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.status_value);

			mIdTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.id_value_1);

			mUpdateLabelButton = (Button) mTransferDetailsDialog
					.findViewById(R.id.label_value);

			mOriginEndpointTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.origin_value);

			mDestinationEndpointTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.destination_value);

			mStatusImageView = (ImageView) mTransferDetailsDialog
					.findViewById(R.id.status_image);

			mRequestTimeTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.request_value);

			mDeadlineTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.deadline_value);

			mCompletionTimeTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.completion_value);

			mTypeTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.type_value);

			mTotalSizeTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.total_size_value);

			mCancelTaskButton = (Button) mTransferDetailsDialog
					.findViewById(R.id.cancel_task_button);

			mFailedSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.failed_value);
			mPendingSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.pending_value);
			mRetryingSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.retrying_value);
			mExpiredSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.expired_value);
			mSkippedSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.skipped_value1);
			mCancelledSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.cancelled_value);
			mSucceededSubtasksTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.succeeded_value);
			mTotalTimeTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.total_time_value);
			mTransferRateTextView = (TextView) mTransferDetailsDialog
					.findViewById(R.id.transfer_rate_value);

			// The values of all the text fields containing information about
			// the task are set
			mFailedSubtasksTextView.setText(mSubtasksFailed);
			mPendingSubtasksTextView.setText(mSubtasksPending);
			mRetryingSubtasksTextView.setText(mSubtasksRetrying);
			mExpiredSubtasksTextView.setText(mSubtasksExpired);
			mSkippedSubtasksTextView.setText(mSubtasksSkipped);
			mCancelledSubtasksTextView.setText(mSubtasksCancelled);
			mSucceededSubtasksTextView.setText(mSubtasksSucceeded);
			mOriginEndpointTextView.setText(mSourceEndpoint);
			mTypeTextView.setText(mType);
			mIdTextView.setText(mId);
			mRequestTimeTextView.setText(mRequestTime.substring(0, 19));
			mDeadlineTextView.setText(mDeadline.substring(0, 19));
			if (mType.contentEquals("TRANSFER")
					&& mStatus.contentEquals("SUCCEEDED")) {
				try {
					mDateStart = mInputDateFormatter.parse(mRequestTime);
					mDateFinish = mInputDateFormatter.parse(mCompletionTime);

					long mTimeDif = mDateFinish.getTime()
							- mDateStart.getTime();
					mTotalTime = mTimeDif / 1000;

					mTotalTimeTextView.setText(sRateService
							.timeFromSeconds(mTotalTime));

					mTransferRate = mTotalSize / (mTimeDif * 1024);
					mTransferRateTextView.setText(String.format("%.2f MB/s",
							mTransferRate));

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (!mLabel.contentEquals("null")) {
				mUpdateLabelButton.setText(mLabel);
			}

			if (!mNiceStatus.contentEquals("null")) {
				mStatusTextView.setText(mStatus + " (" + mNiceStatus + ")");
			} else {
				mStatusTextView.setText(mStatus);
			}

			if (!mDestinationEndpoint.contentEquals("null")) {
				mDestinationEndpointTextView.setText(mDestinationEndpoint);
			}

			if (!mCompletionTime.contentEquals("null")) {
				mCompletionTimeTextView.setText(mCompletionTime
						.substring(0, 19));
			}

			if (mType.contentEquals("TRANSFER")) {
				mTotalSizeTextView.setText(sRateService
						.sizeFromBytes(mTotalSize));
			}
			// The proper icon according to the task's status is set. If the
			// task is either Active or Inactive (pending) a Cancel Button
			// appears
			if (mStatus.contentEquals("ACTIVE")) {
				mStatusImageView.setImageResource(R.drawable.inprogress);
				mCancelTaskButton.setVisibility(View.VISIBLE);
			} else if (mStatus.contentEquals("SUCCEEDED")) {
				mStatusImageView.setImageResource(R.drawable.completed);
			} else if (mStatus.contentEquals("FAILED")) {
				mStatusImageView.setImageResource(R.drawable.error);
			} else if (mStatus.contentEquals("INACTIVE")) {
				mStatusImageView.setImageResource(R.drawable.inactive);
				mCancelTaskButton.setVisibility(View.VISIBLE);
			}

			mCancelTaskButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// If the user click on the Cancel Task button, the
					// cancelTask() function is called
					mCancelTaskButton
							.setBackgroundResource(color.android_light_red);
					cancelTask();
				}
			});

			mUpdateLabelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					updateLabel(mTask);
				}
			});

			mTransferDetailsDialog.show();
			mOkButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mTransferDetailsDialog.dismiss();

				}

			});

		}

	}

	private class CancelTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... task) {

			String mTaskName = task[0];
			return cancelTask(mTaskName);
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			makeToast(result);
			if (isSuccessfull(result)) {
				mTransferDetailsDialog.cancel();
				new GetTasksList().execute();
			}
		}

		private String cancelTask(String taskName) {

			String result;
			try {
				result = attemptCancelTask(taskName);

			} catch (JSONException | IOException | GeneralSecurityException
					| APIError e) {
				result = e.getMessage();
			}
			return result;
		}

		private String attemptCancelTask(String taskName) throws JSONException,
				MalformedURLException, IOException, GeneralSecurityException,
				APIError {

			String result = getString(R.string.error);
			Result mQueryResult;
			JSONObject mJsonObject = new JSONObject();
			mQueryResult = sClient.postResult("/task/" + taskName + "/cancel",
					mJsonObject);
			mJsonObject = mQueryResult.document;
			if (!mJsonObject.getString("DATA_TYPE").equals("result")) {
				return result;
			}
			result = mJsonObject.getString("message");
			return result;
		}

		private boolean isSuccessfull(String result) {
	
			return result.endsWith("successfully.");
		}
	}

	/**
	 * 
	 * This class attempts to cancel an active task with POSTing an empty JSON
	 * Object to task/<task_id>/cancel. The user is informed for the query's
	 * result with a Toast message. If the task is cancelled successfully the
	 * settings window closes and the tasks list is updated.
	 * 
	 * @author christos
	 * 
	 */
	private class UpdateLabel extends AsyncTask<String, Void, String> {

		JSONObject mTask;

		public UpdateLabel(JSONObject mTask) {
			this.mTask = mTask;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			makeToast(getString(R.string.label_update_request_submitted));
		}

		@Override
		protected String doInBackground(String... label) {

			String mLabelName = label[0];
			return updateLabel(mLabelName);
		}

		private String updateLabel(String labelName) {

			String result;
			try {
				result = attemptUpdateLabel(labelName);
			} catch (JSONException | IOException | GeneralSecurityException
					| APIError e) {
				result = e.getMessage();
			}
			return result;

		}

		private String attemptUpdateLabel(String labelName)
				throws JSONException, MalformedURLException, IOException,
				GeneralSecurityException, APIError {

			Result mQueryResult;
			JSONObject mJsonObject = new JSONObject();

			mTask.put("label", labelName);
			mQueryResult = sClient.putResult(
					"/task/" + mTask.getString("task_id"), mTask);

			mJsonObject = mQueryResult.document;

			if (!mJsonObject.getString("DATA_TYPE").equals("result")) {
				return "";
			}
			return mJsonObject.getString("message");

		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			makeToast(result);
			if (result.endsWith("successfully")) {
				mTransferDetailsDialog.cancel();
				new GetTasksList().execute();
			}
		}

	}

}
