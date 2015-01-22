package org.globus.globustransfer.adapters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.globus.globustransfer.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * This Adapter is a custom Adapter used for populating the List View containing
 * the user's tasks. In every row of the List it puts the label (or id) of the
 * task along with an icon indicating its status and a fraction indicating how
 * many of its subtasks have been completed.
 * 
 * @author christos
 * 
 */
public class TaskListAdapter extends ArrayAdapter<String> {

	private Context mLocalContext;
	private List<String> objects;
	private JSONArray jsonArray;

	public TaskListAdapter(Context context, int textViewResourceId,
			List<String> objects, JSONArray jsonArray) {
		super(context, textViewResourceId, objects);
		this.mLocalContext = context;
		this.objects = objects;
		this.jsonArray = jsonArray;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		String mId = null;
		String mStatus = null;
		JSONObject mTask = null;
		String mLabel = null;
		String mSubtasks = "0";
		String mSubtasksSucceeded = "0";
		String mProgress = null;
		String mType = null;
		float mTransferRate, mTotalSize;
		DateFormat mInputDateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd' 'HH:mm:ss+SS:SS");
		Date mDateStart, mDateFinish;
		try {
			mId = objects.get(position);

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject JSONtemp = jsonArray.getJSONObject(i);
				String taskId = JSONtemp.getString("task_id");
				if (mId.contentEquals(taskId))
					mTask = JSONtemp;

			}

			mStatus = mTask.getString("status");
			mLabel = mTask.getString("label");
			mSubtasks = mTask.getString("subtasks_total");
			mSubtasksSucceeded = mTask.getString("subtasks_succeeded");
			mType = mTask.getString("type");
		} catch (JSONException e) {

			e.printStackTrace();
			Toast.makeText(mLocalContext, e.getMessage(), Toast.LENGTH_LONG).show();

		}
		View mRow = convertView;

		if (mRow == null) {
			LayoutInflater vi = (LayoutInflater) mLocalContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mRow = vi.inflate(R.layout.tasks_list_view, null);
		}

		TextView mTaskIdTextView = (TextView) mRow.findViewById(R.id.task_id);
		ImageView mStatusImageView = (ImageView) mRow
				.findViewById(R.id.task_status);
		TextView mProgressTextView = (TextView) mRow
				.findViewById(R.id.progress);
		ImageView mTypeImageView = (ImageView) mRow
				.findViewById(R.id.task_type);

		// The appropriate icon is set according to the task's status
		if (mStatus.contentEquals("ACTIVE")) {
			mStatusImageView.setImageResource(R.drawable.inprogress);
		} else if (mStatus.contentEquals("SUCCEEDED")) {
			mStatusImageView.setImageResource(R.drawable.completed);
		} else if (mStatus.contentEquals("FAILED")) {
			mStatusImageView.setImageResource(R.drawable.error);
		} else if (mStatus.contentEquals("INACTIVE")) {
			mStatusImageView.setImageResource(R.drawable.inactive);
		}

		// If the task has a label, the label is used instead of the task's
		// long and bad looking id
		if (!mLabel.contentEquals("null")) {
			mId = mLabel;

		}

		// A string is formatted to show the progress of the task in the
		// form of a fraction
		// Number_of_tasks_succeeded/Number_of_tasks_in_total
		mProgress = String.format(Locale.ENGLISH, "%s/%s ", mSubtasksSucceeded,
				mSubtasks);
		mProgressTextView.setText(mProgress);

		// Calculating Performance
		if (mType.contentEquals("TRANSFER")
				&& mStatus.contentEquals("SUCCEEDED")) {
			try {
				mDateStart = mInputDateFormatter.parse(mTask
						.getString("request_time"));
				mDateFinish = mInputDateFormatter.parse(mTask
						.getString("completion_time"));
				mTotalSize = Float.parseFloat(mTask
						.getString("bytes_transferred"));

				long mTimeDif = mDateFinish.getTime() - mDateStart.getTime();

				mTransferRate = mTotalSize / (mTimeDif * 1024);
				// mPerformanceTextView.setText(String.format("%.1f",
				// mTransferRate));

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// mPerformanceTextView.setText("-");
		}
		// The appropriate icon is set according to the task's type
		if (mType.contentEquals("TRANSFER")) {
			mTypeImageView.setImageResource(R.drawable.type_transfer);
		} else if (mType.contentEquals("DELETE")) {
			mTypeImageView.setImageResource(R.drawable.type_delete);
		}

		mTaskIdTextView.setText(mId);
		return mRow;
	}

}
