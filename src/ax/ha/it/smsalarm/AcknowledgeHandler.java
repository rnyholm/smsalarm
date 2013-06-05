/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Responsible for the application <code>ax.ha.it.smsalarm</code> acknowledge activity.
 * This class allows users acknowledge an received alarm by calling to specific phone
 * number.<br>
 * Also holds the acknowledge UI.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.0
 * @since 1.1-SE
 * @date 2013-04-22
 */
public class AcknowledgeHandler extends Activity  {
	
    //Log tag string
    private final String LOG_TAG = "AcknowledgeHandler";
    
	// Objects needed for logging and shared preferences handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();    
		
  	/**
  	 * Overridden method to build up UI and set different listeners for the UI elements.
  	 * 
  	 * When activity starts, this method is the entry point.
  	 * The GUI is built up within this method.
  	 * 
  	 * @param savedInstanceState
  	 * @Override
  	 * 
  	 * @see #onPause()
  	 */  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ack);
        
        //Find some TextViews and the progressbar
        final TextView waitToAckTextView = (TextView)findViewById(R.id.waitToAck);
        final ProgressBar waitToAckProgressBar = (ProgressBar)findViewById(R.id.waitToAckProgressBar);
        final TextView waitToAckInfoTextView = (TextView)findViewById(R.id.waitToAckInfo);
        
        //Set the progress bar to indeterminate
        waitToAckProgressBar.setIndeterminate(true);
        
        //Set views visible)
        waitToAckTextView.setVisibility(View.GONE);
        waitToAckProgressBar.setVisibility(View.GONE);
        waitToAckInfoTextView.setVisibility(View.GONE);
        
//        //Shared preferences
//    	final SharedPreferences sharedPref = this.getSharedPreferences(SmsAlarm.SHARED_PREF, Context.MODE_PRIVATE);
//    	
//    	//Get full message as string
//    	String fullMessage = sharedPref.getString(SmsAlarm.FULL_MESSAGE_KEY, "");
//    	
//    	//Get acknowledge number
//    	final String ackNumber = sharedPref.getString(SmsAlarm.ACK_NUMBER_KEY, "");
        
        TextView messageTextView = (TextView)findViewById(R.id.fullAlarmMessage);
        
        //Set larm text message to ui
//        messageTextView.setText(fullMessage);
        
        //Find buttons and set the to button variables
        Button abortButton = (Button)findViewById(R.id.abortAlarm);
        Button ackButton = (Button)findViewById(R.id.acknowledgeAlarm);
        
        //Create objects that acts as listeners to the buttons
        abortButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				finish();
			}
		});
        
        //Create objects that acts as listeners to the buttons
        ackButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				//Check if user has given any number to call, else show toast
//				if(!ackNumber.equals("")) {
//			        //Set views visible
//			        waitToAckTextView.setVisibility(View.VISIBLE);
//			        waitToAckProgressBar.setVisibility(View.VISIBLE);
//			        waitToAckInfoTextView.setVisibility(View.VISIBLE);
//			        
//					//Set min and max value for "random" delay before placing the phone call
//					int min_delay = 1000;
//					int max_delay = 40000;
//					
//					//Create a random object, used to randomize a delay before placing the phone call
//					Random rndm = new Random();
//					
//					//Randomize delay
//					int delay = rndm.nextInt(max_delay-min_delay+1) + min_delay;
//					
//					//Create an intent object, this intent is to place a call. Also set some options
//					final Intent callIntent = new Intent(Intent.ACTION_CALL);
//		    		callIntent.setData(Uri.parse("tel:"+ackNumber));
//					
//					//Initialize a handler object, used to put a thread to sleep
//					Handler handler = new Handler();
//					handler.postDelayed(new Runnable() {
//						public void run() {
//							//Place the call after thread has been a sleep for a random time
//							startActivity(callIntent);
//						}
//					}, delay);
//
//				}	else {
//					Toast.makeText(AcknowledgeHandler.this, R.string.cannotAck, Toast.LENGTH_LONG).show();
//				}
			}
		});
    }
    
    /**
     * Method to handle onPause.
     * 
     * @see #onCreate(Bundle)
     */
    public void onPause(){
    	super.onPause(); 
    }
}