package redis;

import java.io.Serializable;

public class StatusMessage implements Serializable {
	private static final long serialVersionUID = 2082503192322391880L;
	private String workspaceId;
	private String userId;
	private String nickname;
	private String email;
	private String status;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserAccessStatusKey() {
		return this.workspaceId + "_" + userId;
	}

	@Override
	public String toString() {
		return "StatusMessage{" +
			"workspaceId='" + workspaceId + '\'' +
			", userId='" + userId + '\'' +
			", nickname='" + nickname + '\'' +
			", email='" + email + '\'' +
			", status='" + status + '\'' +
			'}';
	}
}