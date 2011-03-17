package com.jtws.tawch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Tawch extends Activity implements OnClickListener {
	private boolean torch_state = false;
	private Camera camera;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Setup click listeners for all the buttons
        View toggleButton = findViewById(R.id.turn_on_button);
        toggleButton.setOnClickListener(this);
    }
    
    @Override
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
    
    public void onClick(View v) {
    	switch(v.getId()) {
    	case R.id.turn_on_button:
    		toggleFlash(v.getContext());
    		break;
    	}
    }
    
    /*
     * Method used to toggle the flash on/off
     * 
     * Depending on settings it should either blank screen
     * or turn the flash on 
     */
    public void toggleFlash(Context context) {
    	Button toggleButton = (Button)findViewById(R.id.turn_on_button);
    	
    	if(torch_state) {
    		// Turn torch off
    		torch_state = false;
    		toggleButton.setText(R.string.turn_on_label);
    		
    		if(camera != null) {
    			camera.release();
    		}
    		
    		// Revert the setting of keeping the screen always on
    		View v = findViewById(R.id.parent_ll);
    		v.setKeepScreenOn(false);
    	} else {
    		Log.d("org.jtws.Tawch", "Flash type pref: " + Prefs.getFlashType(context));
    		
    		// Check the preferences to see what to use
    		if(Prefs.getFlashType(context).equals("flash")) {
	    		// Turn torch on using the camera flash
    			try {
    				camera = Camera.open();
    			} catch(RuntimeException e) {
    				// If it fails to open the camera, use the screen instead
    				Intent i = new Intent(this, ScreenTorch.class);
    				startActivity(i);
    			}
    		} else {
    			camera = null;
    		}
    		
    		// Check that a camera has been found
    		if(camera != null) {
	    		// Set the flash to be always on
	    		Parameters params = camera.getParameters();
	    		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
	    		camera.setParameters(params);

        		torch_state = true;
        		
        		// When the torch is in use, keep the screen on
        		View v = findViewById(R.id.parent_ll);
        		v.setKeepScreenOn(true);

        		// Set the button to the turn off message
        		toggleButton.setText(R.string.turn_off_label);
    		} else {
    			// Turn torch on using the screen, by setting a white background
    			Intent i = new Intent(this, ScreenTorch.class);
    			startActivity(i);
    		}
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
		// Reset the button text and torch_state
		Button toggleButton = (Button)findViewById(R.id.turn_on_button);
		toggleButton.setText(R.string.turn_on_label);
		
		torch_state = false;
    	
		// Release the camera if it's in use
    	if(camera != null) {
    		camera.release();
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
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
    	case R.id.settings:
    		Intent j = new Intent(this, Prefs.class);
    		startActivity(j);
    		break;
    	}
    	
    	return false;
    }
}