package com.example.lolview;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StreamAdapter extends ArrayAdapter<JSONObject>{
	private final Context context;
	private final JSONObject[] values;

	public StreamAdapter(Context context, JSONObject[] values) {
	  super(context, R.layout.activity_stream, values);
	  this.context = context;
	  this.values = values;
	}
	
	/**
	 * Gets the stream name from a stream JSONObject.
	 * @param stream The JSONObject describing a stream.
	 * @return The stream name.
	 */
	public String getStreamName(JSONObject stream) {
		try {
			return stream.getJSONObject("channel").getString("display_name");
		} catch (JSONException e) {
			return e.getLocalizedMessage();
		}
	}
	
	/**
	 * Gets the stream's viewers from a stream JSONObject.
	 * @param stream The JSONObject describing a stream.
	 * @return The number of viewers.
	 */
	public String getStreamViewers(JSONObject stream) {
		try {
			return Integer.toString(stream.getInt("viewers"))+" viewers";
		} catch (JSONException e) {
			return e.getLocalizedMessage();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
	    
	    JSONObject stream = values[position];
	    
	    TextView textView = (TextView) rowView.findViewById(R.id.channel_name);
	    textView.setText(getStreamName(stream));

	    textView = (TextView) rowView.findViewById(R.id.stream_viewers);
	    textView.setText(getStreamViewers(stream));

	    return rowView;
	}
}
