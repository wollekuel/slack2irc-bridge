package de.justeazy.slack2irc;

/**
 * <p>
 * Class to provide data about username and content of a message.
 * </p>
 * 
 * @author Henrik Peters
 */
public class Message implements Cloneable {

	/**
	 * Username of the message
	 */
	private String username;

	/**
	 * Content of the message
	 */
	private String content;

	/**
	 * <p>
	 * Creates an instance of <code>Message</code> with a given username and
	 * content.
	 * </p>
	 * 
	 * @param username
	 *            Username
	 * @param content
	 *            Content
	 */
	public Message(String username, String content) {
		setUsername(username);
		setContent(content);
	}

	/**
	 * <p>
	 * Returns the username.
	 * </p>
	 * 
	 * @return Username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * <p>
	 * Sets the username.
	 * </p>
	 * 
	 * @param username
	 *            Username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * <p>
	 * Returns the content.
	 * </p>
	 * 
	 * @return Content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * <p>
	 * Sets the content.
	 * </p>
	 * 
	 * @param content
	 *            Content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * <p>
	 * Creates a clone of the instance.
	 * </p>
	 */
	@Override
	public Message clone() {
		return new Message(getUsername(), getContent());
	}

}
