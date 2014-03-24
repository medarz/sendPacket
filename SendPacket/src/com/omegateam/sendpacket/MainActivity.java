package com.omegateam.sendpacket;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;
import android.util.Log;


public class MainActivity extends Activity {
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
    }
    
    public void onToggleClicked(View view) {
    	
    	Log.i("SendPacket","Toggle");

        boolean on = ((ToggleButton) view).isChecked();
        Intent intent = new Intent(this, TimerService.class);
        
        if (on) {
        	this.startService(intent);
        } else {
        	this.stopService(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    

    
}
