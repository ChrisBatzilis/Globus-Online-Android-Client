package org.globus.globustransfer.services;

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

public interface RateService {
public float calculateTransferRate(JSONObject task) throws NumberFormatException, JSONException, ParseException;
public  String timeFromSeconds(Long seconds);
public String sizeFromBytes(Float size);
}
