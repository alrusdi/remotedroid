package com.joshsera;

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
    			this.tvError.setText("Invalid IP address");
        		this.tvError.setVisibility(View.VISIBLE);
        		Log.d("pad", ex.toString());
    		}
    	} else {
    		this.tvError.setText("Invalid IP address");
    		this.tvError.setVisibility(View.VISIBLE);
    	}
    	//
    	
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