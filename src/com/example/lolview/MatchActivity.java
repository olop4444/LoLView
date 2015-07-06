package com.example.lolview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MatchActivity extends ActionBarActivity {
	private final static String api_key = "5ebf9d89-fc7f-416a-9915-ac571eb0809f";
	private final static String ddragon_version = "5.2.1";
	private final static String staticData_version = "1.2";
	private final static String summoner_version = "1.4";
	private final static String game_version = "1.3";
	private final static String league_version = "2.5";
	private final static String match_version = "2.2";
	protected static HashMap<String,String> regionMap;
	static int nextId = 1234; //Arbitrary semi-large number for match history image ids.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_match);
		Intent intent = getIntent();
		String summoner = intent.getStringExtra("Summoner");
		String region = intent.getStringExtra("Region");
		setTitle(summoner);
		initializeMap();
		callMatch(summoner,region);
	}
	
	/**
	 * Initializes the regionMap variable, which holds a mapping from region ID to platform ID.
	 */
	private void initializeMap() {
		regionMap = new HashMap<String,String>(10);
		regionMap.put("NA", "NA1");
		regionMap.put("BR","BR1");
		regionMap.put("EUNE", "EUN1");
		regionMap.put("EUW", "EUW1");
		regionMap.put("KR", "KR");
		regionMap.put("LAN", "LA1");
		regionMap.put("LAS", "LA2");
		regionMap.put("OCE", "OC1");
		regionMap.put("RU", "RU");
		regionMap.put("TR", "TR1");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.match, menu);
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
	 * Executes an search for a match.
	 * 
	 * @param summoner The summoner name whose match to search for.
	 * @param region The region to search in.
	 */
	public void callMatch(String summoner, String region) {
		new MatchSearch(this).execute(region,summoner);
		new HistorySearch(this).execute(region,summoner);
	}
	
	/**
	 * Toggles the match history display.
	 * 
	 * @param button The button pressed to call this function.
	 */
	public void toggleHistory(View button) {
		LinearLayout history = (LinearLayout) findViewById(R.id.matchhistory);
		if(history.getVisibility() == View.VISIBLE)
			history.setVisibility(View.GONE);
		else
			history.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Adds a TextView to the end of a LinearLayout.
	 * 
	 * @param text The text to have in the view, if any.
	 * @param layoutId The LinearLayout to add the view to.
	 * @param index The index in the layout to add the view to. Use -1 if you wish to add to the end of the layout.
	 * @param activity The match activity in which the layout resides.
	 * @return The TextView created.
	 */
	public TextView addTextView(String text, int layoutId, int index, MatchActivity activity) {
		TextView textView = new TextView(activity);
	    textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
	    textView.setText(text);
	    LinearLayout linearLayout = (LinearLayout) findViewById(layoutId);
	    if(index != -1)
	    	linearLayout.addView(textView,index);
	    else
	    	linearLayout.addView(textView);
	    
	    return textView;
	}
	
	/**
	 * Creates an ImageView with a specified image.
	 * 
	 * @param imageURL The URL of the image in the ImageView.
	 * @param activity The match activity in which the layout resides.
	 * @return The ImageView created.
	 */
	public ImageView createImageView(String imageURL, MatchActivity activity) {
		ImageView imageView = new ImageView(activity);
	    imageView.setId(nextId++);
	    imageView.setImageDrawable(getDrawable(imageURL,3));
	    return imageView;
	}
    
	/**
	 * Converts a number of seconds into a readable time.
	 * 
	 * @param seconds
	 * @return The number of seconds in hours, minutes, and seconds.
	 */
	public String timeConversion(int seconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    int minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    int hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;
	    
	    return hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
	}
	
	/**
	 * Gets the summoner spell name associated with a spell ID.
	 * 
	 * @param id The id of the summoner spell.
	 * @return The summoner spell's name.
	 */
	public String getSpellName(int id) {
		String request = "https://global.api.pvp.net/api/lol/static-data/na/v"+staticData_version+"/summoner-spell/"+id+"?api_key="+api_key;
		String error = "Invalid spell id.";	
		String spelldata = HttpHelp.httpRequest(request,error);
		
		if(spelldata.charAt(0) != '!') {
			try {
				JSONObject spell = new JSONObject(spelldata);
				return spell.getString("key");
			} catch (JSONException e) {
				return e.getLocalizedMessage();
			}
		}
		return spelldata;
	}
	
	/**
	 * Gets the champion name associated with a champion ID.
	 * 
	 * @param id The id of the champion.
	 * @return The champion's name.
	 */
	public String getChampionName(int id) {
		String request = "https://global.api.pvp.net/api/lol/static-data/na/v"+staticData_version+"/champion/"+id+"?api_key="+api_key;
		String error = "Invalid champion id.";				
		String champdata = HttpHelp.httpRequest(request,error);
		
		if(champdata.charAt(0) != '!') {
			try {
				JSONObject champion = new JSONObject(champdata);
				return champion.getString("key");
			} catch (JSONException e) {
				return e.getLocalizedMessage();
			}
		}
		return champdata;
	}
	
	/**
	 * Gets the image url associated with a summoner spell name.
	 * 
	 * @param spell The name of the summoner spell.
	 * @return The url of the summoner spell icon.
	 */
	public String getSpellIcon(String spell) {
		return "http://ddragon.leagueoflegends.com/cdn/"+ddragon_version+"/img/spell/"+spell+".png";
	}
	
	/**
	 * Gets the image url associated with a champion name.
	 * 
	 * @param champ The name of the champion.
	 * @return The url of the champion icon.
	 */
	public String getChampionIcon(String champ) {
		return "http://ddragon.leagueoflegends.com/cdn/"+ddragon_version+"/img/champion/"+champ+".png";
	}
	
	/**
	 * Gets the image url associated with an item ID.
	 * 
	 * @param id The id of the item.
	 * @return The url of the item icon.
	 */
	public String getItemIcon(int id) {
		if(id != 0 && id != 3285)
			return "http://ddragon.leagueoflegends.com/cdn/"+ddragon_version+"/img/item/"+id+".png";
		return null;
	}
	
	public BitmapDrawable getDrawable(String url, int scale) {
        try {
        	Bitmap x;

        	if(url != null) {
		        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        x = BitmapFactory.decodeStream(input);
        	}
        	else {
	            AssetManager manager = getAssets();
	            InputStream open = null;
	            open = manager.open("Item_Empty.png");
	            x = BitmapFactory.decodeStream(open);
	        }

	        x = Bitmap.createScaledBitmap(x,x.getWidth()/scale,x.getHeight()/scale,false);
	        return new BitmapDrawable(getResources(),x);
        } catch (IOException e) {
        	return null;
        }
	}
	
	////////////////////////////////////////////////////////////////////////
	//Private class begin
	
	/**
	 * Executes the search for a match.
	 * @author Richard Shen
	 */
	private class MatchSearch extends AsyncTask <String, Void, String> {
		
		MatchActivity activity;
		String[] viewStrings;
		BitmapDrawable[] champIcons;
		
		/**
		 * Stores the activity which called this task.
		 * 
		 * @param activity The MatchActivity which called this task.
		 */
	    public MatchSearch(MatchActivity activity) {
	        this.activity = activity;
	    }


		@Override
		protected String doInBackground(String... params) {
			String region = params[0];
	    	String sumName = params[1];
			String request = "https://"+region+".api.pvp.net/api/lol/"+region+"/v"+summoner_version+"/summoner/by-name/"+sumName+"?api_key="+api_key;
			request = request.replace(" ","%20");
			String error = "No summoner found.";
			
			String summoner = HttpHelp.httpRequest(request,error);
			if(summoner.charAt(0) != '!') {
				String match = findMatch(summoner,params[0]);
				try {
					JSONObject matchJSON = new JSONObject(match);
					return analyzeMatch(matchJSON);
				} catch (JSONException e) {
					return match;
				}
			}
			
			return summoner;
		}
		
		/**
		 * Searches for the current match of a summoner.
		 * 
		 * @param summonerinfo A JSON string for a summoner's info.
		 * @param region The region to search in.
		 * @return A JSON string for the match, or an error message if the summoner is not in game.
		 */
		private String findMatch(String summonerinfo, String region) {
			String id = null;
			try {
				JSONObject obj = new JSONObject(summonerinfo);
				id = obj.getJSONObject(obj.keys().next()).getString("id");
			} catch (JSONException e) {
				return e.getLocalizedMessage();
			}
			if(id != null) {
				String params = regionMap.get(region)+"/"+id;
				String request = "https://"+region+".api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/"+params+"?api_key="+api_key;
				String error = "Summoner is not in game.";
				return HttpHelp.httpRequest(request,error);
			}
			return "ID not found.";
		}
		
		/**
		 * Analyzes a match for display.
		 * 
		 * @param match The JSONObject for the match.
		 */
		private String analyzeMatch(JSONObject match) {		
			try {
				JSONArray players = match.getJSONArray("participants");
				int versus = 0; //Has the versus string been placed in the viewStrings array yet?
				viewStrings = new String[players.length()+1];
				champIcons = new BitmapDrawable[players.length()+1];
				for (int i = 0; i < players.length(); i++) {
					JSONObject player = players.getJSONObject(i);
					String summonerName = player.getString("summonerName");
					int champId = (int)player.getLong("championId");
					String champName = getChampionName(champId);
					int team = (int)player.getLong("teamId");
					if(team == 200 && versus == 0) {
						viewStrings[i] = "versus";
						versus = 1;
					}
				    viewStrings[i+versus] = summonerName;
				    champIcons[i+versus] = getDrawable(getChampionIcon(champName),4);
				}
				String mode = match.getString("gameMode");
				int length = (int) match.getLong("gameLength");
				String time = timeConversion(length);
				return mode+"\nGame Time: "+time;
			} catch (JSONException e) {	
				return e.getLocalizedMessage();
			}
		}
		
		protected void onPostExecute(String results) {
			TextView matchView = (TextView)findViewById(R.id.matchview);
			if(results.charAt(0) == '!')
				matchView.setText(results.substring(1));
			else {
				matchView.setText(results);	
				int layoutId = R.id.matchactivity;
				for(int i = 0; i < viewStrings.length; i++) {
					TextView addedView = addTextView(viewStrings[i],layoutId,1+i,activity);
					if(viewStrings[i] != "versus") {
						addedView.setCompoundDrawablesWithIntrinsicBounds(champIcons[i], null, null, null);
				    	addedView.setCompoundDrawablePadding(5);
					}
				}
			}
		}
	}
	
	//End private class
	////////////////////////////////////////////////////////////////////////
	//Second private class
	private class HistorySearch extends AsyncTask<String,Void,JSONArray> {
		
		MatchActivity activity;
		int totalKills = 0;
		int totalDeaths = 0;
		int totalAssists = 0;
		int totalChampDamage = 0;
		int totalGold = 0;
		int totalTime = 0;
		int totalGames = 0;
		String id;
		String region;
		int[] matchIds;
	
		/**
		 * Stores the activity which called this task.
		 * 
		 * @param activity The MatchActivity which called this task.
		 */
	    public HistorySearch(MatchActivity activity) {
	        this.activity = activity;
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy); 
	    }
	    
	    protected JSONArray doInBackground(String... params) {
	    	region = params[0];
	    	String sumName = params[1];
			String request = "https://"+region+".api.pvp.net/api/lol/"+region+"/v"+summoner_version+"/summoner/by-name/"+sumName+"?api_key="+api_key;
			request = request.replace(" ","%20");
			String error = "No summoner found.";
			
			String summoner = HttpHelp.httpRequest(request,error);
			if(summoner.charAt(0) != '!') {
				return findHistory(summoner);
			}
	    	return null;
	    }
	    
	    /**
		 * Searches for a summoner's match history.
		 * 
		 * @param summonerinfo A JSON string for a summoner's info.
		 * @return A JSON string for the match, or an error message if the summoner is not in game.
		 */
		private JSONArray findHistory(String summonerinfo) {
			id = null;
			try {
				JSONObject obj = new JSONObject(summonerinfo);
				id = obj.getJSONObject(obj.keys().next()).getString("id");
			} catch (JSONException e) {
				Log.e("summonerInfo",e.getLocalizedMessage());
				return null;
			}
			if(id != null) {
				String request = "https://"+region+".api.pvp.net/api/lol/"+region.toLowerCase(Locale.US)+"/v"+game_version+"/game/by-summoner/"+id+"/recent?api_key="+api_key;
				String error = "Summoner not found.";
				String history = HttpHelp.httpRequest(request,error);
				try {
					JSONObject obj = new JSONObject(history);
					return obj.getJSONArray("games");
				} catch (JSONException e) {
					Log.e("HistoryJSON",request);
					return null;
				}
			}
			return null;
		}
	    
	    protected void onPostExecute(JSONArray history) {
	    	if(history != null) {
    			try {
    				matchIds = new int[history.length()];
    				for(int i = 0; i < history.length(); i++) {
	    				JSONObject match = history.getJSONObject(i);
	    				addMatch(match,i);
	    			}
	    		
	    			createStats();
    			}
    			catch(JSONException e) {
    				Log.e("execute",e.getLocalizedMessage());
    			}
	    	}
	    }
	    
	    /**
	     * Generates all stats for a player's match history and displays them.
	     */
	    public void createStats() {
    		int layout = R.id.matchhistory;
    		
			String tier = getRank(id);
			addTextView(tier,layout,-1,activity).setPadding(0, 10, 0, 0);
			
			String kda = getAverageKDA();
	    	addTextView(kda,layout,-1,activity);
			
	    	String averageDamage = getAverageDamage();
			addTextView(averageDamage,layout,-1,activity);	

	    	String averageDPM = getAverageDPM();
			addTextView(averageDPM,layout,-1,activity);		

	    	String averageGold = getAverageGold();
			addTextView(averageGold,layout,-1,activity);		

	    	String averageGPM = getAverageGPM();
			addTextView(averageGPM,layout,-1,activity);
	    }
	    
	    /**
	     * Grabs a player's KDA from a match's stats.
	     * 
	     * @param stats The JSONObject containing the stats.
	     * @return A string with the player's k/d/a.
	     */
	    public String getKDA(JSONObject stats) {
	    	try {
				int kills, deaths, assists;
				kills = deaths = assists = 0;
				if(stats.has("championsKilled"))
					kills = (int)stats.getLong("championsKilled");
				if(stats.has("numDeaths"))
					deaths = (int)stats.getLong("numDeaths");
				if(stats.has("assists"))
					assists = (int)stats.getLong("assists");
				totalKills+=kills;
				totalDeaths+=deaths;
				totalAssists+=assists;
				String kda = kills+"/"+deaths+"/"+assists;
		    	return kda;
	    	} catch (JSONException e) {
	    		return e.getLocalizedMessage();
	    	}
	    }
	    
	    /**
	     * Calculates the average KDA of a player based on their match history.
	     * 
	     * @return A string containing the average KDA of the player.
	     */
	    public String getAverageKDA() {
	    	DecimalFormat df = new DecimalFormat("#.#");
	    	String k = df.format((double)totalKills/totalGames);
	    	String d = df.format((double)totalDeaths/totalGames);
	    	String a = df.format((double)totalAssists/totalGames);
	    	return "Average K/D/A: "+k+"/"+d+"/"+a;
	    }
	    
	    /**
	     * Calculates the average champion damage of a player based on their match history.
	     * 
	     * @return A string containing the average champion damage of the player.
	     */
	    public String getAverageDamage() {
	    	return "Average champion damage: "+totalChampDamage/totalGames;
	    }
	    
	    /**
	     * Calculates the average champion damage per minute of a player based on their match history.
	     * 
	     * @return A string containing the average champion damage per minute of the player.
	     */
	    public String getAverageDPM() {
	    	DecimalFormat df = new DecimalFormat("#.#");
	    	String dpm = df.format((double)totalChampDamage/totalTime*60);
	    	return "Average champion damage per minute: "+dpm;
	    }
	    
	    /**
	     * Calculates the average gold earned of a player based on their match history.
	     * 
	     * @return A string containing the average gold earned of the player.
	     */
	    public String getAverageGold() {
	    	return "Average gold earned: "+totalGold/totalGames;
	    }
	    
	    /**
	     * Calculates the average gold per minute of a player based on their match history.
	     * 
	     * @return A string containing the average gold per minute of the player.
	     */
	    public String getAverageGPM() {
	    	DecimalFormat df = new DecimalFormat("#.#");
	    	String gpm = df.format((double)totalGold/totalTime*60);
	    	return "Average gold per minute: "+gpm;
	    }
	    
	    /**
	     * Grabs any other stats from each game and adds them to the data.
	     * 
	     * @param stats The JSONObject holding the game's stats.
	     */
	    public void updateStats(JSONObject stats) {
	    	try {
	    		int damage = 0;
	    		if(stats.has("totalDamageDealtToChampions")) 
	    			damage = (int)stats.getLong("totalDamageDealtToChampions");
	    		totalChampDamage += damage;
	    		
	    		int gold = 0;
	    		if(stats.has("goldEarned")) 
	    			gold = (int)stats.getLong("goldEarned");
	    		totalGold += gold;
	    		
	    		int time = 0;
	    		if(stats.has("timePlayed")) 
	    			time = (int)stats.getLong("timePlayed");
	    		totalTime += time;
	    		
				totalGames++;
	    	} catch (JSONException e) {
	    		return;
	    	}
	    }
	    
	    /**
	     * Determines the solo rank of a player.
	     * 
	     * @param id The summoner id to search for.
	     * @return The summoner's ranked tier as a string, or UNRANKED if no tier.
	     */
	    public String getRank(String id) {
	    	try {
	    		String rank = "Current rank: ";
	    		String request = "https://"+region+".api.pvp.net/api/lol/"+region.toLowerCase(Locale.US)+"/v"+league_version+"/league/by-summoner/"+id+"/entry?api_key="+api_key;
				String response = HttpHelp.httpRequest(request,"");
				JSONObject JSONresponse = new JSONObject(response);
	    		JSONArray leagues = JSONresponse.getJSONArray(id);
	    		for(int i = 0; i < leagues.length(); i++) {
	    			JSONObject league = leagues.getJSONObject(i);
	    			if(league.getString("queue").equals("RANKED_SOLO_5x5")) 
	    				return rank+league.getString("tier");
	    		}
	    		return rank+"UNRANKED";
	    	} catch (JSONException e) {
	    		return e.getLocalizedMessage();
	    	}
	    }
	    
	    /**
	     * Gets a list of item URLs.
	     * 
	     * @param stats The JSONObject containing the items.
	     * @return An array of URLs corresponding to each item's image URL.
	     */
	    public String[] getItems(JSONObject stats) {
	    	try {
		    	String[] itemIcons = new String[7];
				for(int item = 0; item < 7; item++) {
					if(stats.has("item"+item)) {
						int itemId = (int) stats.getLong("item"+item);
						itemIcons[item] = getItemIcon(itemId);
					}
					else
						itemIcons[item] = null;
				}
				return itemIcons;
		    } catch (JSONException e) {
	    		return null;
	    	}
	    }
	    
	    /**
	     * Gets the game info of a match.
	     * 
	     * @param stats The stats of the match.
	     * @param gameMode The game mode of the match.	
	     * @return The game mode, duration, and result in a string.
	     */
	    public String getGameInfo(JSONObject stats, String gameMode) {
	    	try {
				boolean winner = stats.getBoolean("win");
				String gameTime = timeConversion((int)stats.getLong("timePlayed"));
				String gameResult = (winner) ? "Win" : "Loss";
				String info = gameMode+"\nDuration: "+gameTime+"\n"+gameResult;
				return info;
	    	} catch (JSONException e) {
	    		return e.getLocalizedMessage();
	    	}
	    }
	    
	    /**
	     * Gets the MatchInfo for a given match ID.
	     * 
	     * @param matchId The match ID.
	     * @return A JSONObject containing the match info.
	     */
	    public JSONObject getMatchInfo(int matchId) {
	    	String request = "https://"+region+".api.pvp.net/api/lol/"+region.toLowerCase(Locale.US)+"/v"+match_version+"/match/"+matchId+"?api_key="+api_key;
			String response = HttpHelp.httpRequest(request,"");
			try {
				JSONObject JSONresponse = new JSONObject(response);
				return JSONresponse;
			} catch (JSONException e) {
				return null;
			}
	    }
	    
	    /**
	     * Parses a subType string and returns the game mode.
	     * 
	     * @param subType The subtype of a match.
	     * @return The game mode.
	     */
	    public String parseGameMode(String subType) {
	    	if(subType.equals("NONE"))
	    		return "CUSTOM";
	    	if(subType.equals("ARAM_UNRANKED_5x5"))
	    		return "ARAM";
	    	if(subType.equals("COUNTER_PICK"))
	    		return "NEMESIS DRAFT";
	    	if(subType.equals("ONEFORALL_5x5"))
	    		return "ONE FOR ALL";
	    	if(subType.equals("CAP_5x5"))
	    		return "DOMINION";
	    	
	    	subType = subType.replace("_"," ");
	    	subType = subType.replace("x","v");
	    	return subType;
	    }
		
		/**
		 * Sets up a collection of views which show a match.
		 * 
		 * @param match The JSONObject of the match.
		 */
		public void addMatch(JSONObject match, int gameNum) {
			try {
				String[] spellIcons = new String[2];
				JSONObject stats = match.getJSONObject("stats");
				int champId = (int)match.getLong("championId");
				int spell1Id = (int)match.getLong("spell1");
				int spell2Id = (int)match.getLong("spell2");
				String champName = getChampionName(champId);
				String spell1Name = getSpellName(spell1Id);
				String spell2Name = getSpellName(spell2Id);
				String champIcon = getChampionIcon(champName);
				spellIcons[0] = getSpellIcon(spell1Name);
				spellIcons[1] = getSpellIcon(spell2Name);
				String kda = getKDA(stats);
				String[] itemIcons = getItems(stats);
				String gameType = parseGameMode(match.getString("subType"));
				String info = getGameInfo(stats,gameType);
				String score = kda + "\nLvl: " + stats.getLong("level");
				matchIds[gameNum] = (int)match.getLong("gameId");
				
				updateStats(stats);
				RelativeLayout matchView = createViews(info,champIcon,score,spellIcons,itemIcons);
				LinearLayout main = (LinearLayout) findViewById(R.id.matchhistory);
				main.addView(matchView);
				
			} catch (JSONException e) {
				Log.e("addMatch",e.getLocalizedMessage());				
			}
		}
		
		/**
		 * Creates a RelativeLayout holding item and spell icons for match history use.
		 * 
		 * @param items The array of item image urls.
		 * @param spells The array of spell image urls.
		 * @return The RelativeLayout with images inside.
		 */
		public RelativeLayout generateRightImages(String[] items, String[] spells) {
			RelativeLayout container = new RelativeLayout(activity);
			
			RelativeLayout.LayoutParams params;
			
			ImageView item2Image = createImageView(items[2],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			container.addView(item2Image,params);
			
			ImageView item1Image = createImageView(items[1],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item2Image.getId());
			container.addView(item1Image,params);		

			ImageView item0Image = createImageView(items[0],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item1Image.getId());
			container.addView(item0Image,params);
			
			ImageView item5Image = createImageView(items[5],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.BELOW,item2Image.getId());
			container.addView(item5Image,params);
			
			ImageView item4Image = createImageView(items[4],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item5Image.getId());
			params.addRule(RelativeLayout.BELOW,item1Image.getId());
			container.addView(item4Image,params);
			
			ImageView item3Image = createImageView(items[3],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item4Image.getId());
			params.addRule(RelativeLayout.BELOW,item0Image.getId());
			container.addView(item3Image,params);
			
			ImageView item6Image = createImageView(items[6],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item0Image.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			item6Image.setPadding(0, 0, 5, 0);
			container.addView(item6Image,params);
			
			ImageView spell1Image = createImageView(spells[0],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item6Image.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			spell1Image.setPadding(0, 0, 10, 0);
			container.addView(spell1Image,params);
			
			ImageView spell2Image = createImageView(spells[1],activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF,item6Image.getId());
			params.addRule(RelativeLayout.BELOW,spell1Image.getId());
			spell2Image.setPadding(0, 0, 10, 0);
			container.addView(spell2Image,params);
			
			return container;
		}
		
		/**
		 * Adds the match info, champion icon, and score to the main RelativeLayout.
		 * 
		 * @param info The match info.
		 * @param champIcon URL of the champion icon.
		 * @param score Score of the player.
		 * @param container RelativeLayout to add to.
		 */
		public void addLeftViews(String info, String champIcon, String score, RelativeLayout container) {
			RelativeLayout.LayoutParams params;
			
			TextView game = new TextView(activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		    game.setId(nextId++);
		    game.setText(info);
		    game.setTextSize(10);
		    game.setPadding(0, 0, 10, 0);
			container.addView(game,params);
			
			ImageView champImage = createImageView(champIcon,activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.RIGHT_OF,game.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			champImage.setPadding(0, 0, 10, 0);
			container.addView(champImage,params);
			
			TextView scoreView = new TextView(activity);
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.RIGHT_OF,champImage.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		    scoreView.setId(nextId++);
		    scoreView.setText(score);
		    scoreView.setTextSize(11);
			container.addView(scoreView,params);
		}
		
		/**
		 * Creates a view for showing a player's recent match.
		 * 
		 * @param info Any information about a match (gametype, duration, win/loss).
		 * @param champIcon The URL of the champion icon.
		 * @param kda The k/d/a of the player.
		 * @param spellIcons The URLs for summoner spells.
		 * @param itemIcons The URLs for item icons.
		 */
		public RelativeLayout createViews(String info, String champIcon, String score, String[] spellIcons, String[] itemIcons) {
			RelativeLayout container = new RelativeLayout(activity);
			
			addLeftViews(info,champIcon,score,container);
			
			RelativeLayout rightLayout = generateRightImages(itemIcons,spellIcons);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			container.addView(rightLayout,params);
			
			RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			container.setBackgroundResource(R.drawable.border);
			container.setLayoutParams(containerParams);
			return container;
		}
		
	}
	//End private class
	////////////////////////////////////////////////////////////////////////
}
