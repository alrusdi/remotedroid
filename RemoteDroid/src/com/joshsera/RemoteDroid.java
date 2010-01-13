package com.joshsera;

import java.util.*;
import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.widget.*;
import android.util.*;
import android.view.*;

/*
 * To-do
 * 
 * DNS lookup
 * arrow keys, esc, win key
 */

public class RemoteDroid extends Activity {
	// menu item(s)
	public static final int MENU_PREFS = 0;
	public static final int MENU_HELP = 1;


	//
	private EditText tbIp;
	private TextView tvError;
	//
	private HelpDialog dlHelp;
	private ListView mHostlist;

	public RemoteDroid() {
		super();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		// set some listeners
		Button but = (Button)this.findViewById(R.id.btnConnect);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onConnectButton();
			}
		});

		// check SharedPreferences for IP
		Settings.init(this.getApplicationContext());

		mHostlist = (ListView) findViewById(R.id.hostlist);
		populateHostList();

		//
		this.tbIp = (EditText)this.findViewById(R.id.tbIp);
		this.tvError = (TextView)this.findViewById(R.id.lbError);
		if (Settings.getIp() != null) {
			this.tbIp.setText(Settings.getIp());
		}
		//
		if (this.dlHelp == null) {
			this.dlHelp = new HelpDialog(this);
		}
		// goes to onStart
	}

	private void populateHostList() {
		// populates the host list with saved hosts from the settings
		LinkedList<String> ips = Settings.getSavedHosts();
		String[] from = new String[]{"hostip"};
		int[] to = new int[]{R.id.hostEntry};
		List<Map<String,String>> data = new ArrayList<Map<String, String>>();
		for (String s:ips){
			Map<String, String> map = new HashMap<String, String>();
			map.put("hostip", s);
			data.add(map);
		}

		/**
		 * in order to be able to hook up the onclick listeners for 
		 * the children of the simple adapter, we have to override the 
		 * getView() method to set the listeners we want, so callbacks
		 * can function correctly
		 */

		SimpleAdapter adapter = new SimpleAdapter(this,data,R.layout.savedhost,from,to){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View v = super.getView(position, convertView, parent);
				TextView text = (TextView) v.findViewById(R.id.hostEntry);
				final CharSequence str = text.getText();
				ImageButton b = (ImageButton) v.findViewById(R.id.hostbutton);

				// set the listener for clicking on the text
				text.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onSavedHost(str); 
					}
				});

				// set the listener for clicking on the delete button
				b.setOnClickListener(new View.OnClickListener() {     				
					@Override
					public void onClick(View v) { 
						onRemoveSavedHost(str);
					}
				});

				return v;
			}
		};

		mHostlist.setAdapter(adapter);

	}


	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
	}

	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}

	/** App kills anything it started */
	public void onStop() {
		super.onStop();
	}

	/** App starts displaying things */
	public void onResume() {
		super.onResume();
	}


	/** App goes into background */
	public void onPause() {
		super.onPause();
	}

	// menu

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//
		menu.add(0, MENU_PREFS, 0, R.string.txt_preferences).setShortcut('0', 'p').setIcon(R.drawable.icon_prefs);
		menu.add(0, MENU_HELP, 0, R.string.txt_help).setShortcut('1', 'h').setIcon(R.drawable.icon_help);
		//
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		//
		switch (item.getItemId()) {
		case MENU_PREFS:
			//
			this.onPrefs();
			break;
		case MENU_HELP:
			//
			this.onHelp();
			break;
		}
		//
		return super.onOptionsItemSelected(item);
	}

	//

	private void onConnectButton() {
		    	String ip = this.tbIp.getText().toString();
		    	if (ip.matches("^[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}$")) {
		    		try {
		    			Settings.setIp(ip);
		    			//
			    		this.tvError.setVisibility(View.INVISIBLE);
			    		Intent i = new Intent(this, PadActivity.class);
			    		this.startActivity(i);
			    		this.finish();
		    		} catch (Exception ex) {
		    			//this.tvError.setText("Invalid IP address");
		        		//this.tvError.setVisibility(View.VISIBLE);
		        		Toast.makeText(this, "Invalid IP address!", Toast.LENGTH_LONG).show();
		        		Log.d("pad", ex.toString());
		    		}
		    	} else {
		    		//this.tvError.setText("Invalid IP address");
		    		//this.tvError.setVisibility(View.VISIBLE);
		    		Toast.makeText(this, "Invalid IP address!", Toast.LENGTH_LONG).show();
		    	}
		    	//
		    	
		


	}


	private void onRemoveSavedHost(CharSequence str) {
		try {
			Settings.removeSavedHost(str); 
			populateHostList();
			// TODO: we should be able to just call _below_ to update the view
			// however, the DataSetObserver is locked onto a copy of the data,
			// not a reference (from Settings). This needs to be changed...
			//((SimpleAdapter)mHostlist.getAdapter()).notifyDataSetChanged();

		} catch (Exception e) {
			Log.d("remotedroid","couldnt remove "+str.toString()+" from list: "+e.toString());
		}

	}

	private void onSavedHost(CharSequence s) {
		try {
			tbIp.setText(s);
		} catch (Exception e) {
			Log.d("remotedroid",e.toString());
		}

	}

	private void onHelp() {
		this.dlHelp.show();
	}

	private void onPrefs() {
		Intent i = new Intent(this, PrefsActivity.class);
		this.startActivity(i);
		this.finish();
	}

}



