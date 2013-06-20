/*
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */

package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Main activity to configure application. Also holds the main User Interface.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.1
 * @since 0.9beta
 * @date 2013-06-17
 *
 * @see #onCreate(Bundle)
 * @see #onPause()
 */
public class SmsAlarm extends Activity  {
	
	// Log tag string
	private final String LOG_TAG = "SmsAlarm";
	
	// Constants representing different types of input dialogs
	private final int PRIMARY = 0;
	private final int SECONDARY = 1;
	private final int ACKNOWLEDGE = 2;
	private final int RESCUESERVICE = 3;
	
	// Constants representing different datatypes used by class PreferencesHandler
	private final int INTEGER = 0;
	private final int STRING = 1;
	private final int BOOLEAN = 2;
	private final int LIST = 3;	

	// Objects needed for logging, shared preferences and noise handling
	private LogHandler logger = LogHandler.getInstance();
	private static final PreferencesHandler prefHandler = PreferencesHandler.getInstance();
	private NoiseHandler noiseHandler = NoiseHandler.getInstance();
	
	// Variables of different UI elements and types
	// The EdittextObjects
    private EditText primaryListenNumberEditText;
    private EditText selectedToneEditText;
    private EditText ackNumberEditText;
    private EditText rescueServiceEditText;
    
    // The Button objects
    private Button editPrimaryNumberButton;
    private Button addSecondaryNumberButton;
    private Button removeSecondaryNumberButton;
    private Button editMsgToneButton; 
    private Button listenMsgToneButton;
    private Button ackNumberButton;
    private Button editRescueServiceButton;
     
    // The CheckBox objects
    private CheckBox soundSettingCheckBox;
    private CheckBox enableAckCheckBox;
    private CheckBox playToneTwiceSettingCheckBox;
    private CheckBox enableSmsAlarmCheckBox;
    
    // The ImageView objects
    private ImageView divider1ImageView;
    private ImageView divider2ImageView;
    private ImageView divider3ImageView;
    
    // The Spinner objects
    private Spinner toneSpinner;
    private Spinner secondaryListenNumberSpinner;    
    
	// Strings to store different important numbers
    private String primaryListenNumber = "";
    private String acknowledgeNumber = "";
    
    // List of Strings containing secondaryListenNumbers
    private List<String> secondaryListenNumbers = new ArrayList<String>();
    private List<String> emptySecondaryListenNumbers = new ArrayList<String>(); //<-- A "dummy" list just containing one element, one string
    
    // String to store firedepartments name in
    private String rescueService = "";
     
    // Integer to store which tone id to be used
    private int primaryMessageToneId = 0;
    private int secondaryMessageToneId = 1;
    
    // Boolean variables to store whether to use OS soundsettings or not, and if acknowledge is enabled
    private boolean useOsSoundSettings = false;
    private boolean useAlarmAcknowledge = false;
    private boolean playToneTwice = false;
    private boolean enableSmsAlarm = true;
    
    // Integer holding spinners positions
    private int toneSpinnerPos = 0;
  	
  	/**
  	 * When activity starts, this method is the entry point.
  	 * The User Interface is built up and different <code>Listeners</code>
  	 * are set within this method.
  	 * 
  	 * @param savedInstanceState Default Bundle
  	 * 
  	 * @see #findViews()
  	 * @see #updateSelectedToneEditText()
  	 * @see #updateAcknowledgeWidgets()
  	 * @see #updateWholeUI()
  	 * @see #buildAndShowInputDialog(int)
  	 * @see #getSmsAlarmPrefs()
  	 * @see #buildAndShowDeleteSecondaryNumberDialog()
  	 * @see #buildAndShowToneDialog()
  	 * @see #onPause()
  	 * @see ax.ha.it.smsalarm#NoiseHandler.makeNoise(Context, int, boolean, boolean)
  	 * @see ax.ha.it.smsalarm#PreferencesHandler.setPrefs(String, String, Object, Context)
  	 * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
  	 * 
  	 * @Override
  	 */    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of Sms Alarm started");
        
        // Get sharedPreferences
        this.getSmsAlarmPrefs();
       
        // FindViews
        this.findViews();
         
	    // Fill tone spinner with values
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.spinnerValues, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    // Set adapter to tone spinner
	    this.toneSpinner.setAdapter(adapter);  
         
