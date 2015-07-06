package com.example.lolview;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	
	String channel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		Intent intent = getIntent();
		channel = intent.getStringExtra("channel");
		setTitle(channel);
		loadVideo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video, menu);
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
	
	public void loadVideo() {
		VideoView view = (VideoView) findViewById(R.id.video);
		new VideoTask(channel,view,this).execute();
	}
	

	/**
	 * Searches for and loads a video.
	 * @author Richard Shen
	 */
	private class VideoTask extends AsyncTask <Void, Void, Uri> {
		String channel;
		VideoView view;
		VideoActivity activity;
		
		/**
		 * Stores the activity which called this task.
		 * 
		 * @param activity The MatchActivity which called this task.
		 */
	    public VideoTask(String channel, VideoView view, VideoActivity activity) {
	    	this.channel = channel;
	    	this.view = view;
	    	this.activity = activity;
	    }


		@Override
		protected Uri doInBackground(Void... params) {	
			String[] st = getSigAndToken();
			String uri = getVideo(st);
			return(Uri.parse(uri.trim()));
		}
		
		/**
		 * Gets the sig and token for a channel.
		 * @return An array of strings, with the sig in the 0 index and the token in the 1 index.
		 */
		public String[] getSigAndToken() {
			try {
				String request = "http://api.twitch.tv/api/channels/"+channel+"/access_token";
				String error = "Channel not found.";
				String response = HttpHelp.httpRequest(request, error);
				JSONObject access = new JSONObject(response);
				String sig = access.getString("sig");
				String token = access.getString("token");
				String[] st = new String[2];
				st[0] = sig;
				st[1] = token;
				return st;
			} catch (JSONException e) {
				return null;
			}
		}
		
		/**
		 * Gets a twitch video's uri for a channel.
		 * @param st An array of strings, with the sig in the 0 index and the token in the 1 index.
		 * @return The video's uri as a string.
		 */
		public String getVideo(String[] st) {
			try {
				String sig = st[0];
				String token = URLEncoder.encode(st[1],"UTF-8");
				Random rnd = new Random();
				int random = 100000 + rnd.nextInt(900000);
				String request = "http://usher.twitch.tv/api/channel/hls/"+channel+".m3u8?player=twitchweb&&token="+token+"&sig="+sig+"&allow_source=true&type=any&p="+random;			
				String error = "Video not found.";
				String response = HttpHelp.httpRequest(request, error);
				response = response.substring(response.trim().lastIndexOf("\n"));
				return response;
			} catch (UnsupportedEncodingException e) {
				return e.getLocalizedMessage();
			}
		}
		
		protected void onPostExecute(Uri video) {
			if(video != null) {
				MediaController mediaController = new MediaController(activity);
		        mediaController.setAnchorView(view);
		        mediaController.setMediaPlayer(view);
				view.setVideoURI(video);
				view.requestFocus();
				view.start();
			}
		}
	}
}
