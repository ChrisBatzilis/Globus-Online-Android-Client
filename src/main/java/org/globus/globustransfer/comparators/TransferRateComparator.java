package org.globus.globustransfer.comparators;

import java.text.ParseException;
import java.util.Comparator;

import org.globus.globustransfer.services.RateService;
import org.globus.globustransfer.services.RateServiceImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransferRateComparator implements Comparator<String> {

	private JSONArray jsonTasks;
	private RateService rateService;
	private int sortDirection;
	
	public TransferRateComparator(JSONArray jsonTasks,int sortDirection) {
		this.jsonTasks = jsonTasks;
		this.sortDirection=sortDirection;
		rateService=new RateServiceImpl();
	}

	@Override
	public int compare(String app1, String app2) {
		JSONObject j1 = null, j2 = null;
		float transferRate1 = 0, transferRate2 = 0;
		for (int i = 0; i < jsonTasks.length(); i++) {

			try {
				JSONObject JSONtemp = jsonTasks.getJSONObject(i);
				String taskId;
				taskId = JSONtemp.getString("task_id");
				if (app1.contentEquals(taskId)) {
					j1 = JSONtemp;
				}
				if (app2.contentEquals(taskId)) {
					j2 = JSONtemp;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		try {

			transferRate1 = rateService.calculateTransferRate(j1);
			transferRate2 = rateService.calculateTransferRate(j2);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} 

		if (transferRate1 < transferRate2) {
			return 1 * sortDirection;
		} else {
			return -1 * sortDirection;
		}
	}

}
