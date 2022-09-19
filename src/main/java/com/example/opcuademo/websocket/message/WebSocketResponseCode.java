package com.example.opcuademo.websocket.message;

public enum WebSocketResponseCode {
	// WEBSOCKET CONNECT
	CLIENT_CONNECT_SUCCESS("CONNECT", "Connect Success", 200),

	// REGISTRATION
	CLIENT_REGISTRATION_SUCCESS("REGISTER", "Registration Success", 300),
	CLIENT_REGISTRATION_FAIL("REGISTER", "Registration Fail", 301),
	CLIENT_REGISTRATION_FAIL_DUPLICATE("REGISTER", "Registration Fail. Previous Session Exist", 302),

	// REMOTE EXIT
	CLIENT_REMOTE_EXIT_SUCCESS("REMOTE_EXIT", "Remote Exit Request Success", 400),
	CLIENT_REMOTE_EXIT("REMOTE_EXIT", "Receive Remote Exit Request From Same User Id", 401),
	CLIENT_REMOTE_EXIT_FAIL("REMOTE_EXIT", "Remote Exit Client Not found. Remote Exit Fail", 402),
	CLIENT_REMOTE_EXIT_REJECT("REMOTE_EXIT", "Remote Exit Reject. Target User Status is Not Suitable To Exit", 403),

	// FORCE LOGOUT
	CLIENT_FORCE_LOGOUT("FORCE_LOGOUT", "Receive Force Logout Request From Administrator", 500),

	// WORKSPACE CHANGE
	CLIENT_WORKSPACE_UPDATE("WORKSPACE_UPDATE", "User Workspace Information Changed", 600),
	CLIENT_WORKSPACE_UPDATE_DUPLICATE_FAIL(
		"WORKSPACE_UPDATE", "User Workspace Information Change Fail. Previous User Session Exist", 601),
	CLIENT_WORKSPACE_UPDATE_FAIL("WORKSPACE_UPDATE", "Workspace Change Request Fail. Server Error", 602);

	private final String command;
	private final String message;
	private final int code;

	WebSocketResponseCode(String command, String message, int code) {
		this.command = command;
		this.message = message;
		this.code = code;
	}

	public String getCommand() {
		return command;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}
}
