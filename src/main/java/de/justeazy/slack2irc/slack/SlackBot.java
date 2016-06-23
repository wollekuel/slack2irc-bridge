package de.justeazy.slack2irc.slack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import de.justeazy.slack2irc.Bot;
import de.justeazy.slack2irc.Message;

/**
 * <p>
 * Bot to connect to the Slack network.
 * </p>
 * 
 * @author Henrik Peters
 */
public class SlackBot implements Bot {

	/**
	 * Logging instance
	 */
	private static Logger l = LogManager.getLogger(SlackBot.class);

	/**
	 * Properties to configure the connection to the Slack network
	 */
	private Properties properties;

	/**
	 * Support for property changes (listen for property "postedMessage" to get
	 * information about new messages in that network)
	 */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Session of the connection to the Slack network
	 */
	private SlackSession slackSession;

	/**
	 * Last posted message
	 */
	private Message postedMessage;

	/**
	 * <p>
	 * Creates an instance of <code>SlackBot</code> with the given properties.
	 * </P>
	 * 
	 * @param properties
	 *            Properties to configure the connection
	 */
	public SlackBot(Properties properties) {
		setProperties(properties);

		slackSession = SlackSessionFactory.createWebSocketSlackSession(properties.getProperty("slackAuthToken"));
		slackSession.addMessagePostedListener(new SlackMessagePostedListener() {
			public void onEvent(SlackMessagePosted event, SlackSession session) {
				SlackUser messageSender = event.getSender();
				l.trace("messageSender.userName = " + messageSender.getUserName());
				if (!messageSender.getUserName().equals(getUserName())) {
					l.trace("event.messageContent = " + event.getMessageContent());
					Message oldPostedMessage = postedMessage != null ? postedMessage.clone() : null;
					postedMessage = new Message(messageSender.getUserName(), event.getMessageContent());
					pcs.firePropertyChange("postedMessage", oldPostedMessage, postedMessage);
				}
			}
		});
	}

	/**
	 * <p>
	 * Sends a message to the configured channel in the Slack network.
	 * </p>
	 */
	public void sendMessage(Message message) {
		slackSession.sendMessage(slackSession.findChannelByName(properties.getProperty("slackChannel")),
				"<" + message.getUsername() + "> " + message.getContent());
	}

	/**
	 * <p>
	 * Adds the given property change listener to the bot.
	 * </p>
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * <p>
	 * Starts the bot by connecting it to slack session.
	 * </p>
	 */
	public void run() {
		try {
			slackSession.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Returns the last posted message.
	 * </p>
	 */
	public Message getPostedMessage() {
		return this.postedMessage;
	}

	/**
	 * <p>
	 * Returns the user name of the bot in this slack session.
	 * </p>
	 * 
	 * @return User name of the bot
	 */
	private String getUserName() {
		return slackSession.sessionPersona().getUserName();
	}

	/**
	 * <p>
	 * Returns the specified properties.
	 * </p>
	 * 
	 * @return Properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * <p>
	 * Sets the properties to connect to the network.
	 * </p>
	 * 
	 * @param properties
	 *            Properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
