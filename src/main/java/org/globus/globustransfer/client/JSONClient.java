package org.globus.globustransfer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.globusonline.transfer.APIError;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONClient extends AuthTokenClient {

	public JSONClient(String username, String authToken) {
		super(username, authToken);

	}

	public Result getResult(String path, Map<String, String> queryParams)
			throws IOException, MalformedURLException,
			GeneralSecurityException, JSONException, APIError {

		return requestResult("GET", path, null, queryParams);
	}
	
	public Result getResult(String path) throws MalformedURLException, IOException, GeneralSecurityException, JSONException, APIError{
		return getResult(path,null);
	}

	 public Result putResult(String path, JSONObject data)
		        throws IOException, MalformedURLException, GeneralSecurityException,
		               JSONException, APIError {
		        return putResult(path, data, null);
		    }

		    public Result putResult(String path, JSONObject data,
		                            Map<String, String> queryParams)
		        throws IOException, MalformedURLException, GeneralSecurityException,
		               JSONException, APIError {

		        return requestResult("PUT", path, data, queryParams);
		    }

	public Result postResult(String path, JSONObject data) throws IOException,
			MalformedURLException, GeneralSecurityException, JSONException,
			APIError {
		return postResult(path, data, null);
	}

	public Result postResult(String path, JSONObject data,
			Map<String, String> queryParams) throws IOException,
			MalformedURLException, GeneralSecurityException, JSONException,
			APIError {

		return requestResult("POST", path, data, queryParams);
	}

	public Result requestResult(String method, String path, JSONObject data,
			Map<String, String> queryParams) throws IOException,
			MalformedURLException, GeneralSecurityException, JSONException,
			APIError {
		String stringData = null;
		if (data != null)
			stringData = data.toString();

		HttpsURLConnection c = request(method, path, stringData, queryParams);

		Result result = new Result();
		result.statusCode = c.getResponseCode();
		result.statusMessage = c.getResponseMessage();

		result.document = new JSONObject(readString(c.getInputStream()));

		c.disconnect();

		return result;
	}

	public static class Result {
		public JSONObject document;
		public int statusCode;
		public String statusMessage;

		public Result() {
			this.document = null;
			this.statusCode = -1;
			this.statusMessage = null;
		}
	}

	public static String readString(InputStream in) throws IOException {
		Reader reader = null;
		try {
			// TODO: add charset
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

}
