package org.tario.mqttota;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tario.mqttota.listener.NodeStateListener;
import org.tario.mqttota.state.OverallState;

@Component
public class OtaPusher {

	private String mqttServer;
	private String mqttClient;
	private String mqttUser;
	private String mqttPassword;
	private int queryWait;

	private NodeStateListener nodeStateListener;
	private OverallState state;

	OtaPusher(
			@Value("${mqtt.server}") String mqttServer,
			@Value("${mqtt.client}") String mqttClient,
			@Value("${mqtt.user}") String mqttUser,
			@Value("${mqtt.password}") String mqttPassword,
			@Value("${mqtt.query.wait}") int queryWait,
			NodeStateListener nodeStateListener,
			OverallState state) {
		this.mqttServer = mqttServer;
		this.mqttClient = mqttClient;
		this.mqttUser = mqttUser;
		this.mqttPassword = mqttPassword;
		this.queryWait = queryWait;
		this.nodeStateListener = nodeStateListener;
		this.state = state;
	}

	public void run() {
		MqttClient sampleClient = null;
		try {
			sampleClient = new MqttClient(mqttServer, mqttClient);
			MqttConnectOptions conOptions = new MqttConnectOptions();
			conOptions.setUserName(mqttUser);
			conOptions.setPassword(mqttPassword.toCharArray());
			sampleClient.connect(conOptions);

			sampleClient.subscribe(nodeStateListener.getStateTopicPath(), nodeStateListener);
			Thread.sleep(queryWait);
			List<String> nodes = state.getAllNodes();
			for (String node : nodes) {
				System.out.println("-------------------");
				System.out.println("Node: " + node);
				System.out.println("State: " + state.getNodeState(node));
			}

		} catch (MqttException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (sampleClient != null && sampleClient.isConnected()) {
				try {
					sampleClient.disconnect();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
