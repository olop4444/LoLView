package com.example.lolview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Spinner spinner = (Spinner) findViewById(R.id.region_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.regions, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	 * Searches for the match a summoner is in.
	 * 
	 * @param button The button pressed to call this function.
	 */
	public void searchMatch(View button) {
		//Gets search form data
		final EditText nameField = (EditText) findViewById(R.id.summonername);
		final Spinner regionField = (Spinner) findViewById(R.id.region_spinner);
		String name = nameField.getText().toString().trim();
		String region = regionField.getSelectedItem().toString();
		
		if(name.length() > 0) {
			Intent intent = new Intent(this,MatchActivity.class);
			intent.putExtra("Summoner", name);
			intent.putExtra("Region",region);
			startActivity(intent);
		}
		else {
			errorAlert("Please enter a summoner name.");
		}
	}
	
	/**
	 * Opens the list of streams.
	 * 
	 * @param button The button pressed to call this function.
	 */
	public void openStreamList(View button) {	
		Intent intent = new Intent(this,StreamActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Creates an error alert.
	 * 
	 * @param message The error message displayed.
	 */
	public void errorAlert(String message) {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
	}
	
	//Exits from the activity.
	public void exit() {
		finish();
	}
}
