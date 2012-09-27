package com.jtws.tawch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class Tawch extends Activity {
	private final String TAG = "com.jtws.Tawch";
	
	private boolean torchState = false;
	
	private Button toggleButton;
	private Camera camera;
	private AdView adview;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Setup click listeners for all the buttons
        toggleButton = (Button)findViewById(R.id.turn_on_button);
        toggleButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				toggleFlash();
			}
        	
        });
        
        // Add test device
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice("F5DE7F445ACCC803052D88C2B5A0AFAA");
        adRequest.addTestDevice("6AE4AF6D0C4D2708108CAFA014D1C5A1");
        
        // Get an advert
        adview = (AdView)findViewById(R.id.ad_view);
        adview.loadAd(adRequest);
    }
    
    @Override
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
    
    /**
     * Initialises the camera if not already started
     * 
     * @return True on success, false on failure
     */
    public boolean initCamera() {
    	if (camera == null) {
    		camera = Camera.open();
    	}
    	return true;
    }
    
    /**
     * Releases the cameras resources and sets the camera property to null
     * 
     * @return True on success, false on failure
     */
    public boolean releaseCamera() {
    	if (camera != null) {
    		camera.release();
    		camera = null;
    	}
    	return true;
    }
    
    /**
     * Uses two different methods to attempt to set the camera flash mode to on/off
     * 
     * @param isOn Whether to turn the camera on, or off.
     * @return True on success, false on failure
     */
    public boolean setFlashMode(boolean isOn) {
    	if (camera == null) {
    		return false;
    	}
    	String value;
    	
    	// Attempt 1
    	if (isOn) {
    		value = Camera.Parameters.FLASH_MODE_TORCH;
    	} else {
    		value = Camera.Parameters.FLASH_MODE_OFF;
    	}
    	if (setCameraParameters(isOn, value, 1)) {
    		return true;
    	}
    	
    	// Attempt 2
    	if (isOn) {
    		value = "torch";
    	} else {
    		value = "off";
    	}
    	if (setCameraParameters(isOn, value, 2)) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Sets the cameras parameters, uses two different methods
     * 
     * @param isOn Whether the flash light should be set on, or off
     * @param value The value to use when setting the parameter
     * @param attempt The attempt number, this changes which method is used *Ugly hack, I know*
     * @return True on success, false on failure
     */
    public boolean setCameraParameters(boolean isOn, String value, int attempt) {
    	Camera.Parameters params = camera.getParameters();
    	try {
    		if (attempt == 1) {
    			params.setFlashMode(value);
    		}
    		if (attempt == 2) {
    			params.set("flash-mode", value);
    		}
    		camera.setParameters(params);
    		camera.startPreview();
    		
    		String currentMode = camera.getParameters().getFlashMode();
    		if (isOn && currentMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
    			return true;
    		}
    		if (!isOn && currentMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
    			return true;
    		}
    		return false;
    	} catch (Exception e) {
    		Log.e(TAG, "Error setting flash mode to " + value);
    		return false;
    	}
    }
    
    /**
     * Starts the ScreenTorch activity
     */
    public void startScreenTorch() {
    	Intent i = new Intent(this, ScreenTorch.class);
    	startActivity(i);
    }
    
    /**
     * Toggles the flash based on the current setting. If the flash could not be turned
     * on it will resort to using the ScreenTorch activity. Also takes into account
     * whether or not a users preference is to use the screen
     */
    public void toggleFlash() {
    	if(Prefs.getFlashType(this).equals("screen") || camera == null) {
    		Log.d(TAG, "Preferences state to use phone screen");
    		startScreenTorch();
    		return;
    	}
    	
    	torchState = !torchState;
    	if (!torchState && Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
    		this.releaseCamera();
    		this.initCamera();
    	} else if (!setFlashMode(torchState)) {
    		if (torchState) {
	    		Toast.makeText(this, "Cannot use flash, please report this error " +
	    				"with your phones model. Thanks.", Toast.LENGTH_LONG).show();
    			startScreenTorch();
    			return;
    		}
    	}
    	
    	// Decide whether or not to keep the screen on.
    	View v = findViewById(R.id.parent_ll);
    	if(torchState) {
    		v.setKeepScreenOn(true);
    		toggleButton.setText(R.string.turn_off_label);
    	} else {
    		v.setKeepScreenOn(false);
    		toggleButton.setText(R.string.turn_on_label);
    	}
    }
    
    @Override
    public void onPause() {
		// Reset the button text and torch_state
		Button toggleButton = (Button)findViewById(R.id.turn_on_button);
		toggleButton.setText(R.string.turn_on_label);
		torchState = false;
		releaseCamera();
		super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	// Attempt to reopen the camera
    	try {
    		initCamera();
    	} catch(Exception e) {
    		Log.e(TAG, "Error opening camera", e);
    		
    		releaseCamera();
    	}
    }
    
    @Override
    public void onDestroy() {
    	adview.destroy();
    	super.onDestroy();
    }
    
    /**
     * Shows a share intent menu so that the application can be shared
     */
    public void showShareMenu() {
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
    	shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
    	shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
    	shareIntent.setType("text/plain");
    	startActivity(Intent.createChooser(shareIntent, "Share"));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.about:
    		Intent i = new Intent(this, About.class);
    		startActivity(i);
    		break;
    	case R.id.share:
    		showShareMenu();
    		break;
    	case R.id.settings:
    		Intent j = new Intent(this, Prefs.class);
    		startActivity(j);
    		break;
    	}
    	
    	return false;
    }
}