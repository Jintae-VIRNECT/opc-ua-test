package com.example.opcuademo.websocket;

import redis.StatusMessage;

public class ClientSessionInfo {
	private final long createdAtTime;
	private StatusMessage statusMessage;
	private long lastPingTime;
	private long lastPongTime;

	public ClientSessionInfo(
		StatusMessage statusMessage, long lastPingTime, long lastPongTime
	) {
		this.statusMessage = statusMessage;
		this.lastPingTime = lastPingTime;
		this.lastPongTime = lastPongTime;
		this.createdAtTime = System.currentTimeMillis();
	}

	public static ClientSessionInfo createFirstConnectInfo() {
		return new ClientSessionInfo(null, 0L, 0L);
	}

	public StatusMessage getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(StatusMessage statusMessage) {
		this.statusMessage = statusMessage;
	}

	public long getLastPingTime() {
		return lastPingTime;
	}

	public void setLastPingTime(long lastPingTime) {
		this.lastPingTime = lastPingTime;
	}

	public long getLastPongTime() {
		return lastPongTime;
	}

	public void setLastPongTime(long lastPongTime) {
		this.lastPongTime = lastPongTime;
	}

	public long getLastPingPongRelayTime() {
		return Math.abs(this.lastPingTime - this.lastPongTime);
	}

	public long getCreatedAtTime() {
		return createdAtTime;
	}

	@Override
	public String toString() {
		return "{" +
			"statusMessage=" + statusMessage +
			", lastPingTime=" + lastPingTime +
			", lastPongTime=" + lastPongTime +
			'}';
	}
}
