package org.globus.globustransfer.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


public class RateServiceImpl implements RateService {

	/**
	 * It calculates the Transfer Rate of a task, provided that the task is a
	 * completed transfer
	 * 
	 * @param mTask
	 *            The task
	 * @return The Transfer Rate of the task
	 * @throws NumberFormatException
	 * @throws JSONException
	 * @throws ParseException
	 */
	@Override
	public float calculateTransferRate(JSONObject task) 
		throws NumberFormatException, JSONException, ParseException {
			float transferRate = -1;
			if (task.getString("type").contentEquals("TRANSFER")
					&& task.getString("status").contentEquals("SUCCEEDED")) {

				DateFormat mInputDateFormatter = new SimpleDateFormat(
						"yyyy-MM-dd' 'HH:mm:ss+SS:SS");
				Date mDateStart, mDateFinish;

				float size = Float.parseFloat(task.getString("bytes_transferred"));
				mDateStart = mInputDateFormatter.parse(task
						.getString("request_time"));
				mDateFinish = mInputDateFormatter.parse(task
						.getString("completion_time"));

				long mTimeDif = mDateFinish.getTime() - mDateStart.getTime();

				transferRate = size / (mTimeDif * 1024);
			}
			return transferRate;
	}

	
	/**
	 * It converts a time interval from seconds to seconds to either seconds,
	 * minutes or hours
	 * 
	 * @param seconds
	 *            The time interval
	 * @return The time interval in the appropriate unit of time
	 */
	@Override
	public String timeFromSeconds(Long seconds) {
		String mTimeResult = "0s";

		final float minute = 60;
		final float hour = 60 * minute;

		if (seconds < minute) {
			mTimeResult = String.valueOf(seconds).concat(" s");
		} else if (minute <= seconds && seconds < hour) {
			double mins = Math.floor(seconds / minute);
			double secs = seconds - mins * minute;
			mTimeResult = String.format("%.0f m %.0f s", mins, secs);

		} else if (hour <= seconds) {
			double hours = Math.floor(seconds / hour);
			double mins = Math.floor((seconds - hours * hour) / minute);
			double secs = seconds - hours * hour - mins * minute;
			mTimeResult = String.format("%.0f h %.0f m %.0f s", hours, mins,
					secs);
		}

		return mTimeResult.concat(" ");
	}
	
	/**
	 * Returns a string containing the size of a file converted to KBs, MBs, GBs
	 * or just bytes according to its size.
	 * 
	 * @param size
	 *            The given file size in bytes
	 * @return The file size along with the appropriate unit of size
	 */
	public String sizeFromBytes(Float size) {

		String mSizeResult = "0b";

		final float KB = 1024;
		final float MB = 1024 * KB;
		final float GB = 1024 * MB;

		if (size < KB) {
			mSizeResult = String.format("%.0f", size).concat(" b");
		} else if (KB <= size && size < MB) {
			size = size / KB;

			mSizeResult = String.format("%.0f", size).concat(" KB");
		} else if (MB <= size && size < GB) {
			size = size / MB;

			mSizeResult = String.format("%.0f", size).concat(" MB");
		} else if (size >= GB) {
			size = size / GB;
			mSizeResult = String.format("%.1f", size).concat(" GB");
		}

		return mSizeResult.concat(" ");
	}

}
