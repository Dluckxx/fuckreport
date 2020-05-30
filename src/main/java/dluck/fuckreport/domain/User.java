package dluck.fuckreport.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 用户实体类
 */
@Entity
@Table(name = "user")
public class User {
	@Id
	private String uid;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String email;
	@Column
	private String sessionId;
	@Column(length = 1024)
	private String centerSoftWeb;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getCenterSoftWeb() {
		return centerSoftWeb;
	}

	public void setCenterSoftWeb(String centerSoftWeb) {
		this.centerSoftWeb = centerSoftWeb;
	}
}
