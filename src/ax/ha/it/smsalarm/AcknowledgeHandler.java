/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Responsible for the application <code>ax.ha.it.smsalarm</code> acknowledge activity.
 * This class allows users to acknowledge an received alarm by calling to specific phone
 * number.<br>
 * Also holds the acknowledge UI.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.0
 * @since 1.1-SE
 * @date 2013-06-20
 */
public class AcknowledgeHandler extends Activity  {
	
    //Log tag string
    private final String LOG_TAG = "AcknowledgeHandler";
    
	// Objects needed for logging and shared preferences handling
	private LogHandler logger = LogHandler.getInstance();
	private PreferencesHandler prefHandler = PreferencesHandler.getInstance();    
	
	// Variables of different UI elements and types
	// The TextView Objects
    private TextView titleTextView;
    private TextView fullMessageTextView; 
    private TextView lineBusyTextView;
    private TextView countDownTextView;
    private TextView secondsTextView;
    
    // The Button objects
    private Button acknowledgeButton;
    private Button abortButton;
    
    // The ProgressBar Object
    private ProgressBar redialProgressBar;
    
    // The ImageView Objects
    private ImageView divider1ImageView;
    private ImageView divider2ImageView;
		
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
        
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler started");
       
        // FindViews
        this.findViews();        

        
//        //Shared preferences
//    	final SharedPreferences sharedPref = this.getSharedPreferences(SmsAlarm.SHARED_PREF, Context.MODE_PRIVATE);
//    	
//    	//Get full message as string
//    	String fullMessage = sharedPref.getString(SmsAlarm.FULL_MESSAGE_KEY, "");
//    	
//    	//Get acknowledge number
//    	final String ackNumber = sharedPref.getString(SmsAlarm.ACK_NUMBER_KEY, "");
        
        
        //Set larm text message to ui
//        messageTextView.setText(fullMessage);
        
        
        //Create objects that acts as listeners to the buttons
        abortButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				finish();
			}
		});
        
        //Create objects that acts as listeners to the buttons
        acknowledgeButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// TODO: Implement functionality
			}
		});
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler completed");
    }
    
    /**
     * Method to handle onPause.
     * 
     * @see #onCreate(Bundle)
     */
    public void onPause(){
    	super.onPause(); 
    }
    
    /**
     * To find UI widgets and get their reference by ID stored in class variables.
     * 
     * @see #onCreate(Bundle)
  	 * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     */
    private void findViews() {
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":findViews()", "Start finding Views by their ID");
    	
    	// Declare and initialize variables of type TextView
    	this.titleTextView = (TextView)findViewById(R.id.ackTitle_tv);
    	this.fullMessageTextView = (TextView)findViewById(R.id.ackFullAlarm_tv);
    	this.lineBusyTextView = (TextView)findViewById(R.id.ackLineBusy_tv);
    	this.countDownTextView = (TextView)findViewById(R.id.ackCountdown_tv);
    	this.secondsTextView = (TextView)findViewById(R.id.ackSeconds_tv);
    	
    	// Declare and initialize variables of type Button
    	this.acknowledgeButton = (Button)findViewById(R.id.ackAcknowledgeAlarm_btn);
    	this.abortButton = (Button)findViewById(R.id.ackAbortAlarm_btn);
    	
    	// Declare and initialize variable of type ProgressBar
    	this.redialProgressBar = (ProgressBar)findViewById(R.id.ackRedial_pb);
    	
        // Declare and initialize variables of type ImageView
        this.divider1ImageView = (ImageView)findViewById(R.id.ackDivider1_iv);
        this.divider2ImageView = (ImageView)findViewById(R.id.ackDivider2_iv);
    	
        // If Android API level less then 11 set bright gradient else set dark gradient
        if(Build.VERSION.SDK_INT < 11) {
        	this.divider1ImageView.setImageResource(R.drawable.gradient_divider_10_and_down); 
        	this.divider2ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
        	// Logging
        	this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":findViews()", "API level < 11, set bright gradients");
        } else {
        	this.divider1ImageView.setImageResource(R.drawable.gradient_divider_11_and_up); 
        	this.divider2ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
        	this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":findViews()", "API level > 10, set dark gradients");
        } 
        
        // Log and hide UI widgets that user don't need to see right now
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":findViews()", "Hiding elements not needed to show right now");      
        this.lineBusyTextView.setVisibility(View.GONE);
        this.countDownTextView.setVisibility(View.GONE);
        this.secondsTextView.setVisibility(View.GONE);
        this.redialProgressBar.setVisibility(View.GONE);
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":findViews()", "All Views found");
    }
}