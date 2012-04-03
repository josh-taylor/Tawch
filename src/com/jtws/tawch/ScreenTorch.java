package com.jtws.tawch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ScreenTorch extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(new ScreenCanvas(this));
	}
	
	static public class ScreenCanvas extends View {
		private int shade = 255;
		
		public ScreenCanvas(Context context) {
			super(context);
			
			setFocusable(true);
			setFocusableInTouchMode(true);
			
			setKeepScreenOn(true);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// Set the new shade depending on where the user has pressed
			float y = event.getY();
			shade = (int) (((getHeight() - y) / getHeight()) * 255);
			
			// Force redraw
			invalidate();
			
			return true;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			// Draw the background
			Paint background = new Paint();
			background.setARGB(255, shade, shade, shade);
			
			canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		}
	}
}
