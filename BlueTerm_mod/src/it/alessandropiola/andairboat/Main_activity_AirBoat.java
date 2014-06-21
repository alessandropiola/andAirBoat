package it.alessandropiola.andairboat;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;


public class Main_activity_AirBoat extends Activity implements SensorEventListener {
	
	//sensore
   private float mSensorY;

    // sensor-related
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static TextView mTitle;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    /**
     * Set to true to add debugging code and logging.
     */
    public static final boolean DEBUG = true;

    /**
     * Set to true to log each character received from the remote process to the
     * android log, which makes it easier to debug some kinds of problems with
     * emulating escape sequences and control codes.
     */
    public static final boolean LOG_CHARACTERS_FLAG = DEBUG && false;

    /**
     * Set to true to log unknown escape sequences.
     */
    public static final boolean LOG_UNKNOWN_ESCAPE_SEQUENCES = DEBUG && false;

    /**
     * The tag we use when logging, so that our messages can be distinguished
     * from other messages in the log. Public because it's used by several
     * classes.
     */
	public static final String LOG_TAG = "AirBoat";

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;	

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
	private BluetoothAdapter mBluetoothAdapter = null;
	
    /**
     * Our main view. Displays the emulated terminal screen.
     */
    //private EmulatorView mEmulatorView;

    /**
     * A key listener that tracks the modifier keys and allows the full ASCII
     * character set to be entered.
     */
   	
	public int letter ;
    private static BluetoothSerialService mSerialService = null;
    
	//private static InputMethodManager mInputManager;
	
	private boolean mEnablingBT;
    private boolean mLocalEcho = false;
    //private int mControlKeyId = 0;

    //private static final String LOCALECHO_KEY = "localecho";
    //private static final String CONTROLKEY_KEY = "controlkey";

    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;
    public static final int BLUE = 0xff344ebd;

   // private static final int[][] COLOR_SCHEMES = {
   //     {BLACK, WHITE}, {WHITE, BLACK}, {WHITE, BLUE}};

    //private static final int[] CONTROL_KEY_SCHEMES = {
    //    KeyEvent.KEYCODE_DPAD_CENTER,
    //    KeyEvent.KEYCODE_AT,
    //    KeyEvent.KEYCODE_ALT_LEFT,
    //    KeyEvent.KEYCODE_ALT_RIGHT
    //};
//    private static final String[] CONTROL_KEY_NAME = {
//        "Ball", "@", "Left-Alt", "Right-Alt"
//    };
    //private static String[] CONTROL_KEY_NAME;

    //private int mControlKeyCode;

    //private SharedPreferences mPrefs;
	
    private MenuItem mMenuItemConnect;

    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		
        
