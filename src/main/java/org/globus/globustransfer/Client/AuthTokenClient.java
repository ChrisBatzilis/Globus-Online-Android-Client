package org.globus.globustransfer.Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.globusonline.transfer.APIError;

public class AuthTokenClient {
	protected String username;
	protected String baseUrl;
	protected String format;

	protected int timeout = 30 * 1000;

	protected KeyManager[] keyManagers;
	protected TrustManager[] trustManagers;
	protected SSLSocketFactory socketFactory;

	protected String authToken = null;

	static final String VERSION = "v0.10";
	static final String DEFAULT_BASE_URL = "https://transfer.api.globusonline.org/"
			+ VERSION;

	public static final String FORMAT_JSON = "application/json";
	public static final String FORMAT_XML = "application/xml";
	public static final String FORMAT_HTML = "application/xhtml+xml";
	public static final String FORMAT_DEFAULT = FORMAT_JSON;
	public static final String TOKEN_PREFIX = "Globus-Goauthtoken ";

	static final String CLIENT_VERSION = "0.10.6";

	public AuthTokenClient(String username, String authToken) {
		this.username = username;
		this.authToken = authToken;
		this.baseUrl = DEFAULT_BASE_URL;
		this.format = FORMAT_DEFAULT;
	}

	public HttpsURLConnection request(String method, String path, String data,
			Map<String, String> queryParams) throws IOException,
			MalformedURLException, GeneralSecurityException, APIError {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (queryParams != null) {
			path += "?" + buildQueryString(queryParams);
		}

		URL url = new URL(this.baseUrl + path);
		HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
		c.setConnectTimeout(this.timeout);
//		c.setSSLSocketFactory(this.socketFactory);
		c.setRequestMethod(method);
		c.setFollowRedirects(false);
		c.setUseCaches(false);
		c.setDoInput(true);
		c.setRequestProperty("X-Transfer-API-Client", this.getClass().getName()
				+ "/" + CLIENT_VERSION);
		c.setRequestProperty("Authorization", TOKEN_PREFIX + authToken);

		c.setRequestProperty("Accept", this.format);
		if (data != null) {
			c.setDoOutput(true);
			c.setRequestProperty("Content-Type", this.format);
			c.setRequestProperty("Content-Length", "" + data.length());
		}
		c.connect();

		if (data != null) {
			DataOutputStream out = new DataOutputStream(c.getOutputStream());
			out.writeBytes(data);
			out.flush();
			out.close();
		}

		int statusCode = c.getResponseCode();
		if (statusCode >= 400) {
			String statusMessage = c.getResponseMessage();
			String errorHeader = null;
			Map<String, List<String>> headers = c.getHeaderFields();
			if (headers.containsKey("X-Transfer-API-Error")) {
				errorHeader = ((List<String>) headers
						.get("X-Transfer-API-Error")).get(0);
			}
			throw constructAPIError(statusCode, statusMessage, errorHeader,
					c.getErrorStream());
		}
		return c;
	}

	public static String buildQueryString(Map<String, String> map)
			throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (first)
				first = false;
			else
				builder.append("&");
			builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return builder.toString();
	}

	protected APIError constructAPIError(int statusCode, String statusMessage,
			String errorCode, InputStream input) {
		return new APIError(statusCode, statusMessage, errorCode);
	}

}
