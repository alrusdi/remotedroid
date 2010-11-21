package com.joshsera;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;

/*
 * To-do
 * 
 * DNS lookup
 * arrow keys, esc, win key
 */

public class RemoteDroid extends Activity {
	private static final String TAG = "RemoteDroid";
	// menu item(s)
	public static final int MENU_PREFS = 0;
	public static final int MENU_HELP = 1;


	//
	private EditText tbIp;
	//
	private HelpDialog dlHelp;
	//
	private DiscoverThread discover;
	private Handler handler;
	private SimpleAdapter adapter;
	private Vector<String> hostlist;

	public RemoteDroid() {
		super();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // added to save screen space, the Title was shown twice, in Standard Android bar, then below in Bolder larger text, this gets rid of the standard android bar
		setContentView(R.layout.main);
		//
		this.handler = new Handler();
		// set some listeners
		Button but = (Button)this.findViewById(R.id.btnConnect);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onConnectButton();
			}
		});

		// check SharedPreferences for IP
		Settings.init(this.getApplicationContext());

		//
		this.tbIp = (EditText)this.findViewById(R.id.etIp);
		if (Settings.ip != null) {
			this.tbIp.setText(Settings.ip);
		}
		//
		if (this.dlHelp == null) {
			this.dlHelp = new HelpDialog(this);
		}
		// discover some servers
		this.hostlist = new Vector<String>();
		((ListView)this.findViewById(R.id.lvHosts)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView adapter, View v, int position, long id) {
				onHostClick(position);
			}
		});
	}

	private void updateHostList() {
		FoundHostsAdapter adapter = new FoundHostsAdapter(this.hostlist, this.getApplication());
		((ListView)this.findViewById(R.id.lvHosts)).setAdapter(adapter);
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
		this.discover = new DiscoverThread(new DiscoverThread.DiscoverListener() {
			public void onAddressReceived(String address) {
				hostlist.add(address);
				Log.d(TAG, "Got host back, "+address);
				handler.post(new Runnable() {
					public void run() {
						updateHostList();
					}
				});
			}
		});
		this.discover.start();
	}


	/** App goes into background */
	public void onPause() {
		super.onPause();
		this.discover.closeSocket();
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
				Intent i = new Intent(this, PadActivity.class);
				this.startActivity(i);
				this.finish();
			} catch (Exception ex) {
				//this.tvError.setText("Invalid IP address");
				//this.tvError.setVisibility(View.VISIBLE);
				Toast.makeText(this, this.getResources().getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
				Log.d(TAG, ex.toString());
			}
		} else {
			//this.tvError.setText("Invalid IP address");
			//this.tvError.setVisibility(View.VISIBLE);
			Toast.makeText(this, this.getResources().getText(R.string.toast_invalidIP), Toast.LENGTH_LONG).show();
		}
	}
	
	private void onHostClick(int item) {
		this.tbIp.setText(this.hostlist.get(item));
		this.onConnectButton();
	}

	private void onHelp() {
		this.dlHelp.show();
	}

	private void onPrefs() {
		Intent i = new Intent(RemoteDroid.this, PrefsActivity.class);
		this.startActivity(i);
	}
	
}
