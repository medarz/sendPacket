package com.omegateam.sendpacket;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SubscriberService extends Service {
	
	public static final String 		DEBUG_TAG = "MqttService"; // Debug TAG				// Broker Port	
	public static final int			MQTT_QOS_0 = 0; // QOS Level 0 ( Delivery Once no confirmation )
	public static final int 		MQTT_QOS_1 = 1; // QOS Level 1 ( Delevery at least Once with confirmation )
	public static final int			MQTT_QOS_2 = 2; // QOS Level 2 ( Delivery only once with confirmation with handshake )

    public static final String BROKER_URL = "tcp://medarz.info:1883";

    /* In a real application, you should get an Unique Client ID of the device and use this, see
    http://android-developers.blogspot.de/2011/03/identifying-app-installations.html */
    /* Este valor se debería obtener de una base de datos (1a vez a traves de un login)*/
    public static String clientId;

    //This is the topic that has to be updated:
    public static final String TOPIC = "receive/packet";
    
    private MqttClient mqttClient; 

    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public void onCreate() {
        // cancel if already existed  	
    	Log.i("SendPacket","onCreate Subscribe >>");
    	
    	TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	clientId = mTelephonyMgr.getSubscriberId();    	
    	Log.d("SendPacket","Topic: " + TOPIC);
    	
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	Log.d("SendPacket","Conectando al servicio.");
        Toast.makeText(getApplicationContext(), "Suscrito a: receive/packet", Toast.LENGTH_LONG).show();
        try {
            mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
            mqttClient.setCallback(new PushCallback(this));
            mqttClient.connect();
            mqttClient.subscribe(TOPIC);

        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
        	Log.i("SendPacket","Desconectando del servicio");
            mqttClient.disconnect(0);
            Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}