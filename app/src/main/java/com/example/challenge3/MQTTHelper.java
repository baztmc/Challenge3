package com.example.challenge3;

import android.content.Context;
import android.util.Log;

import com.example.challenge3.db.DataOpenHelper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MQTTHelper {

    public interface MessageArrivedCallback {
        void onMessageArrived(String topic, MqttMessage message);
    }


    private DataOpenHelper dbHelper;
    private MessageArrivedCallback messageArrivedCallback;
    public MqttAndroidClient mqttAndroidClient;
    //final String server = "tcp://2.80.198.184:1883";
    final String server = "tcp://broker.hivemq.com:1883";
    final String TAG = "MQTT";
    private String name;

    /*public interface ConnectionCallback {
        void onConnect();
        void onConnectionFailure();
    }
    private ConnectionCallback connectionCallback;
    public void setConnectionCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }*/

    private boolean lastLedStatus = false; // Default value, assuming the LED is initially off

    public boolean getLastLedStatus() {
        return lastLedStatus;
    }

    public void setLastLedStatus(boolean status) {
        lastLedStatus = status;
    }
    public void setMessageArrivedCallback(MessageArrivedCallback callback) {
        this.messageArrivedCallback = callback;
    }



    public MQTTHelper(Context context, String name, String topic) {
        this.name = name;

        mqttAndroidClient = new MqttAndroidClient(context, server, name);

        dbHelper= new DataOpenHelper(context);

    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);



        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    //Adjusting the set of options that govern the behaviour of Offline (or Disconnected) buffering of messages
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    /*if (connectionCallback != null) {
                        connectionCallback.onConnect();
                    }*/


                    // Subscribe to the necessary topics after successful connection
                    subscribeToTopic("baz/temperature");
                    subscribeToTopic("baz/humidity");
                    subscribeToTopic("baz/led/status");
                    /*String command = "ON";
                    String topic = "baz/led/control";
                    try {
                        mqttAndroidClient.publish(topic, new MqttMessage(command.getBytes()));
                        System.out.println("baz/led/control: " + command);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }*/



                    mqttAndroidClient.setCallback(new MqttCallback() {
                        //TextView temp = (TextView) view.findViewById(R.id.temp);
                        //TextView hum = (TextView) findViewById(R.id.hum);

                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.d("file", message.toString());

                            if (messageArrivedCallback != null) {
                                messageArrivedCallback.onMessageArrived(topic, message);
                            }

                            if (topic.equals("baz/temperature")) {
                                System.out.println("temp: " + message.toString());
                                dbHelper.insertTemperature(message.toString());
                            }

                            if (topic.equals("baz/humidity")) {
                                System.out.println("hum: " + message.toString());
                                dbHelper.insertHumidity(message.toString());
                            }

                            if (topic.equals("baz/led/status")) {
                                // Update the last known LED status
                                lastLedStatus = message.toString().equals("ON");
                            }

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Failed to connect to: " + server + " " + exception.toString());
                    // Notify the callback on connection failure
                    /*if (connectionCallback != null) {
                        connectionCallback.onConnectionFailure();
                    }*/
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }


    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception subscribing");
            ex.printStackTrace();
        }
    }

    public void unsubscribeToTopic(String topic) {
        try {
            mqttAndroidClient.unsubscribe(topic, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG, "Unsubscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Unsubscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception unsubscribing");
            ex.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }


    //todo led control
    public void publishLedControl(String command) {
        String topic = "baz/led/control";
        try {
            mqttAndroidClient.publish(topic, new MqttMessage(command.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
