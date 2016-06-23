package de.justeazy.slack2irc;

public class Message implements Cloneable {

	private String username;
	private String content;

	public Message(String username, String content) {
		setUsername(username);
		setContent(content);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public Message clone() {
		return new Message(getUsername(), getContent());
	}

}
