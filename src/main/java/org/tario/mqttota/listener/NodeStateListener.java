package org.tario.mqttota.listener;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tario.mqttota.state.OverallState;

@Component
public class NodeStateListener implements IMqttMessageListener {

	private OverallState state;
	private String topicPrefix;
	private String stateSuffix;
	private String versionSuffix;

	NodeStateListener(
			OverallState state,
			@Value("${mqtt.topic.prefix}") String topicPrefix,
			@Value("${mqtt.topic.suffix.state}") String stateSuffix,
			@Value("${mqtt.topic.suffix.version}") String versionSuffix) {
		this.state = state;
		this.topicPrefix = topicPrefix;
		this.stateSuffix = stateSuffix;
		this.versionSuffix = versionSuffix;
	}

	public String getStateTopicPath() {
		return topicPrefix + "+" + stateSuffix;
	}

	public String getVersionTopicPath() {
		return topicPrefix + "+" + versionSuffix;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String node;
		String value = new String(message.getPayload());
		if (topic.endsWith(stateSuffix)) {
			node = extractNode(topic, stateSuffix);
			state.updateNodeState(node, value);
		} else if (topic.endsWith(versionSuffix)) {
			node = extractNode(topic, versionSuffix);
			state.updateNodeVersion(node, value);
		} else {
			throw new IllegalArgumentException("Unknown topic '" + topic + "'");
		}
	}

	private String extractNode(String topic, String suffix) {
		String part = topic.substring(topicPrefix.length());
		return part.substring(0, part.length() - suffix.length());

	}

}