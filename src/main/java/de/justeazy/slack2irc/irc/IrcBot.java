package de.justeazy.slack2irc.irc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Properties;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import de.justeazy.slack2irc.Bot;

/**
 * <p>
 * Bot to connect to an IRC network.
 * </p>
 * 
 * @author Henrik Peters
 */
public class IrcBot extends PircBot implements Bot {

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
	private String postedMessage = null;

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
		String oldPostedMessage = postedMessage == null ? null : new String(postedMessage);
		postedMessage = "<" + sender + "> " + message;
		pcs.firePropertyChange("postedMessage", oldPostedMessage, postedMessage);
	}

	/**
	 * <p>
	 * Returns the last posted message.
	 * </p>
	 */
	public String getPostedMessage() {
		return this.postedMessage;
	}

	/**
	 * <p>
	 * Sends a message to the configured channel in the IRC network.
	 * </p>
	 */
	public void sendMessage(String message) {
		this.sendMessage(properties.getProperty("ircChannel"), message);
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
