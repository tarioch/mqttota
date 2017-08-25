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

	NodeStateListener(
			OverallState state,
			@Value("${mqtt.topic.prefix}") String topicPrefix,
			@Value("${mqtt.topic.suffix.state}") String stateSuffix) {
		this.state = state;
		this.topicPrefix = topicPrefix;
		this.stateSuffix = stateSuffix;
	}

	public String getStateTopicPath() {
		return topicPrefix + "+" + stateSuffix;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String node = topic.substring(topicPrefix.length());
		node = node.substring(0, node.length() - stateSuffix.length());
		String nodeState = new String(message.getPayload());
		state.updateNodeState(node, nodeState);
	}

}