        // initializing sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
		if (DEBUG)
			Log.e(LOG_TAG, "+++ ON CREATE +++");


        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.airboat_activity);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        

        
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
            finishDialogNoBluetooth();
			return;
		}
		
        

        mSerialService = new BluetoothSerialService(this, mHandlerBT);        

		
        
		//attivo i 4 motori al massimo (seekBar)
        SeekBar motore1 = (SeekBar)findViewById(R.id.seekBar1);
		SeekBar motore2 = (SeekBar)findViewById(R.id.seekBar2);
		SeekBar motore3 = (SeekBar)findViewById(R.id.seekBar3);
		SeekBar motore4 = (SeekBar)findViewById(R.id.seekBar4);
		
		motore2.setProgress(100);
		motore3.setProgress(100);
		motore4.setProgress(100);
		motore1.setSecondaryProgress(75);
		motore2.setSecondaryProgress(75);
		motore3.setSecondaryProgress(75);
		motore4.setSecondaryProgress(75);
		
		motore2.setProgress(100);
		motore2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				String strValue = null;
				strValue = "0";
				if (progress>20) strValue = "1";
				if (progress>40) strValue = "2";
				if (progress>60) strValue = "3";
				if (progress>80) strValue = "4";
				
				
				
				BTInvia(strValue);
		    	
				
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		motore1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				String strValue = null;
				strValue = "5";
				if (progress>20) strValue = "6";
				if (progress>40) strValue = "7";
				if (progress>60) strValue = "8";
				if (progress>80) strValue = "9";
				
				
				//Log.e(LOG_TAG,strValue);
				BTInvia(strValue);
		    	
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		
		});
		
		//ANIMAZIONE CON GESTIONE SCHERMO
		//leggo la dimensione dello schermo per gestire il 
		float intdelta1 = 64;
		float intdelta2 = 64;
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int wwidth = displaymetrics.widthPixels;
		//se lo schermo è piccolo miglioro il delta per la visualzzazione
		if (height<400)  {
			intdelta1=50;
			intdelta2=50;
		}
		Log.i(LOG_TAG,"delta:"+intdelta2);
		Log.i(LOG_TAG,"height:"+height);
		
		// rotation from 0 to 90 degrees here
		Button avanti = (Button) findViewById(R.id.avanti);



		RotateAnimation a = new RotateAnimation(0, -90,intdelta1,intdelta2);
		a.setFillAfter(true);
		a.setDuration(4000);
		avanti.startAnimation(a);
		// rotation from 0 to 90 degrees here
		Button indietro = (Button) findViewById(R.id.indietro);
		RotateAnimation b = new RotateAnimation(0, 90,intdelta1,intdelta2);
		b.setFillAfter(true);
		b.setDuration(4000);
		indietro.startAnimation(b);
		
		//gestione touch pulsante aventi
		avanti.setOnTouchListener(new View.OnTouchListener() {
			@Override
			  public boolean onTouch(View arg0, MotionEvent arg1) {
				
			    // TODO Auto-generated method stub
			    int action = arg1.getAction();
			    if(action == MotionEvent.ACTION_DOWN) { 
			    	BTInvia("A");
			    	BTInvia("D");
			    	BTInvia("G");
			    	BTInvia("L");
			    	
			    	
			    	Log.w(LOG_TAG,"AD");
			    	
			    	return true;
			    } else if (action == MotionEvent.ACTION_UP) {
			    	BTInvia("C");
			    	BTInvia("F");
			    	BTInvia("I");
			    	BTInvia("N");
			    	
			    	Log.e(LOG_TAG,"CF");
			    	
			    	return true;
			    }
			         return false;

		}
			
		});
		//gestione touch pulsante indietro
		indietro.setOnTouchListener(new View.OnTouchListener() {
			@Override
			  public boolean onTouch(View arg0, MotionEvent arg1) {
				
			    // TODO Auto-generated method stub
			    int action = arg1.getAction();
			    if(action == MotionEvent.ACTION_DOWN) { 
			    	BTInvia("B");
			    	BTInvia("E");
			    	BTInvia("H");
			    	BTInvia("M");
			    	Log.e(LOG_TAG,"BE");
			    	return true;
			    } else if (action == MotionEvent.ACTION_UP) {
			    	BTInvia("C");
			    	BTInvia("F");
			    	BTInvia("I");
			    	BTInvia("N");
			    	
			    	Log.e(LOG_TAG,"CF");
			    	
			    	return true;
			    }
			         return false;

		}
			
		});
		
	    CheckBox setWhater = (CheckBox)findViewById(R.id.cbWhater);
	    setWhater.setOnCheckedChangeListener(new OnCheckedChangeListener() {


			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)BTInvia("P");
				else BTInvia("O");
			}
			});
	}


	@Override
	public void onStart() {
		super.onStart();
		if (DEBUG)
			Log.e(LOG_TAG, "++ ON START ++");
		
		mEnablingBT = false;
	}
	@Override
	public synchronized void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		if (DEBUG) {
			Log.e(LOG_TAG, "+ ON RESUME +");
		}
		
		if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
		    if ( (mBluetoothAdapter != null)  && (!mBluetoothAdapter.isEnabled()) ) {
			
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.alert_dialog_turn_on_bt)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.alert_dialog_warning_title)
                    .setCancelable( false )
                    .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		mEnablingBT = true;
                    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);			
                    	}
                    })
                    .setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		finishDialogNoBluetooth();            	
                    	}
                    });
                AlertDialog alert = builder.create();
                alert.show();
		    }		
		
		    if (mSerialService != null) {
		    	// Only if the state is STATE_NONE, do we know that we haven't started already
		    	if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
		    		// Start the Bluetooth chat services
		    		mSerialService.start();
		    	}
		    }

		   // if (mBluetoothAdapter != null) {
		   // 	readPrefs();
		   // 	updatePrefs();

		    	//mEmulatorView.onResume();
		    //}
		}
	}

 

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.e(LOG_TAG, "--- ON DESTROY ---");
		
        if (mSerialService != null)
        	mSerialService.stop();
        
	}

  /*  private void readPrefs() {
        mLocalEcho = mPrefs.getBoolean(LOCALECHO_KEY, mLocalEcho);
        //mFontSize = readIntPref(FONTSIZE_KEY, mFontSize, 20);
        //mColorId = readIntPref(COLOR_KEY, mColorId, COLOR_SCHEMES.length - 1);
        mControlKeyId = readIntPref(CONTROLKEY_KEY, mControlKeyId,
                CONTROL_KEY_SCHEMES.length - 1);
    }

    private void updatePrefs() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //mEmulatorView.setTextSize((int) (mFontSize * metrics.density));
        //setColors();
        mControlKeyCode = CONTROL_KEY_SCHEMES[mControlKeyId];
    }

    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }*/
    
	public int getConnectionState() {
		return mSerialService.getState();
	}


    public void send(byte[] out) {
    	mSerialService.write( out );
    }
    

    public void BTInvia(String lettera){
    	Log.w(LOG_TAG,"BTInvia:"+lettera);
    	String text = lettera;
    	char c = text.charAt(0);
    	letter = c;

        if (letter >= 0) {
        	byte[] buffer = new byte[1];
        	buffer[0] = (byte)letter;
        	mSerialService.write(buffer);
        }
      
    }

   private final Handler mHandlerBT = new Handler() {
    	
        @Override
        public void handleMessage(Message msg) {        	
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothSerialService.STATE_CONNECTED:
                	if (mMenuItemConnect != null) {
                		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                		mMenuItemConnect.setTitle(R.string.disconnect);
                	}
                	
                	//mInputManager.showSoftInput(mEmulatorView, InputMethodManager.SHOW_IMPLICIT);
                	
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    
                    // a quuesto punto sono connesso ed invio uno stop ai motori
                    BTInvia("C");
			    	BTInvia("F");
			    	BTInvia("I");
			    	BTInvia("N");
                    break;
                    
                case BluetoothSerialService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                    
                case BluetoothSerialService.STATE_LISTEN:
                case BluetoothSerialService.STATE_NONE:
                	if (mMenuItemConnect != null) {
                		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
                		mMenuItemConnect.setTitle(R.string.connect);
                	}

            		//mInputManager.hideSoftInputFromWindow(mEmulatorView.getWindowToken(), 0);
                	
                    mTitle.setText(R.string.title_not_connected);

                    break;
                }
                break;
            case MESSAGE_WRITE:
            	if (mLocalEcho) {
            		byte[] writeBuf = (byte[]) msg.obj;
            		//mEmulatorView.write(writeBuf, msg.arg1);
            	}
                
                break;
             
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };    

    
    public void finishDialogNoBluetooth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_no_bt)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.app_name)
        .setCancelable( false )
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       finish();            	
                	   }
               });
        AlertDialog alert = builder.create();
        alert.show(); 
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.d(LOG_TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        
        case REQUEST_CONNECT_DEVICE:

            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mSerialService.connect(device);  
                
            }
            break;

        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "BT not enabled");
                
                finishDialogNoBluetooth();                
            }
        }
    }
    @Override
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        mMenuItemConnect = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.connect:
        	
        	if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
        		// Launch the DeviceListActivity to see devices and do scan
        		Intent serverIntent = new Intent(this, DeviceListActivity.class);
        		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        	}
        	else
            	if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
            		mSerialService.stop();
		    		mSerialService.start();
            	}
            return true;


        }
        return false;
    }



	public void onSensorChanged(SensorEvent event)
    {
    	
    	//leggo i valori dell'accelerometro
        mSensorY = event.values[1];
        //dopo aver letto i sensori sposto le varie 
		//attivo i 4 motori al massimo (seekBar)
        SeekBar motore1 = (SeekBar)findViewById(R.id.seekBar1);
		SeekBar motore2 = (SeekBar)findViewById(R.id.seekBar2);
		SeekBar motore3 = (SeekBar)findViewById(R.id.seekBar3);
		SeekBar motore4 = (SeekBar)findViewById(R.id.seekBar4);
		
		motore1.setProgress((int) (100+(mSensorY*10)));
		motore2.setProgress((int) (100-(mSensorY*10)));
		motore3.setProgress((int) (100+(mSensorY*10)));
		motore4.setProgress((int) (100-(mSensorY*10)));
        

    }


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
 }



