package org.tario.mqttota;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
	private String luaFolder;

	private NodeStateListener nodeStateListener;
	private OverallState state;
	private CommandRunner runner;
	private MqttClient client;

	OtaPusher(
			@Value("${mqtt.server}") String mqttServer,
			@Value("${mqtt.client}") String mqttClient,
			@Value("${mqtt.user}") String mqttUser,
			@Value("${mqtt.password}") String mqttPassword,
			@Value("${mqtt.query.wait}") int queryWait,
			@Value("${lua.folder}") String luaFolder,
			NodeStateListener nodeStateListener,
			OverallState state,
			CommandRunner runner) {
		this.mqttServer = mqttServer;
		this.mqttClient = mqttClient;
		this.mqttUser = mqttUser;
		this.mqttPassword = mqttPassword;
		this.queryWait = queryWait;
		this.luaFolder = luaFolder;
		this.nodeStateListener = nodeStateListener;
		this.state = state;
		this.runner = runner;
	}

	@PostConstruct
	public void postConstruct() {
		try {
			client = new MqttClient(mqttServer, mqttClient);
			MqttConnectOptions conOptions = new MqttConnectOptions();
			conOptions.setUserName(mqttUser);
			conOptions.setPassword(mqttPassword.toCharArray());
			client.connect(conOptions);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void preDestroy() {
		if (client != null && client.isConnected()) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateNode(String node) {
		System.out.println("Updating node " + node);

		Path baseDir = Paths.get(luaFolder, "base");
		sendFiles(baseDir, node);

		Path nodeDir = Paths.get(luaFolder, "nodes", node);
		if (Files.isDirectory(nodeDir)) {
			sendFiles(nodeDir, node);
		}

		runner.executeReboot(node, client);
	}

	private void sendFiles(Path path, String node) {
		try {
			Files.list(path).forEach((p) -> writeFile(p, node));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeFile(Path path, String node) {
		try {
			List<String> content = Files.readAllLines(path);
			Path fileName = path.getFileName();
			System.out.println("Sending " + fileName);
			runner.writeFile(node, fileName.toString(), content, client);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readState() {
		try {
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
		} catch (MqttException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
