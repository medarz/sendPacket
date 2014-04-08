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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

public class TimerService extends Service {
	
	/*********************************  App Parameters ***********************************/
	
	public String text_topico = "";
	public String text_mensaje = "";
	
	public String IMSI;
	
	private NotificationManager mNotificationManager;
	private int notificationID = 100;
	
	public static final String BROKER_URL = "tcp://192.241.195.144:1883";
	public MqttClient client;

    
    public long NOTIFY_INTERVAL; 
 
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
    	
    	TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	IMSI = mTelephonyMgr.getSubscriberId();
    	text_topico = "send/" + IMSI.substring(9);
    	
    	Log.d("SendPacket","Topic: " + text_topico);
    	
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
 
      Log.i("SendPacket", "onStartCommand() called");
      
      Bundle b=intent.getExtras();
      int payld = b.getInt("Payload");
      int waittime = b.getInt("Waittime");
      
      Log.d("SendPacket","Payload: " + payld);
      Log.d("SendPacket","Waittime: " + waittime);
      
      NOTIFY_INTERVAL = waittime * 1000;  
      for(int i=0; i< (payld-80)/10; i++)
      {
  		text_mensaje = text_mensaje + "123456789|";
  	  }
     	
      if(mTimer != null) {
          mTimer.cancel();
      } else {
          // recreate new
          mTimer = new Timer();
          connectMQTT();
          doNotification();
      }
      // schedule task
      mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);

      return super.onStartCommand(intent, flags, startId);
       
    }
    
    public void onDestroy() {
    	
        mTimer.cancel();
        cancelNotification();
        
        if(client.isConnected())
        {
	        try 
	        {
	          Toast.makeText(this, "Deteniendo", Toast.LENGTH_SHORT).show();
	  		  client.disconnect();
	  		  Log.i("SendPacket","Desconectando del servidor..."); 
	  	    }catch (MqttException e) {
	  	       e.printStackTrace();
	  	       System.exit(1);
	  	    }
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
                    //Toast.makeText(getApplicationContext(), getDateTime(),
                      //      Toast.LENGTH_SHORT).show();

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
   	    Toast.makeText(this, "Conectado",Toast.LENGTH_LONG).show();
            client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            
            Log.i("SendPacket","Conectado al servidor: " + BROKER_URL);
        } catch (MqttException e) {
        	Toast.makeText(this, "No se pudo conectar.",Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
	}
	
	public void sendMessage() {
	   
	   try {
	      publishMessage();
	     //  client.disconnect();
	   }
	   catch (MqttException e) {
		   if(e.getReasonCode()== MqttException.REASON_CODE_CONNECTION_LOST)
		   {
			   Log.e("SendPacket","Se perdio la conexion");
			   onDestroy();
		   }
		   else
		   {
		       e.printStackTrace();
		       System.exit(1);
		   }
	   } 
	}
	
	public void publishMessage() throws MqttException {
	
	   if( client.isConnected() ){
		   MqttTopic messageTopic = client.getTopic( text_topico );
		   MqttMessage message = new MqttMessage(text_mensaje.getBytes());
		   messageTopic.publish(message);
			Log.i("SendPacket","Published data. Topic: " + messageTopic.getName() + "  Message: " + text_mensaje);   
	   }
	  else
	   {
		   Toast.makeText(this, "Se perdio la conexion. Deteniendo..", Toast.LENGTH_SHORT).show();
		   onDestroy();
	   }
	  
	} 		
	
    public void doNotification(){
    	
    	// prepare intent which is triggered if the
    	// notification is selected

    	Intent intent = new Intent(this, MainActivity.class);
    	
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
    	            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	
    	PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

    	// build notification
    	// the addAction re-use the same intent to keep the example short
    	Notification n  = new Notification.Builder(this)
    	        .setContentTitle("Enviando tráfico.")
    	        .setContentText("SendPacket")
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setContentIntent(pIntent)
    	        .setAutoCancel(false).build();
    	    
    	mNotificationManager = 
    	  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	n.flags |= Notification.FLAG_NO_CLEAR;

    	mNotificationManager.notify(notificationID, n); 
    	
    }
    
    protected void cancelNotification() {
        Log.i("Cancel", "notification");
        mNotificationManager.cancel(notificationID);
     }
}