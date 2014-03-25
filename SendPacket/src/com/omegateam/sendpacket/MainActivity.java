package com.omegateam.sendpacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.util.Log;

public class MainActivity extends Activity {
	
	private String TAG = "SendPacket";
	private SeekBar seek1;
	private SeekBar seek2;
	
	boolean withMMS = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
       try {
    	   if(withMMS)
    	   {
    		   if( connectThroughMMS() )
			    {
			    	Log.d(TAG,"Usando APN de MMS"); 
			    }
			    else 
			    { 
			    	Log.d(TAG,"No se pudo cambiar a MMS"); 
			    }
    	   }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
       seek1 = (SeekBar) findViewById(R.id.seekBar1);  
       seek1.incrementProgressBy(10);
       
       seek2 = (SeekBar) findViewById(R.id.seekBar2);
       
       final TextView seekBarValueP = (TextView)findViewById(R.id.pay);

       seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
               seekBarValueP.setText(String.valueOf(progress));
           }
           @Override
           public void onStartTrackingTouch(SeekBar seekBar){
           }
           @Override
           public void onStopTrackingTouch(SeekBar seekBar){
           }
       });
       
       final TextView seekBarValue = (TextView)findViewById(R.id.frec);

       seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
               seekBarValue.setText(String.valueOf(progress));
           }
           @Override
           public void onStartTrackingTouch(SeekBar seekBar){
           }
           @Override
           public void onStopTrackingTouch(SeekBar seekBar){
           }
       });
        
    }
    
    private boolean connectThroughMMS() throws IOException{
    	
    	Log.d(TAG, "Cambiando el APN");
    	
    	if(isNetworkAvailabe())
        {
    		int addr;
    		
    		Context context = getApplicationContext();
    		ConnectivityManager connectivityManagerM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        int resultInt = connectivityManagerM.startUsingNetworkFeature( ConnectivityManager.TYPE_MOBILE, "enableMMS" );
	        Log.d(TAG, "startUsingNetworkFeature - enableMMS: " + resultInt);
	        
	        try {
	            for (int counter=0; counter<30; counter++) {
	                State checkState = connectivityManagerM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getState();
	                if (0 == checkState.compareTo(State.CONNECTED))
	                {
	                	Log.d(TAG,"CONNECTED TYPE_MOBILE_MMS ");
	                    break;
	                }
	                Thread.sleep(200);
	            }
	        } catch (InterruptedException e) {
	            //nothing to do
	        }
	       //create a route for the specified address
	        byte[] addrBytes = new byte[]{(byte)192, (byte)241, (byte)195, (byte)144}; //192.241.195.144
	        addr = ((addrBytes[3] & 0xff) << 24)
	                | ((addrBytes[2] & 0xff) << 16)
	                | ((addrBytes[1] & 0xff) << 8 )
	                |  (addrBytes[0] & 0xff);
	            
	        Log.d(TAG,"Addr: " + addr);         
	        
	        boolean resultBool = connectivityManagerM.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, addr);
	        Log.d(TAG, "requestRouteToHost: " + resultBool);
	        if (!resultBool)
	            Log.e(TAG, "Wrong requestRouteToHost result: expected true, but was false");

	        return resultBool;
        }
        return false;    	
    }
    
    private static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        
        
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8 )
                |  (addrBytes[0] & 0xff);
        return addr;
    }
    
    private boolean isNetworkAvailabe(){
    	
    	Log.d(TAG, "Verificando conectividad: ");
    	Context context = getApplicationContext();
    	ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
         
         if(netInfo != null && netInfo.isConnected())
         {
        	return true;
         }
         else 
        	return false;
    	
    }
    
    public void onToggleClicked(View view) {

        ToggleButton toggleB = ((ToggleButton) view);
        Intent intent = new Intent(this, TimerService.class);
   
        if (toggleB.isChecked()) {
        	if(isNetworkAvailabe())
        	{
        		int pay, wtime;
        		
        		Bundle b=new Bundle();
        		
        		pay = seek1.getProgress();
        		wtime = seek2.getProgress();
        		
        		seek1.setEnabled(false);
        		seek2.setEnabled(false);
        		
        		b.putInt("Payload",pay);
        		b.putInt("Waittime", wtime);
        		intent.putExtras(b);
        		this.startService(intent);
        		
        	}
        	else
            {
	         	Toast.makeText(this, "No hay red disponible", Toast.LENGTH_SHORT).show();
	         	toggleB.toggle();
            }        	
        } else 
        {
        	this.stopService(intent);
        	seek1.setEnabled(true);
    		seek2.setEnabled(true);
        }      
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
