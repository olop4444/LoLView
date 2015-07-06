package com.example.lolview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class StreamActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stream);
		
		loadStreams();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stream, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Loads all LoL streams from Twitch and displays them.
	 */
	public void loadStreams() {
		new StreamTask(this).execute();
	}
	
	/**
	 * Gets a channel name of a stream.
	 * @param stream The JSONObject for a stream.
	 * @return The channel name.
	 */
	public String getChannelName(JSONObject stream) {
		try {
			return stream.getJSONObject("channel").getString("name");
		} catch (JSONException e) {
			return e.getLocalizedMessage();
		}
	}
	
	@Override
	//Starts the twitch stream on click.
	protected void onListItemClick(ListView l, View v, int position, long id) {
	  JSONObject stream = (JSONObject) getListAdapter().getItem(position);
	  String channelName = getChannelName(stream);
	  Intent intent = new Intent(this,VideoActivity.class);
	  intent.putExtra("channel", channelName);
	  startActivity(intent);
	}
	
	/**
	 * Executes the search for streams.
	 * @author Richard Shen
	 */
	private class StreamTask extends AsyncTask <Void, Void, JSONArray> {
		StreamActivity activity;
		
		/**
		 * Stores the activity which called this task.
		 * 
		 * @param activity The MatchActivity which called this task.
		 */
	    public StreamTask(StreamActivity parent) {
	    	activity = parent;
	    }


		@Override
		protected JSONArray doInBackground(Void... params) {
			String request = "https://api.twitch.tv/kraken/streams?game=League%20of%20Legends";
			String error = "Twitch streams could not be loaded.";
			String response = HttpHelp.httpRequest(request,error);
			try {
				JSONObject streamList = new JSONObject(response);
				JSONArray streams = streamList.getJSONArray("streams");
				return streams;
			} catch (JSONException e) {
				Log.d("background",response);
				return null;
			}
		}
		
		protected void onPostExecute(JSONArray streams) {
			if (streams != null) {
				try {
					int length = Math.min(streams.length(), 25);
					JSONObject[] streamArray = new JSONObject[length];
					for(int i = 0; i < length; i++) {
						streamArray[i] = streams.getJSONObject(i);
					}
					StreamAdapter adapter = new StreamAdapter(activity,streamArray);
					activity.setListAdapter(adapter);
				} catch (JSONException e) {
					Log.d("onPost",streams.toString());
				}
			}
		}
	}
}
