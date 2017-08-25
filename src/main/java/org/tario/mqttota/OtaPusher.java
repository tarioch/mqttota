package org.tario.mqttota;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtaPusher {

	private String mqttServer;
	private String mqttClient;
	private String mqttUser;
	private String mqttPassword;

	OtaPusher(
			@Value("${mqtt.server}") String mqttServer,
			@Value("${mqtt.client}") String mqttClient,
			@Value("${mqtt.user}") String mqttUser,
			@Value("${mqtt.password}") String mqttPassword) {
		this.mqttServer = mqttServer;
		this.mqttClient = mqttClient;
		this.mqttUser = mqttUser;
		this.mqttPassword = mqttPassword;
	}

	public void run() {
		try {
			MqttClient sampleClient = new MqttClient(mqttServer, mqttClient);
			MqttConnectOptions conOptions = new MqttConnectOptions();
			conOptions.setUserName(mqttUser);
			conOptions.setPassword(mqttPassword.toCharArray());
			sampleClient.connect(conOptions);

			StateListener stateListener = new StateListener();

			sampleClient.subscribe("nodemcu/+/state", stateListener);
			Thread.sleep(2000);
			sampleClient.disconnect();
		} catch (MqttException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static class StateListener implements IMqttMessageListener {

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			System.out.println(topic + ":" + new String(message.getPayload()));
		}

	}

}
