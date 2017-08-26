package org.tario.mqttota;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tario.mqttota.listener.NodeStateListener;
import org.tario.mqttota.runner.CommandRunner;
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
	private CommandRunner runner;

	OtaPusher(
			@Value("${mqtt.server}") String mqttServer,
			@Value("${mqtt.client}") String mqttClient,
			@Value("${mqtt.user}") String mqttUser,
			@Value("${mqtt.password}") String mqttPassword,
			@Value("${mqtt.query.wait}") int queryWait,
			NodeStateListener nodeStateListener,
			OverallState state,
			CommandRunner runner) {
		this.mqttServer = mqttServer;
		this.mqttClient = mqttClient;
		this.mqttUser = mqttUser;
		this.mqttPassword = mqttPassword;
		this.queryWait = queryWait;
		this.nodeStateListener = nodeStateListener;
		this.state = state;
		this.runner = runner;
	}

	public void run() {
		MqttClient client = null;
		try {
			client = new MqttClient(mqttServer, mqttClient);
			MqttConnectOptions conOptions = new MqttConnectOptions();
			conOptions.setUserName(mqttUser);
			conOptions.setPassword(mqttPassword.toCharArray());
			client.connect(conOptions);

			client.subscribe(nodeStateListener.getStateTopicPath(), nodeStateListener);
			client.subscribe(nodeStateListener.getVersionTopicPath(), nodeStateListener);
			Thread.sleep(queryWait);
			List<String> nodes = state.getAllNodes();
			for (String node : nodes) {
				System.out.println("-------------------");
				System.out.println("Node: " + node);
				System.out.println("State: " + state.getNodeState(node));
				System.out.println("Version: " + state.getNodeVersion(node));
			}

			// runner.executeReboot("salt1", client);
			List<String> content = Files.readAllLines(Paths.get("src/main/lua/setup.lua"));
			runner.writeFile("node1", "setup.lua", content, client);
			Thread.sleep(queryWait);
		} catch (MqttException | InterruptedException | IOException e) {
			e.printStackTrace();
		} finally {
			if (client != null && client.isConnected()) {
				try {
					client.disconnect();
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
