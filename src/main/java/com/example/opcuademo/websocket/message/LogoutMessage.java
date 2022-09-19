package com.example.opcuademo.websocket.message;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutMessage {
	private String service;
	private String workspaceId;
	private String userId;
	private String targetUserId;
	private String event;
	private Map<String, Object> contents = new HashMap<>();

	public String getLogoutTargetUserAccessStatusKey() {
		return workspaceId + "_" + targetUserId;
	}

	@Override
	public String toString() {
		return "{" +
			"service='" + service + '\'' +
			", workspaceId='" + workspaceId + '\'' +
			", userId='" + userId + '\'' +
			", targetUserId=" + targetUserId +
			", event='" + event + '\'' +
			", contents=" + contents +
			'}';
	}
}
