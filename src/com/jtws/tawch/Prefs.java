package com.jtws.tawch;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	private static final String OPT_FLASH_TYPE = "flash_type";
	private static final String OPT_FLASH_TYPE_DEF = "flash";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
		//setContentView(R.layout.preferences);
	}
	
	/**
	 * Get the current value of the flash type option
	 */
	public static String getFlashType(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(OPT_FLASH_TYPE, OPT_FLASH_TYPE_DEF);
	}
}
