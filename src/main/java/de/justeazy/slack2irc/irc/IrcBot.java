package de.justeazy.slack2irc.irc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import de.justeazy.slack2irc.Bot;
import de.justeazy.slack2irc.Message;

/**
 * <p>
 * Bot to connect to an IRC network.
 * </p>
 * 
 * @author Henrik Peters
 */
public class IrcBot extends PircBot implements Bot {

	/**
	 * Logging instance
	 */
	private static Logger l = LogManager.getLogger(IrcBot.class);

	/**
	 * Properties to configure the connection to the IRC network
	 */
	private Properties properties;

	/**
	 * Support for property changes (listen for property "postedMessage" to get
	 * information about new messages in that network)
	 */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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
	 * Creates an instance of <code>IrcBot</code> with the given properties.
	 * </P>
	 * 
	 * @param properties
	 *            Properties to configure the connection
	 */
	public IrcBot(Properties properties) {
		setProperties(properties);

		this.setName(properties.getProperty("ircNick"));
		this.setVerbose(Boolean.parseBoolean(properties.getProperty("ircVerbose")));
	}

	/**
	 * <p>
	 * Overrides <code>onMessage</code> of PircBot to handle new message in the
	 * IRC network. New messages are fired for the property "postedMessage".
	 * </p>
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		Message oldPostedMessage = postedMessage != null ? postedMessage.clone() : null;
		postedMessage = new Message(sender, message);
		pcs.firePropertyChange("postedMessage", oldPostedMessage, postedMessage);
	}

	/**
	 * <p>
	 * Overrides <code>onJoin</code> of PircBot to handle Joins in the IRC
	 * network. Notifications are fired for the property "joinPartQuitMessage".
	 * </p>
	 */
	public void onJoin(String channel, String sender, String login, String hostname) {
		if (!sender.equals(this.getNick())) {
			Message oldJoinPartQuitMessage = joinPartQuitMessage != null ? joinPartQuitMessage.clone() : null;
			joinPartQuitMessage = new Message(null, sender + " has joined IRC.");
			pcs.firePropertyChange("joinPartQuitMessage", oldJoinPartQuitMessage, joinPartQuitMessage);
		}
	}

	/**
	 * <p>
	 * Overrides <code>onPart</code> of PircBot to handle Parts in the IRC
	 * network. Notifications are fired for the property "joinPartQuitMessage".
	 * </p>
	 */
	public void onPart(String channel, String sender, String login, String hostname) {
		if (!sender.equals(this.getNick())) {
			Message oldJoinPartQuitMessage = joinPartQuitMessage != null ? joinPartQuitMessage.clone() : null;
			joinPartQuitMessage = new Message(null, sender + " has parted IRC.");
			pcs.firePropertyChange("joinPartQuitMessage", oldJoinPartQuitMessage, joinPartQuitMessage);
		}
	}

	/**
	 * <p>
	 * Overrides <code>onQuit</code> of PircBot to handle Quits in the IRC
	 * network. Notifications are fired for the property "joinPartQuitMessage".
	 * </p>
	 */
	public void onQuit(String channel, String sender, String login, String hostname) {
		if (!sender.equals(this.getNick())) {
			l.trace("channel = " + channel);
			l.trace("sender = " + sender);
			l.trace("login = " + login);
			l.trace("hostname = " + hostname);
			Message oldJoinPartQuitMessage = joinPartQuitMessage != null ? joinPartQuitMessage.clone() : null;
			joinPartQuitMessage = new Message(null, channel + " has quit IRC.");
			pcs.firePropertyChange("joinPartQuitMessage", oldJoinPartQuitMessage, joinPartQuitMessage);
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
	 * Returns the last notification about Joins, Parts and Quits.
	 * </p>
	 */
	public Message getJoinPartQuitMessage() {
		return this.joinPartQuitMessage;
	}

	/**
	 * <p>
	 * Sends a message to the configured channel in the IRC network.
	 * </p>
	 */
	public void sendMessage(Message message) {
		String sendMessage = "";
		if (message.getUsername() != null) {
			sendMessage += "<" + message.getUsername() + "> ";
		}
		sendMessage += message.getContent();
		l.trace("sendMessage = " + sendMessage);
		this.sendMessage(properties.getProperty("ircChannel"), sendMessage);
	}

	/**
	 * <p>
	 * Returns a sorted array of channel usernames.
	 * </p>
	 */
	public String[] getChannelUsers() {
		User[] users = this.getUsers(properties.getProperty("ircChannel"));
		String[] usernames = new String[users.length - 1];
		int i = 0;
		for (User user : users) {
			if (!user.getNick().equals(this.getNick())) {
				usernames[i++] = user.getNick();
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
	 * Starts the bot by connecting it to the configured network. After the
	 * connection to the network is established, the bot automatically joins the
	 * configured channel.
	 * </p>
	 */
	public void run() {
		try {
			this.connect(properties.getProperty("ircServer"), Integer.parseInt(properties.getProperty("ircPort")),
					properties.getProperty("ircPassword"));
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IrcException e) {
			e.printStackTrace();
		}
		this.joinChannel(properties.getProperty("ircChannel"));
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
