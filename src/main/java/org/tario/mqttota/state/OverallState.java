package org.tario.mqttota.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OverallState {
	private ConcurrentHashMap<String, String> nodeState = new ConcurrentHashMap<>();

	public void updateNodeState(String node, String state) {
		nodeState.put(node, state);
	}

	public String getNodeState(String node) {
		return nodeState.get(node);
	}

	public List<String> getAllNodes() {
		return new ArrayList<>(nodeState.keySet());
	}
}
