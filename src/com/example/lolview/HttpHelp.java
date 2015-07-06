package com.example.lolview;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpHelp {
	
	/**
	 * Makes an http request.
	 * 
	 * @param request The http request to make.
	 * @param error The error message to be displayed if the response code is an error code.
	 * @return The http entity as a string, or an error message prepended with a '!' character.
	 */
	public static String httpRequest(String request, String error) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(request);
		try {
			HttpResponse response = httpClient.execute(httpGet, localContext);	
			HttpEntity entity = response.getEntity();
			int responsevalue = response.getStatusLine().getStatusCode();
			if(responsevalue != 200) 
				return "!"+error;
			
			return EntityUtils.toString(entity);
			
		} catch (Exception e) {
			Log.e("http",e.getLocalizedMessage());
			return "!"+e.getLocalizedMessage();
		}
	}
}
