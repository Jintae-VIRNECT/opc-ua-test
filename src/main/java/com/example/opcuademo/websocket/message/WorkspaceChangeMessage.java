package com.example.opcuademo.websocket.message;

public class WorkspaceChangeMessage {
	private String userId;
	private String workspaceId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public String getUserAccessStatusKey() {
		return this.workspaceId + "_" + userId;
	}

	@Override
	public String toString() {
		return "{" +
			"userUUID='" + userId + '\'' +
			", workspaceId='" + workspaceId + '\'' +
			'}';
	}
}