        // Set listener to editPrimaryNumberButton
        this.editPrimaryNumberButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().editPrimaryNumberButton.OnClickListener().onClick()", "Edit PRIMARY listen number Button pressed");					
				// Build up and show input dialog of type primary number
				buildAndShowInputDialog(PRIMARY);
			}
		});
        
        // Set listener to addSecondaryNumberButton
        this.addSecondaryNumberButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().addSecondaryNumberButton.OnClickListener().onClick()", "Add SECONDARY listen number Button pressed");				
				// Build up and show input dialog of type secondary number
				buildAndShowInputDialog(SECONDARY);
			}
		});
        
        // Set listener to removeSecondaryNumberButton
        this.removeSecondaryNumberButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().removeSecondaryNumberButton.OnClickListener().onClick()", "Remove SECONDARY listen number Button pressed");
				
				// Only show delete dialog if secondary listen numbers exists, else show toast
				if(!secondaryListenNumbers.isEmpty()) {
					// Show alert dialog(prompt user for deleting number)
					buildAndShowDeleteSecondaryNumberDialog();
				} else {
					Toast.makeText(SmsAlarm.this, R.string.noSecondaryNumberExists, Toast.LENGTH_LONG).show();
					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":onCreate().removeSecondaryNumberButton.OnClickListener().onClick()", "Cannot build and show dialog because the list of SECONDARY listen numbers is empty so there is nothing to remove");
				}
			}
		});        
        
        // Set listener to ackNumberButton
        this.ackNumberButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().ackNumberButton.OnClickListener().onClick()", "Edit acknowledge number Button pressed");				
				// Build up and show input dialog of type acknowledge number
				buildAndShowInputDialog(ACKNOWLEDGE);
			}
		});
        
        // Set listener to editRescueServiceButton
        this.editRescueServiceButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().editRescueServiceButton.OnClickListener().onClick()", "Edit rescue service Button pressed");
				// Build up and show input dialog of type primary number
				buildAndShowInputDialog(RESCUESERVICE);
			}
		});         
        
        // Set listener to editMsgToneButton
        this.editMsgToneButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().editMsgToneButton.OnClickListener().onClick()", "Edit message tone Button pressed");
				// Build up and Show alert dialog(prompt for message tone)
				buildAndShowToneDialog();
			}
		});
        
        // Set listener to listenMsgToneButton
        this.listenMsgToneButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				// Play the correct tone and vibrate, depending on spinner value
				if(toneSpinnerPos == 0) {
					// Logging
					logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Listen message tone Button pressed. Message tone for PRIMARY alarm will be played");
					// Play message tone and vibrate
					noiseHandler.makeNoise(SmsAlarm.this, primaryMessageToneId, useOsSoundSettings, false);
					Intent noiseIntent = new Intent(SmsAlarm.this, NoiseHandler.class);
					startService(noiseIntent);
				} else if(toneSpinnerPos == 1) {
					logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Listen message tone Button pressed. Message tone for SECONDARY alarm will be played");
					noiseHandler.makeNoise(SmsAlarm.this, secondaryMessageToneId, useOsSoundSettings, false);
				} else {
		        	// DO NOTHING EXCEPT LOG ERROR MESSAGE
		        	logger.logCatTxt(logger.getERROR(), LOG_TAG + ":onCreate().listenMsgToneButton.OnClickListener().onClick()", "Invalid spinner position occurred. Current tone Spinner position is: \"" + Integer.toString(toneSpinnerPos) + "\"");
				}
			}
		});     
        
        // Set listener to soundSettingCheckBox
        this.soundSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		// Log that CheckBox been pressed
        		logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings CheckBox pressed(or CheckBox initialized)");	
        		
				// Set checkbox depending on it's checked status and store variable
				if(soundSettingCheckBox.isChecked()){
					// Store value to variable
					useOsSoundSettings = true;
					// logging
		    	    logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings CheckBox \"Checked\"(" + useOsSoundSettings + ")");					
				} else {
					useOsSoundSettings = false;
					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().soundSettingCheckBox.onCheckedChange()", "Use OS sound settings CheckBox \"Unchecked\"(" + useOsSoundSettings + ")");		
				}
				
				// Store value to shared preferences
		    	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getUSE_OS_SOUND_SETTINGS_KEY(), useOsSoundSettings, SmsAlarm.this); 
        	}
    	});
        
        // Set listener to enableAckCheckBox
        this.enableAckCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {      		
        		// Log that CheckBox been pressed
        		logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().enableAckCheckBox.onCheckedChange()", "Enable acknowledge CheckBox pressed(or CheckBox initialized)");	
        		
				// Set checkbox depending on it's checked status and store variable
				if(enableAckCheckBox.isChecked()){
					// Store value to variable
					 useAlarmAcknowledge = true;					 
				} else {
					 useAlarmAcknowledge = false;					 
				}
				
				// Store value to shared preferences
		      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_ACK_KEY(), useAlarmAcknowledge, SmsAlarm.this);
				
				// Update UI widgets affected by enable acknowledge
				updateAcknowledgeWidgets();
        	}
    	});
        
        // Set listener to playToneTwiceSettingCheckBox
        this.playToneTwiceSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		// Log that CheckBox been pressed
        		logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice CheckBox pressed(or CheckBox initialized)");	
        		
				// Set checkbox depending on it's checked status and store variable
				if(playToneTwiceSettingCheckBox.isChecked()){
					// Store value to variable
					playToneTwice = true;
					// Logging
		    		logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice CheckBox \"Checked\"(" + playToneTwice + ")"); 
				} else {
					 playToneTwice = false;
					 logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().playToneTwiceSettingCheckBox.onCheckedChange()", "Play tone twice CheckBox \"Unhecked\"(" + playToneTwice + ")"); 
				}
				
				// Store value to shared preferences
		      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPLAY_TONE_TWICE_KEY(), playToneTwice, SmsAlarm.this);  
        	}
    	});
        
        // Set listener to enableSmsAlarmCheckBox
        this.enableSmsAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		// Log that CheckBox been pressed
        		logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange(", "Enable Sms Alarm CheckBox pressed(or CheckBox initialized)");	
        		
				//Set checkbox depending on it's checked status and store variable
				if(enableSmsAlarmCheckBox.isChecked()){
					// Store value to variable
					enableSmsAlarm = true;
					// Logging
		    		logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm CheckBox \"Checked\"(" + enableSmsAlarm + ")"); 
				} else {
					enableSmsAlarm = false;
		    		logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":onCreate().enableSmsAlarmCheckBox.onCheckedChange()", "Enable SmsAlarm CheckBox \"Unchecked\"(" + enableSmsAlarm + ")"); 
				}
				
				// Store value to shared preferences
		      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_SMS_ALARM_KEY(), enableSmsAlarm, SmsAlarm.this);
        	}

    	});        
        
        // Set listener to tone spinner
		this.toneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent,View view,int pos, long id) {	
		        // Store tone spinners position to class variable
		        toneSpinnerPos = toneSpinner.getSelectedItemPosition();
				// Logging
				logger.logCatTxt(logger.getINFO(), LOG_TAG + ":onCreate().toneSpinner.OnItemSelectedListener().onItemSelected()", "Item in tone Spinner pressed(or Spinner initialized)");		        
		        // Update selected tone EditText widget
		        updateSelectedToneEditText();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub			
			}
		});
            
        // Update all UI widgets
        this.updateWholeUI();      
        
        // Log in debugging and information purpose
        this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreate()", "Creation of Sms Alarm completed");        
    }
    
    /**
     * To handle events to trigger when activity pauses.
     * <b><i>Not yet implemented.</i></b>
     * 
     * @see #onCreate(Bundle)
     * 
     * @Override
     */
    @Override
    public void onPause(){
    	super.onPause(); 
    	// DO NOTHING!
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	this.logger.logCatTxt(this.logger.getWARN(), this.LOG_TAG + ":onDestroy()", "Oh my god, they killed Kenny!");
    }
    
    /**
	 * To build up the menu, called one time only and that's the first time the menu 
	 * is inflated.
	 * 
	 * @see #onOptionsItemSelected(MenuItem)
     * 
     * @Override
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		  // Logging
		  this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onCreateOptionsMenu()", "Menu created");
          MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.menu, menu);
          return true;
    }
    
    /**
	 * Method to inflate menu with it's items.
     * 
     * @see #buildAndShowAboutDialog()
     * 
     * @Override
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
      		case R.id.item1:
      			// Logging
      			this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":onOptionsSelected()", "Menu item 1 selected");
      			// Build up and show the about dialog
      			this.buildAndShowAboutDialog();
      			return true;            	
      		default:
      			return super.onOptionsItemSelected(item);
		}
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
    	
        // Declare and initialize variables of type EditText
    	this.primaryListenNumberEditText = (EditText)findViewById(R.id.primaryNumber_et);
    	this.selectedToneEditText = (EditText)findViewById(R.id.msgTone_et);
    	this.ackNumberEditText = (EditText)findViewById(R.id.ackNumber_et);
    	this.rescueServiceEditText = (EditText)findViewById(R.id.rescueServiceName_et);
        
        // Declare and initialize variables of type button
    	this.editPrimaryNumberButton = (Button)findViewById(R.id.editPrimaryNumber_btn);
    	this.addSecondaryNumberButton = (Button)findViewById(R.id.addSecondaryNumber_btn);
    	this.removeSecondaryNumberButton = (Button)findViewById(R.id.deleteSecondaryNumber_btn);
    	this.editMsgToneButton = (Button)findViewById(R.id.editMsgTone_btn); 
    	this.listenMsgToneButton = (Button)findViewById(R.id.listenMsgTone_btn);
    	this.ackNumberButton = (Button)findViewById(R.id.editAckNumber_btn);
        this.editRescueServiceButton = (Button)findViewById(R.id.editRescueServiceName_btn);
        
        // Declare and initialize variables of type CheckBox
        this.soundSettingCheckBox = (CheckBox)findViewById(R.id.useSysSoundSettings_chk);
        this.enableAckCheckBox = (CheckBox)findViewById(R.id.enableAcknowledge_chk);
        this.playToneTwiceSettingCheckBox = (CheckBox)findViewById(R.id.playToneTwiceSetting_chk);
        this.enableSmsAlarmCheckBox = (CheckBox)findViewById(R.id.enableSmsAlarm_chk);
        
        // Declare and initialize variables of type Spinner
        this.toneSpinner = (Spinner)findViewById(R.id.toneSpinner_sp);
        this.secondaryListenNumberSpinner = (Spinner)findViewById(R.id.secondaryNumberSpinner_sp); 
        
        // Declare and initialize variables of type ImageView
        this.divider1ImageView = (ImageView)findViewById(R.id.mainDivider1_iv);
        this.divider2ImageView = (ImageView)findViewById(R.id.mainDivider2_iv);
        this.divider3ImageView = (ImageView)findViewById(R.id.mainDivider3_iv);
        
        // If Android API level less then 11 set bright gradient else set dark gradient
        if(Build.VERSION.SDK_INT < 11) {
        	this.divider1ImageView.setImageResource(R.drawable.gradient_divider_10_and_down); 
        	this.divider2ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
        	this.divider3ImageView.setImageResource(R.drawable.gradient_divider_10_and_down);
        	// Logging
        	this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":findViews()", "API level < 11, set bright gradients");
        } else {
        	this.divider1ImageView.setImageResource(R.drawable.gradient_divider_11_and_up); 
        	this.divider2ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
        	this.divider3ImageView.setImageResource(R.drawable.gradient_divider_11_and_up);
        	this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":findViews()", "API level > 10, set dark gradients");
        }            
        
        // Set some attributes to the smsPrimaryNumberEditText
        this.primaryListenNumberEditText.setEnabled(false);
        this.primaryListenNumberEditText.setClickable(false);
        this.primaryListenNumberEditText.setFocusable(false);
        this.primaryListenNumberEditText.setBackgroundColor(Color.WHITE);
        this.primaryListenNumberEditText.setTextColor(Color.BLACK);   
        
        // Set some attributes to the ackNumberEditText 
        this.ackNumberEditText.setEnabled(false);
        this.ackNumberEditText.setClickable(false);
        this.ackNumberEditText.setFocusable(false);
        this.ackNumberEditText.setBackgroundColor(Color.WHITE);   
        
        // Set some attributes to the fireDepartmentEditText
        this.rescueServiceEditText.setEnabled(false);
        this.rescueServiceEditText.setClickable(false);
        this.rescueServiceEditText.setFocusable(false);
        this.rescueServiceEditText.setBackgroundColor(Color.WHITE);  
        this.rescueServiceEditText.setTextColor(Color.BLACK);         
        
        // Set some attributes to the selectedToneEditText
        this.selectedToneEditText.setEnabled(false);
        this.selectedToneEditText.setClickable(false);
        this.selectedToneEditText.setFocusable(false);
        this.selectedToneEditText.setBackgroundColor(Color.WHITE);
        this.selectedToneEditText.setTextColor(Color.BLACK);   
        
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":findViews()", "All Views found");
    }
    
    /**
     * To set all <code>Shared Preferences</code> used by class <code>SmsAlarm</code>.
     * 
     * @see #getSmsAlarmPrefs()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.setPrefs(String, String, Object, Context)
     */
    private void setSmsAlarmPrefs(){   	
    	// Some logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":setSmsAlarmPrefs()", "Start setting shared preferences used by class SmsAlarm");
    	
      	// Set preferences used by class Sms Alarm
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_LISTEN_NUMBER_KEY(), this.primaryListenNumber, this);
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_LISTEN_NUMBERS_KEY(), this.secondaryListenNumbers, this);
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_MESSAGE_TONE_KEY(), this.primaryMessageToneId, this);
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_MESSAGE_TONE_KEY(), this.secondaryMessageToneId, this);  
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getUSE_OS_SOUND_SETTINGS_KEY(), this.useOsSoundSettings, this);        	
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_ACK_KEY(), this.useAlarmAcknowledge, this);
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getACK_NUMBER_KEY(), this.acknowledgeNumber, this);  
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPLAY_TONE_TWICE_KEY(), this.playToneTwice, this);  
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_SMS_ALARM_KEY(), this.enableSmsAlarm, this);     
      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getRESCUE_SERVICE_KEY(), this.rescueService, this);         	

    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":setSmsAlarmPrefs()", "Shared preferences set");
    }
    
    /**
     * To get <code>Shared Preferences</code> used by class <code>SmsAlarm</code>.
     * 
     * @see #setSmsAlarmPrefs()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.getPrefs(String, String, int, Context, Object)
     * @see ax.ha.it.smsalarm#PreferencesHandler.getPrefs(String, String, int, Context)   
     */
	@SuppressWarnings("unchecked")
	private void getSmsAlarmPrefs() {
    	//Some logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getSmsAlarmPrefs()", "Start retrieving shared preferences needed by class SmsAlarm");
    	
    	//Get shared preferences needed by class Sms Alarm
    	this.primaryListenNumber = (String) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_LISTEN_NUMBER_KEY(), this.STRING, this);
    	this.secondaryListenNumbers = (List<String>) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_LISTEN_NUMBERS_KEY(), this.LIST, this);
    	this.primaryMessageToneId = (Integer) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_MESSAGE_TONE_KEY(), this.INTEGER, this);
    	this.secondaryMessageToneId = (Integer) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_MESSAGE_TONE_KEY(), this.INTEGER, this, 1);
    	this.useOsSoundSettings = (Boolean) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getUSE_OS_SOUND_SETTINGS_KEY(), this.BOOLEAN, this);
    	this.useAlarmAcknowledge = (Boolean) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_ACK_KEY(), this.BOOLEAN, this);   
    	this.acknowledgeNumber = (String) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getACK_NUMBER_KEY(), this.STRING, this);  
    	this.playToneTwice = (Boolean) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPLAY_TONE_TWICE_KEY(), this.BOOLEAN, this);    
    	this.enableSmsAlarm = (Boolean) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getENABLE_SMS_ALARM_KEY(), this.BOOLEAN, this, true);  
    	this.rescueService = (String) prefHandler.getPrefs(prefHandler.getSHARED_PREF(), prefHandler.getRESCUE_SERVICE_KEY(), this.STRING, this);    	

    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":getSmsAlarmPrefs()", "Shared preferences retrieved");  	 
    }
    
    /**
     * To build up a dialog prompting user if it's okay to delete the selected secondary listen number.
     * 
     * @see #buildAndShowAboutDialog()
     * @see #buildAndShowInputDialog(int)
     * @see #buildAndShowToneDialog()
     * @see #updateSecondaryListenNumberSpinner()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.setPrefs(String, String, Object, Context)
     */
    private void buildAndShowDeleteSecondaryNumberDialog() {
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog()", "Start building delete SECONDARY number dialog");
    	
    	//Store secondaryListenNumberSpinner position
    	final int position = secondaryListenNumberSpinner.getSelectedItemPosition();
    	
    	//String to store complete prompt message in
    	String promptMessage = getString(R.string.deleteSecondaryNumberPromptMessage) + " " + secondaryListenNumbers.get(position) + "?";
    	
    	//Build up the alert dialog
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
    	//Set some attributes, title and message containing actual number
    	dialog.setIcon(android.R.drawable.ic_dialog_alert);
    	dialog.setTitle(R.string.deleteSecondaryNumberPromptTitle); 
    	dialog.setMessage(promptMessage);
    	
    	//Set dialog to non cancelable
    	dialog.setCancelable(false);
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog()", "Dialog attributes set");    	
    	
    	// Set a positive button and listen on it
    	dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {    		
				// Log information
	        	logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog().PosButton.OnClickListener().onClick()", "Positive Button pressed");	
	        	logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog().PosButton.OnClickListener().onClick()", "SECONDARY listen number: \"" + secondaryListenNumbers.get(position) + "\" is about to be removed from list of SECONDARY listen numbers");	        	
	    		// Delete number from list
	    		secondaryListenNumbers.remove(position);  		
				// Store to shared preferences
		      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_LISTEN_NUMBERS_KEY(), secondaryListenNumbers, SmsAlarm.this);
		      	// Update affected UI widgets
		      	updateSecondaryListenNumberSpinner();	        
	    	}  	
    	}); 
    	
    	//Set a neutral button, due to documentation it has same functionality as "back" button
    	dialog.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
	    	  public void onClick(DialogInterface dialog, int whichButton) {
	    		  // DO NOTHING, except logging
	    		  logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");	
	    	  }
    	}); 
    	
    	// Logging
    	this.logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowDeleteSecondaryNumberDialog()", "Showing dialog");	
    	
    	//Show it
    	dialog.show();    	
    }

    /**
     * Universal method to build of four different types of input dialogs.
     * The supported types are: <b><i>PRIMARY</b></i>, <b><i>SECONDARY</b></i>, <b><i>ACKNOWLEDGE</b></i> and <b><i>RESCUESERVICE</b></i>.
     * If a dialog type are given as parameter thats not supported a dummy dialog
     * will be built and shown.
     * 
     * @param int Dialog type as integer
     * 
     * @see #buildAndShowAboutDialog()
     * @see #buildAndShowToneDialog()
     * @see #buildAndShowDeleteSecondaryNumberDialog()
     * @see #updatePrimaryListenNumberEditText()
     * @see #updateSecondaryListenNumberSpinner()
     * @see #updateAcknowledgeNumberEditText()
     * @see #updateRescueServiceEditText()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.setPrefs(String, String, Object, Context)
     */
    private void buildAndShowInputDialog(final int dialogType) {  
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowInputDialog()", "Start building dialog");
    	
    	//Build up the alert dialog
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
    	//Set some attributes
    	dialog.setIcon(android.R.drawable.ic_dialog_info);
    	
    	//Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	
    	/*
    	 * Switch through the different dialog types and set correct strings and edittext to the dialog. 
    	 * If dialog type is non supported a default dialog DUMMY is built up.
    	 */
    	switch(dialogType) {
    	case (PRIMARY): // <--0
    		// Set title
        	dialog.setTitle(R.string.numberPromptTitle);
    		// Set message
    		dialog.setMessage(R.string.primaryNumberPromptMessage);
    		// Set hint to edittext
        	input.setHint(R.string.numberPromptHint);
        	// Set Input type to edittext
        	input.setInputType(InputType.TYPE_CLASS_PHONE);
        	// Set dialog to non cancelable
        	dialog.setCancelable(false);
        	// Bind dialog to input
        	dialog.setView(input);
        	// Logging
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type PRIMARY");
    		break;
    	case (SECONDARY): // <--1
        	dialog.setTitle(R.string.numberPromptTitle);
    		dialog.setMessage(R.string.secondaryNumberPromptMessage);
        	input.setHint(R.string.numberPromptHint);
        	input.setInputType(InputType.TYPE_CLASS_PHONE);
        	dialog.setCancelable(false);
        	dialog.setView(input);
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type SECONDARY");
    		break;
    	case (ACKNOWLEDGE): // <--2
        	dialog.setTitle(R.string.numberPromptTitle);
    		dialog.setMessage(R.string.ackNumberPromptMessage);
        	input.setHint(R.string.numberPromptHint);
        	input.setInputType(InputType.TYPE_CLASS_PHONE);
        	dialog.setCancelable(false);
        	dialog.setView(input);
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type ACKNOWLEDGE");
    		break;
    	case (RESCUESERVICE): // <--3
        	dialog.setTitle(R.string.rescueServicePromptTitle);
    		dialog.setMessage(R.string.rescueServicePromptMessage);
        	input.setHint(R.string.rescueServiceHint);
        	input.setInputType(InputType.TYPE_CLASS_TEXT);
        	dialog.setCancelable(false);
        	dialog.setView(input);
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowInputDialog()", "Dialog attributes is set for dialog type RESCUESERVICE");
    		break;
    	default: // <--Unsupported dialog type. Displaying a dummy dialog!
        	dialog.setTitle("Congratulations!");
    		dialog.setMessage("Somehow you got this dialog to show up! I bet a monkey must have been messing around with the code;-)");
        	dialog.setCancelable(false);
        	this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":buildAndShowInputDialog()", "A UNSUPPORTED dialog type has been given as parameter, a DUMMY dialog will be built and shown");
    	}
    	
    	// Set a positive button and listen on it
    	dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Log information
	        	logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Positive Button pressed");	
	    		
	        	// Boolean indicating if there are duplicates of primary and secondary alarm numbers
	        	boolean duplicatedNumbers = false;
	        	
	        	/*
	        	 * Switch through the different dialog types and set proper input handling to each of them. 
	        	 * If dialog type is non supported no input is taken.
	        	 */
	        	switch(dialogType) {
	        	case (PRIMARY): // <--0
    				// If list is not empty there are numbers to equalize with each other, else just store the input
    				if(!secondaryListenNumbers.isEmpty()) {
    					// Iterate through all strings in the list
    					for(int i=0; i < secondaryListenNumbers.size(); i++) {
    						// If a string in the list is equal with the input then it's a duplicated
    						if(secondaryListenNumbers.get(i).equals(input.getText().toString()) && !input.getText().toString().equals("")) {
    							duplicatedNumbers = true;
    						}
    					}

    					// Store input if no duplication of numbers exists, else prompt user for number again
        				if(!duplicatedNumbers) {
        					// Store input to class variable
        					primaryListenNumber = input.getText().toString();
        					// Store to shared preferences
        			      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_LISTEN_NUMBER_KEY(), primaryListenNumber, SmsAlarm.this);
        			      	// Update affected UI widgets
        			      	updatePrimaryListenNumberEditText();
        			      	// Log
        					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY phone number has been stored from user input. New PRIMARY phone number is: \"" + primaryListenNumber +"\"");
        				} else {
        					Toast.makeText(SmsAlarm.this, R.string.duplicatedNumbers, Toast.LENGTH_LONG).show();
        					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given PRIMARY phone number(" + input.getText().toString() + ") exists in the list of SECONDARY phone numbers and therefore cannot be stored. Showing dialog of type PRIMARY again");
        					buildAndShowInputDialog(dialogType);
        				}    					
    				} else {
    					primaryListenNumber = input.getText().toString();
    			      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_LISTEN_NUMBER_KEY(), primaryListenNumber, SmsAlarm.this);
    			      	updatePrimaryListenNumberEditText();    					
    					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New PRIMARY phone number has been stored from user input. New PRIMARY phone number is: \"" + primaryListenNumber + "\"");
    				} 
	        		break;
	        	case (SECONDARY): // <--1
    				// If input isn't equal with the primaryListenNumber and input isn't empty
    				if(!primaryListenNumber.equals(input.getText().toString()) && !input.getText().toString().equals("")) {    					
    					// Iterate through all strings in the list to check if number already exists in list
    					for(int i=0; i < secondaryListenNumbers.size(); i++) {
    						// If a string in the list is equal with the input then it's a duplicated
    						if(secondaryListenNumbers.get(i).equals(input.getText().toString()) && !input.getText().toString().equals("")) {
    							duplicatedNumbers = true;
    						}
    					}

    					// Store input if duplicated numbers is false
        				if(!duplicatedNumbers) {
        					// Add given input to list
        					secondaryListenNumbers.add(input.getText().toString());
        					// Store to shared preferences
        			      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_LISTEN_NUMBERS_KEY(), secondaryListenNumbers, SmsAlarm.this);
        			      	// Update affected UI widgets
        			      	updateSecondaryListenNumberSpinner();
        			      	// Log
        					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New SECONDARY phone number has been stored from user input to the list of SECONDARY phone numbers . New SECONDARY phone number is: \"" + input.getText().toString() + "\"");	        					
        				} else {
        					Toast.makeText(SmsAlarm.this, R.string.numberAlreadyInList, Toast.LENGTH_LONG).show();
        					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY phone number(" + input.getText().toString() + ") already exists in the list of SECONDARY phone numbers and therefore cannot be stored. Showing dialog of type SECONDARY again");        					
        					buildAndShowInputDialog(dialogType);
        				}    					
    				} else {
    					if(primaryListenNumber.equals(input.getText().toString())) {
    						Toast.makeText(SmsAlarm.this, R.string.duplicatedNumbers, Toast.LENGTH_LONG).show();
        					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY phone number(" + input.getText().toString() + ") is the same as the PRIMARY phone number and therefore cannot be stored. Showing dialog of type SECONDARY again");
    					} else {
    						Toast.makeText(SmsAlarm.this, R.string.emptySecondaryNumber, Toast.LENGTH_LONG).show();
        					logger.logCatTxt(logger.getWARN(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Given SECONDARY phone number is empty and therefore cannot be stored. Showing dialog of type SECONDARY again");    						
    					}
    					buildAndShowInputDialog(dialogType);
    				}
	        		break;
	        	case (ACKNOWLEDGE): // <--2       
	        		// Store input to class variable
	        		acknowledgeNumber = input.getText().toString();
	        		// Store to shared preferences
	          	    prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getACK_NUMBER_KEY(), acknowledgeNumber, SmsAlarm.this); 
	          	    // update affected UI widgets
	          	    updateAcknowledgeNumberEditText();
	          	    // Log
					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New ACKNOWLEDGE phone number has been stored from user input . New ACKNOWLEDGE phone number is: \"" + acknowledgeNumber + "\"");	        					
	        		break;
	        	case (RESCUESERVICE): // <--3
	        		// Store input to class variable
	        		rescueService = input.getText().toString();
	        		// Store to shared preferences
	          		prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getRESCUE_SERVICE_KEY(), rescueService, SmsAlarm.this); 
	          		// update affected UI widgets
	          		updateRescueServiceEditText();
	          		// Log
					logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "New RESCUESERVICE name has been stored from user input . New RESCUESERVICE name is: \"" + rescueService + "\"");	        						        	
	        		break;
	        	default: // <--Unsupported dialog type
					logger.logCatTxt(logger.getERROR(), LOG_TAG + ":buildAndShowInputDialog().PositiveButton.OnClickListener().onClick()", "Nothing is stored beacause given dialog type is UNSUPPORTED, given dialog is of type number: \"" + dialogType + "\"");	        						        	
	        	}	        
	    	}  	
    	});
    	
    	// Only set neutral button if dialog type is supported
    	if(dialogType >= PRIMARY && dialogType <= RESCUESERVICE) {
	    	//Set a neutral button, due to documentation it has same functionality as "back" button
	    	dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    	  public void onClick(DialogInterface dialog, int whichButton) {
		    		  // DO NOTHING, except logging
		    		  logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowInputDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");	 
		    	  }
	    	});
    	}
    	
    	// Logging
    	this.logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowInputDialog()", "Showing dialog");	 
    	
    	//Show it
    	dialog.show();
    }
    
    /**
     * To build up and show a dialog with a list populated with message tones.
     * User chooses applications message tones from that list.
     * 
     * @see #buildAndShowInputDialog(int)
     * @see #buildAndShowAboutDialog()
     * @see #updateSelectedToneEditText()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     * @see ax.ha.it.smsalarm#PreferencesHandler.setPrefs(String, String, Object, Context)
     */
    private void buildAndShowToneDialog() {    
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowToneDialog()", "Start building tone dialog");
    	
       	// Build up the alert dialog
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);

    	// Set attributes
    	dialog.setIcon(android.R.drawable.ic_dialog_info);
    	dialog.setTitle(R.string.tonePromptTitle);
    	dialog.setCancelable(false);
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowToneDialog()", "Dialog attributes set");
    	
    	// Set items to list view from resource array tones
    	dialog.setItems(R.array.tones, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface arg0, int listPosition) {
				// Log information
	        	logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "Item in message tones list pressed");		
				
				// Store position(toneId) in correct variable, depending on spinner value
				if(toneSpinnerPos == 0) { // <--PRIMARY MESSAGE TONE
					// Store primary message tone id from position of list
					primaryMessageToneId = listPosition;
					// Log information
		        	logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "New PRIMARY message tone selected. Tone: \"" + noiseHandler.msgToneLookup(SmsAlarm.this, primaryMessageToneId) + "\", id: \"" + primaryMessageToneId + "\" and tone Spinner position: \"" + Integer.toString(toneSpinnerPos) + "\"");			
					// Store primary message tone id to preferences to preferences
			      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getPRIMARY_MESSAGE_TONE_KEY(), primaryMessageToneId, SmsAlarm.this);
			      	// Update selected tone EditText
			      	updateSelectedToneEditText();
				} else if(toneSpinnerPos == 1) { // <--SECONDARY MESSAGE TONE
					secondaryMessageToneId = listPosition;
		        	logger.logCatTxt(logger.getDEBUG(), LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "New SECONDARY message tone selected. Tone: \"" + noiseHandler.msgToneLookup(SmsAlarm.this, secondaryMessageToneId) + "\", id: \"" + secondaryMessageToneId + "\" and tone Spinner position: \"" + Integer.toString(toneSpinnerPos) + "\"");		
			      	prefHandler.setPrefs(prefHandler.getSHARED_PREF(), prefHandler.getSECONDARY_MESSAGE_TONE_KEY(), secondaryMessageToneId, SmsAlarm.this);  					
					updateSelectedToneEditText();
				} else { // <--UNSUPPORTED SPINNER POSITION
		        	// DO NOTHING EXCEPT LOG ERROR MESSAGE
		        	logger.logCatTxt(logger.getERROR(), LOG_TAG + ":buildAndShowToneDialog().Item.OnClickListener().onClick()", "Invalid spinner position occurred. Current tone Spinner position is: \"" + Integer.toString(toneSpinnerPos) + "\"");
				}
			}
		});
    	
    	// Set a neutral button and listener
    	dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    	  public void onClick(DialogInterface dialog, int whichButton) {
	    		  // DO NOTHING, except logging
	    		  logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowToneDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");	
	    	  }
    	});

    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowToneDialog()", "Showing dialog");
    	
    	//Show dialog
    	dialog.show();        
    }
    
    /**
     * To build up and show an about dialog.
     * 
     * @see #buildAndShowDeleteSecondaryNumberDialog()
     * @see #buildAndShowInputDialog(int)
     * @see #buildAndShowToneDialog()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)
     */
    private void buildAndShowAboutDialog() {  
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowAboutDialog()", "Start building about dialog");   	
 
       	//Build up the alert dialog
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	
    	final View view = factory.inflate(R.layout.about, null);

    	//Set attributes
    	dialog.setIcon(android.R.drawable.ic_dialog_info);
    	dialog.setTitle(R.string.about);
    	dialog.setView(view);
    	dialog.setCancelable(false);
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowAboutDialog()", "Dialog attributes set");    	
    	
    	//Set a neutral button
    	dialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
	    	  public void onClick(DialogInterface dialog, int whichButton) {
	    		  // DO NOTHING, except logging
	    		  logger.logCatTxt(logger.getINFO(), LOG_TAG + ":buildAndShowAboutDialog().NeutralButton.OnClickListener().onClick()", "Neutral Button pressed in dialog, nothing done");	
	    	  }
    	});
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":buildAndShowAboutDialog()", "Showing dialog");    	

    	// Show dialog
    	dialog.show();        
    }
    
    /**
     * To update Sms Alarms whole User Interface.
     * 
     * @see #updatePrimaryListenNumberEditText()
     * @see #updateSecondaryListenNumberSpinner()
     * @see #updateAcknowledgeNumberEditText()
     * @see #updateRescueServiceEditText()
     * @see #updateSelectedToneEditText()
     * @see #updateUseOsSoundSettingsCheckbox()
     * @see #updatePlayToneTwiceCheckBox()
     * @see #updateEnableSmsAlarmCheckBox()
     * @see #updateAcknowledgeWidgets()
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)     
     */
    private void updateWholeUI() {
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateWholeUI", "Whole user interface is about to be updated");
    	
    	// Update primary listen number EditText
    	this.updatePrimaryListenNumberEditText();

    	// Update secondary listen numbers Spinner
    	this.updateSecondaryListenNumberSpinner();
    	
    	// Update acknowledge number EditText
    	this.updateAcknowledgeNumberEditText();
    	
    	// Update rescue service EditText
    	this.updateRescueServiceEditText();  
        
        // Update selected EditText widget
        this.updateSelectedToneEditText();
        
        // Update use OS sound settings CheckBox widget
        this.updateUseOsSoundSettingsCheckbox();
        
        // Update play tone twice CheckBox widget
        this.updatePlayToneTwiceCheckBox();
        
        // Update enable Sms Alarm CheckBox widget
        this.updateEnableSmsAlarmCheckBox();
    	
    	// Update widgets in relation to alarm acknowledgement
        this.updateAcknowledgeWidgets();
        
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateWholeUI", "User interface updated");        
    }    
    /**
     * To update primary listen number <code>EditText</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updatePrimaryListenNumberEditText() {
        // Update primary listen number EditText with value
    	this.primaryListenNumberEditText.setText(this.primaryListenNumber);  
    	
	    // Logging
	    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updatePrimaryListenNumberEditText()", "PRIMARY listen number EditText set to: " + this.primaryListenNumber);    	
	    this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updatePrimaryListenNumberEditText()", "PRIMARY listen number EditText updated");    
    }
    
    /**
     * To update secondary listen numbers <code>Spinner</code> with correct values.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateSecondaryListenNumberSpinner() {
	    //Check if there are secondary listen numbers and build up a proper spinner according to that information
	    if(!this.secondaryListenNumbers.isEmpty()) {
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.secondaryListenNumbers);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    this.secondaryListenNumberSpinner.setAdapter(adapter);	 
		    // Logging
		    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateSecondaryListenNumberSpinner()", "Populate SECONDARY listen number spinner with values: " + this.secondaryListenNumbers);
	    } else {
	    	this.emptySecondaryListenNumbers.add(getString(R.string.enterPhoneNumberHint));
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.emptySecondaryListenNumbers);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    this.secondaryListenNumberSpinner.setAdapter(adapter);	
		    // Logging
		    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateSecondaryListenNumberSpinner()", "List with SECONDARY listen numbers is empty, populating spinner with an empty list");		    
	    }  
	    
	    // Logging
	    this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateSecondaryListenNumberSpinner()", "SECONDARY listen numbers Spinner updated");
    }
    
    /**
     * To update acknowledge number <code>EditText</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateAcknowledgeNumberEditText() {
        // Update acknowledge number EditText with value
        this.ackNumberEditText.setText(this.acknowledgeNumber); 
        
	    // Logging
	    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateAcknowledgeNumberEditText()", "Acknowledge number EditText set to: " + this.acknowledgeNumber);    	
	    this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateAcknowledgeNumberEditText()", "Acknowledge number EditText updated");           
    }

    /**
     * To update selected tone <code>EditText</code> widget with value of <code>toneSpinner</code> position.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateSelectedToneEditText() {
    	// Log tone spinner position
    	this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateSelectedToneEditText()", "Tone Spinner position is: " + Integer.toString(this.toneSpinnerPos));
    	
        // Set message tone to the selectedToneEditText, depending on which value spinner has. Also log this event
        if(this.toneSpinnerPos == 0) {
        	this.selectedToneEditText.setText(this.noiseHandler.msgToneLookup(this, this.primaryMessageToneId));
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateSelectedToneEditText()", "Selected tone EditText updated");
        } else if(this.toneSpinnerPos == 1) {
        	this.selectedToneEditText.setText(this.noiseHandler.msgToneLookup(this, this.secondaryMessageToneId));
        	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateSelectedToneEditText()", "Selected tone EditText updated");
        } else {
        	// DO NOTHING EXCEPT LOG ERROR MESSAGE
        	this.logger.logCatTxt(this.logger.getERROR(), this.LOG_TAG + ":updateSelectedToneEditText()", "Invalid spinner position occurred. Current tone Spinner position is: \"" + Integer.toString(this.toneSpinnerPos) + "\"");
        }    	
    }
    
    /**
     * To update rescue service <code>EditText</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateRescueServiceEditText() {
        // Update rescue service EditText
        this.rescueServiceEditText.setText(this.rescueService); 
        
	    // Logging
	    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateRescueServiceEditText()", "Rescue service EditText set to: " + this.rescueService);    	
	    this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateRescueServiceEditText()", "Rescue service EditText updated");          
    }
    
    /**
     * To update use OS sound settings <code>CheckBox</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)   
     */
    private void updateUseOsSoundSettingsCheckbox() {
        // Update use OS sound settings CheckBox
    	if(this.useOsSoundSettings) {
    		this.soundSettingCheckBox.setChecked(true);
    	    this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateUseOsSoundSettingsCheckbox()", "Use OS sound settings CheckBox \"Checked\"(" + this.useOsSoundSettings + ")"); 
    	} else {
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateUseOsSoundSettingsCheckbox()", "Use OS sound settings CheckBox \"Unchecked\"(" + this.useOsSoundSettings + ")"); 
    	}
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateUseOsSoundSettingsCheckbox()", "Use OS sound settings CheckBox updated");    
    }
    
    /**
     * To update play tone twice <code>CheckBox</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updatePlayToneTwiceCheckBox() {
    	// Update play tone twice CheckBox
    	if(this.playToneTwice) {
    		this.playToneTwiceSettingCheckBox.setChecked(true);
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice CheckBox \"Checked\"(" + this.playToneTwice + ")"); 
    	} else {
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice CheckBox \"Unchecked\"(" + this.playToneTwice + ")"); 
    	}
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updatePlayToneTwiceCheckBox()", "Play tone twice CheckBox updated");  
    }
    
    /**
     * To update enable Sms Alarm <code>CheckBox</code> widget.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateEnableSmsAlarmCheckBox() {
    	// Update enable Sms Alarm CheckBox(default checked=true)
    	if(!this.enableSmsAlarm) {
    		this.enableSmsAlarmCheckBox.setChecked(false);
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm CheckBox \"Unchecked\"(" + this.enableSmsAlarm + ")"); 
    	} else {
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm CheckBox \"Checked\"(" + this.enableSmsAlarm + ")");     		
    	}
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateEnableSmsAlarmCheckBox()", "Enable SmsAlarm CheckBox updated"); 
    }
    
    /**
     * To update widgets with relations to alarm acknowledgement. These are widgets of
     * type <code>CheckBox</code>, <code>Button</code> and <code>EditText</code>, they are
     * enableAckCheckBox, ackNumberButton and ackNumberEditText.
     * 
     * @see ax.ha.it.smsalarm#LogHandler.logCatTxt(int, String , String)        
     */
    private void updateAcknowledgeWidgets() {
    	/* 
    	 * Set checkbox for the enableAckCheckBox to true or false, also
    	 * set some attributes to the ackNumberButton and the ackNumberField
    	 */
    	if(this.useAlarmAcknowledge) {
    		this.enableAckCheckBox.setChecked(true);
    		this.ackNumberButton.setEnabled(true);
    		this.ackNumberEditText.setTextColor(Color.BLACK);
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge CheckBox \"Checked\"(" + this.useAlarmAcknowledge + "), acknowledge number Button is \"Enabled\" and acknowledge number EditText is \"Enabled\"");
    	} else {
    		this.ackNumberButton.setEnabled(false);
    		this.ackNumberEditText.setTextColor(Color.GRAY);
    		this.logger.logCatTxt(this.logger.getDEBUG(), this.LOG_TAG + ":updateAcknowledgeWidgets()", "Enable acknowledge CheckBox \"Unchecked\"(" + this.useAlarmAcknowledge + "), acknowledge number Button is \"Disabled\" and acknowledge number EditText is \"Disabled\"");
    	}  
    	
    	// Logging
    	this.logger.logCatTxt(this.logger.getINFO(), this.LOG_TAG + ":updateAcknowledgeWidgets()", "Acknowledge alarm UI widgets updated"); 
    }
}