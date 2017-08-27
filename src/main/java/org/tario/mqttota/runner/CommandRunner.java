package org.tario.mqttota.runner;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommandRunner {

	private String topicPrefix;
	private String topicSuffixCmd;
	private int cmdWait;

	CommandRunner(
			@Value("${mqtt.topic.prefix}") String topicPrefix,
			@Value("${mqtt.topic.suffix.otacmd}") String topicSuffixCmd,
			@Value("${mqtt.cmd.wait}") int cmdWait) {
		this.topicPrefix = topicPrefix;
		this.topicSuffixCmd = topicSuffixCmd;
		this.cmdWait = cmdWait;
	}

	public void executeReboot(String node, MqttClient client) {
		sendCommand("restart", "", node, client);
	}

	public void writeFile(String node, String filename, List<String> content, MqttClient client) {
		sendCommand("openfile", "", node, client);
		for (String line : content) {
			sendCommand("writeline", line + "\n", node, client);
		}
		sendCommand("closefile", filename, node, client);
	}

	private void sendCommand(String command, String arguments, String node, MqttClient client) {
		String content = command + ":" + arguments;
		try {
			client.publish(getCmdTopic(node), content.getBytes(), 2, false);
			Thread.sleep(cmdWait);
		} catch (MqttException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String getCmdTopic(String node) {
		return topicPrefix + node + topicSuffixCmd;
	}

}
