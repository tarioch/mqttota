package org.tario.mqttota.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OverallState {
	private static final String UNKNOWN = "Unknown";

	private ConcurrentHashMap<String, String> nodeState = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> nodeVersion = new ConcurrentHashMap<>();

	public void updateNodeState(String node, String state) {
		nodeState.put(node, state);
	}

	public String getNodeState(String node) {
		return nodeState.getOrDefault(node, UNKNOWN);
	}

	public void updateNodeVersion(String node, String version) {
		nodeVersion.put(node, version);
	}

	public String getNodeVersion(String node) {
		return nodeVersion.getOrDefault(node, UNKNOWN);
	}

	public List<String> getAllNodes() {
		return new ArrayList<>(nodeState.keySet());
	}

}
