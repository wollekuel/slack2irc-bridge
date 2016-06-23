package de.justeazy.slack2irc;

import java.beans.PropertyChangeListener;

/**
 * <p>
 * Interface for the bots connected to Slack and IRC.
 * </p>
 * 
 * @author Henrik Peters
 */
public interface Bot extends Runnable {

	/**
	 * <p>
	 * Sends a message to the network.
	 * </p>
	 * 
	 * @param message
	 *            Message to send
	 */
	public void sendMessage(String message);

	/**
	 * <p>
	 * Adds a listener to the bot to get a notification on new messages in that
	 * network.
	 * 
	 * @param listener
	 *            New posted message listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * <p>
	 * Returns the last posted message in that network.
	 * </p>
	 * 
	 * @return Last posted Message
	 */
	public String getPostedMessage();

}
