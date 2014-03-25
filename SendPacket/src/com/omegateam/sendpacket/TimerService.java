package com.omegateam.sendpacket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service {
	
	/*********************************  App Parameters ***********************************/
	
	public String text_topico = "send/packet";
	public String text_mensaje = "";
	
	public static final String BROKER_URL = "tcp://192.241.195.144:1883";
	public MqttClient client;

    // constant
    public static final long NOTIFY_INTERVAL = 2 * 1000; // 10 seconds
 
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
 
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 
    @Override
    public void onCreate() {
        // cancel if already existed  	
    	Log.i("SendPacket","onCreate >>");
    	int i;
    	
    	for(i=0; i<12; i++){
    		text_mensaje = text_mensaje + "123456789|";
    	}
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
 
      Log.i("SendPacket", "onStartCommand() called");
      
      Bundle b=intent.getExtras();
      int payld = b.getInt("Payload");
      int waittime = b.getInt("Waittime");
      
      Log.d("SendPacket","Payload: " + payld);
      Log.d("SendPacket","Waittime: " + waittime);
     	
      if(mTimer != null) {
          mTimer.cancel();
      } else {
          // recreate new
          mTimer = new Timer();
          connectMQTT();
      }
      // schedule task
      mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);

      return super.onStartCommand(intent, flags, startId);
       
    }
    
    public void onDestroy() {
        Toast.makeText(this, "Deteniendo", Toast.LENGTH_SHORT).show();
        mTimer.cancel();
        
        try 
        {
  		  client.disconnect();
  		  Log.i("SendPacket","Desconectando del servidor..."); 
  	    }catch (MqttException e) {
  	       e.printStackTrace();
  	       System.exit(1);
  	    }
   }
    
 
    class TimeDisplayTimerTask extends TimerTask {
 
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
 
                @Override
                public void run() {
                    // display toast
                    Toast.makeText(getApplicationContext(), getDateTime(),
                            Toast.LENGTH_SHORT).show();

                    sendMessage(); 
                }   
            });
        }
        
        private String getDateTime() {
            // get date time in custom format
            SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]");
            return sdf.format(new Date());
        }
    }
    
    public void connectMQTT() {
	   	
   	 try {
   	    Toast.makeText(this, "Conectando",Toast.LENGTH_LONG).show();
            client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            Log.i("SendPacket","Conectado al servidor: " + BROKER_URL);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
	}
	
	public void sendMessage() {
	   
	   try {
	      Log.i("SendPacket","Conectando");
	      publishMessage();
	     //  client.disconnect();
	   } catch (MqttException e) {
	       e.printStackTrace();
	       System.exit(1);
	   }
	}
	
	public void publishMessage() throws MqttException {
	
	   MqttTopic messageTopic = client.getTopic( text_topico );
	   MqttMessage message = new MqttMessage(text_mensaje.getBytes());
	   messageTopic.publish(message);
		Log.i("SendPacket","Published data. Topic: " + messageTopic.getName() + "  Message: " + text_mensaje);
	} 		
}