package de.justeazy.slack2irc.slack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.events.SlackDisconnected;
import com.ullink.slack.simpleslackapi.events.SlackGroupJoined;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackUserChange;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackDisconnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackGroupJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackUserChangeListener;

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
	private Message postedMessage = null;

	/**
	 * Last notification about Joins, Parts or Quits
	 */
	private Message joinPartQuitMessage = null;

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

		// add listener to get new posted messages
		slackSession.addMessagePostedListener(new SlackMessagePostedListener() {
			public void onEvent(SlackMessagePosted event, SlackSession session) {
				SlackUser messageSender = event.getSender();
				l.trace("messageSender.userName = " + messageSender.getUserName());
				if (!messageSender.getUserName().equals(getUserName())) {
					l.trace("event.messageContent = " + event.getMessageContent());

					// parse message in order to fire changing joinPartQuitMessage property or postedMessage property
					Pattern p = Pattern.compile("\\x3C\\x40\\w+\\x7C(.*)\\x3E\\shas\\s(\\w+)\\sthe\\sgroup");
					Matcher m = p.matcher(event.getMessageContent());
					if (m.find()) {
						l.trace("Firing property \"joinPartQuitMessage\"");
						Message oldJoinPartQuitMessage = joinPartQuitMessage != null ? joinPartQuitMessage.clone()
								: null;
						joinPartQuitMessage = new Message(null, m.group(1) + " has " + m.group(2) + " Slack.");
						pcs.firePropertyChange("joinPartQuitMessage", oldJoinPartQuitMessage, joinPartQuitMessage);
					} else {
						l.trace("Firing property \"postedMessage\"");
						Message oldPostedMessage = postedMessage != null ? postedMessage.clone() : null;
						postedMessage = new Message(messageSender.getUserName(), event.getMessageContent());
						pcs.firePropertyChange("postedMessage", oldPostedMessage, postedMessage);
					}
				}
			}
		});

		// add listener for debugging purposes
		slackSession.addGroupJoinedListener(new SlackGroupJoinedListener() {
			public void onEvent(SlackGroupJoined event, SlackSession session) {
				l.debug("event = " + event);
			}
		});

		// add listener for debugging purposes
		slackSession.addSlackUserChangeListener(new SlackUserChangeListener() {
			public void onEvent(SlackUserChange event, SlackSession session) {
				l.debug("event = " + event);
			}
		});

		// add listener for debugging purposes
		slackSession.addSlackConnectedListener(new SlackConnectedListener() {
			public void onEvent(SlackConnected event, SlackSession session) {
				l.debug("event = " + event);
			}
		});

		// add listener for debugging purposes
		slackSession.addSlackDisconnectedListener(new SlackDisconnectedListener() {
			public void onEvent(SlackDisconnected event, SlackSession session) {
				l.debug("event = " + event);
			}
		});
	}

	/**
	 * <p>
	 * Sends a message to the configured channel in the Slack network.
	 * </p>
	 */
	public void sendMessage(Message message) {
		String sendMessage = "";
		if (message.getUsername() != null) {
			sendMessage += "<" + message.getUsername() + "> ";
		}
		sendMessage += message.getContent();
		l.trace("sendMessage = " + sendMessage);
		slackSession.sendMessage(slackSession.findChannelByName(properties.getProperty("slackChannel")), sendMessage);
	}

	/**
	 * <p>
	 * Returns a sorted array of the usernames in the Slack channel.
	 * </p>
	 */
	public String[] getChannelUsers() {
		SlackChannel slackChannel = slackSession.findChannelByName(properties.getProperty("slackChannel"));
		Collection<SlackUser> members = slackChannel.getMembers();
		String[] usernames = new String[members.size() - 1];
		int i = 0;
		for (SlackUser member : members) {
			if (!member.getUserName().equals(getUserName())) {
				usernames[i++] = member.getUserName();
			}
		}
		Arrays.sort(usernames);
		return usernames;
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
