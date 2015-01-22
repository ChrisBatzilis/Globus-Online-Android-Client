package org.globus.globustransfer.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.globus.globustransfer.exceptions.SignInException;
import org.json.JSONException;
import org.json.JSONObject;

public class SignInServiceImpl implements SignInService {

	private static final String sGlobusOnlineAuthenticate = "https://nexus.api.globusonline.org/authenticate";

	@Override
	public String signIn(String username, String password) {

		try {			
			return attemptSignIn(username, password);

		} catch (MalformedURLException e) {
			throw new SignInException(e.getMessage());
		} catch (ProtocolException e) {
			throw new SignInException(e.getMessage());
		} catch (IOException e) {
			throw new SignInException(e.getMessage());
		} catch (JSONException e) {
			throw new SignInException(e.getMessage());
		}
		catch(SignInException e){
			throw e;
		}

	}

	private String attemptSignIn(String username, String password)
			throws IOException, JSONException {
		HttpsURLConnection mConnection = null;
		JSONObject mSendJson = new JSONObject();
		String mReceiveString = "";
		String mAuthToken = null;

		URL mUrl = new URL(sGlobusOnlineAuthenticate);
		mConnection = (HttpsURLConnection) mUrl.openConnection();

		mSendJson.put("username", username);
		mSendJson.put("password", password);

		mConnection.setRequestMethod("POST");
		mConnection.setRequestProperty("Content-Type", "application/json");
		mConnection.setRequestProperty("Content-Length", ""
				+ mSendJson.toString().getBytes().length);
		mConnection.setRequestProperty("Content-Language", "en-US");
		mConnection.setDoOutput(true);
		mConnection.setDoInput(true);

		DataOutputStream mOutputStreamReader = new DataOutputStream(
				mConnection.getOutputStream());

		mOutputStreamReader.writeBytes(mSendJson.toString());
		mOutputStreamReader.flush();
		mOutputStreamReader.close();

		InputStreamReader mInputStreamReader = null;
		BufferedReader mBufferedReader = null;
		String mLine = "";

		mInputStreamReader = new InputStreamReader(mConnection.getInputStream());
		mBufferedReader = new BufferedReader(mInputStreamReader);

		while ((mLine = mBufferedReader.readLine()) != null) {
			mReceiveString = mReceiveString.concat(mLine);
		}
		mBufferedReader.close();

		Map<String, List<String>> mAllHeaderFields = mConnection
				.getHeaderFields();
		List<String> mAllCookieFields = mAllHeaderFields.get("Set-Cookie");

		for (String cookieString : mAllCookieFields) {
			List<HttpCookie> mCookies = HttpCookie.parse(cookieString);

			for (HttpCookie cookie : mCookies) {
				String cookieKey = cookie.getName();
				String cookieValue = cookie.getValue();
				if (cookieKey.equals("access_token")) {
					mAuthToken = cookieValue;
				}
			}
		}

		if (mConnection.getResponseCode() == 200 && mAuthToken != null) {
			return mAuthToken;
		}
		throw new SignInException(mConnection.getResponseMessage());

	}

}
