package com.omegateam.sendpacket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class PushCallback implements MqttCallback {

	private NotificationManager mNotificationManager;
	private int notificationID = 101;
    private ContextWrapper context;

    public PushCallback(ContextWrapper context) {
        this.context = context;
    }

    @Override
    public void connectionLost(Throwable cause) {
        //We should reconnect here
    	mNotificationManager.cancel(notificationID);
    }
    
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		
	    Log.i("SendPacket","Llego un mensaje y lo detecte");

	    mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int icon = R.drawable.ic_launcher;
        CharSequence text = "Mario's App";
        CharSequence contentTitle = "MQTT App";
        CharSequence contentText = message.toString();
        long when = System.currentTimeMillis();
         
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,0, intent, 0);
        Notification notification = new Notification(icon,message.toString(),when);
         
        notification.ledARGB = Color.BLUE;
        notification.ledOffMS = 300;
        notification.ledOnMS = 300;
         
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        //notification.flags |= Notification.FLAG_SHOW_LIGHTS;
         
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        mNotificationManager.notify(notificationID, notification);
  
	}
	
}