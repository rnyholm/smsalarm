/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
	// Enumeration for different datatypes needed when retrieving shared preferences
	private enum Datatypes {
		INTEGER,
		STRING,
		BOOLEAN,
		LIST;
	}
	
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
    
    // Strings for the data presentation in UI
    private String rescueService = "";
    private String fullMessage = "";
    
    // String represinting phone number to which we acknowledge to
    private String AcknowledgeNumber = "";
		
 	/**
  	 * When activity starts, this method is the entry point.
  	 * The User Interface is built up and different <code>Listeners</code>
  	 * are set within this method.
  	 * 
  	 * @param savedInstanceState Default Bundle
  	 * 
  	 * @see #findViews()
  	 * @see #getAckHandlerPrefs()
  	 * @see #onPause()
  	 * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
  	 * 
  	 * @Override
  	 */   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ack);
        
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler started");
       
        // FindViews
        this.findViews();  
        
        // Get Shared Preferences
        this.getAckHandlerPrefs();
        
        // Set TextViews
        this.setTextViews();        
        
        //Create objects that acts as listeners to the buttons
        abortButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().abortButton.OnClickListener().onClick()", "Abort button has been pressed, finishing activity");
				finish();
			}
		});
        
        //Create objects that acts as listeners to the buttons
        acknowledgeButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Check to see if we have any phone number to acknowledge to
				if(!AcknowledgeNumber.equals("")) {
					// Logging
					logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed and phone number to acknowledge to exist. Continue acknowledge");
					// TODO: Implement functionality
				} else {
					Toast.makeText(AcknowledgeHandler.this, R.string.cannotAck, Toast.LENGTH_LONG).show();
					// Logging
					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":onCreate().acknowledgeButton.OnClickListener().onClick()", "Acknowledge button has been pressed but no phone number to acknowledge to has been given");
				}			
			}
		});
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of the Acknowledge Handler completed");
    }
    
    /**
     * To handle events to trigger when activity pauses.
     * <b><i>Not yet implemented.</i></b>
     * 
     * @see #onCreate(Bundle)
     * 
     * @Override
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
    
    /**
     * To get <code>Shared Preferences</code> used by class <code>AcknowledgeHandler</code>.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String, String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.getPrefs(String, String, int, Context)   
     */
	private void getAckHandlerPrefs() {
    	//Some logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getAckHandlerPrefs()", "Start retrieving shared preferences needed by class AcknowledgeHandler");
    	
    	//Get shared preferences needed by class Acknowledge Handler
    	this.rescueService = (String) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getRESCUE_SERVICE_KEY(), Datatypes.STRING.ordinal(), this);
    	this.fullMessage = (String) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getFULL_MESSAGE_KEY(), Datatypes.STRING.ordinal(), this);
    	this.AcknowledgeNumber = (String) this.prefHandler.getPrefs(this.prefHandler.getSHARED_PREF(), this.prefHandler.getACK_NUMBER_KEY(), Datatypes.STRING.ordinal(), this);

    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getAckHandlerPrefs()", "Shared preferences retrieved");  	 
    }
	
	/**
	 * To set <code>TextViews</code> with data for a proper presentation of the UI.
	 * 
	 * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String, String)
	 */
	private void setTextViews() {
    	//Some logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":setTextViews()", "Setting TextViews with proper data");
		// Set TextViews from variables and resources
		this.titleTextView.setText(this.rescueService.toUpperCase() + " " + getResources().getString(R.string.fireAlarm));
		this.fullMessageTextView.setText(this.fullMessage);
	}
}