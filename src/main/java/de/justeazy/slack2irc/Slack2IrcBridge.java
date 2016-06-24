package de.justeazy.slack2irc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.justeazy.slack2irc.irc.IrcBot;
import de.justeazy.slack2irc.slack.SlackBot;

/**
 * <p>
 * Slack2IRC Bridge uses the Simple Slack API to get a connection to Slack. It
 * also uses PircBot to get a connection to any given IRC server. The main task
 * of Slack2IRC Bridge is to forward every message of Slack to IRC and vice
 * versa.
 * </p>
 * 
 * @author Henrik Peters
 *
 */
public class Slack2IrcBridge implements PropertyChangeListener {

	/**
	 * Logging instance
	 */
	private static Logger l = LogManager.getLogger(Slack2IrcBridge.class);

	/**
	 * Instance of the IRC bot
	 */
	private Bot ircBot;

	/**
	 * Thread of the IRC bot
	 */
	private Thread ircThread;

	/**
	 * Instance of the Slack bot
	 */
	private Bot slackBot;

	/**
	 * Thread of the Slack bot
	 */
	private Thread slackThread;

	/**
	 * <p>
	 * Creates an instance of the bridge by initializing the IRC and the Slack
	 * bot with a given properties file.
	 * </p>
	 * 
	 * @param propertiesFile
	 *            file with connection properties
	 * @throws FileNotFoundException
	 *             If the properties file can't be found
	 * @throws IOException
	 *             If the properties file can't be read
	 */
	public Slack2IrcBridge(String propertiesFile) throws FileNotFoundException, IOException {
		Properties properties = initProperties(propertiesFile);

		ircBot = new IrcBot(properties);
		ircBot.addPropertyChangeListener(this);
		ircThread = new Thread(ircBot);

		slackBot = new SlackBot(properties);
		slackBot.addPropertyChangeListener(this);
		slackThread = new Thread(slackBot);
	}

	/**
	 * <p>
	 * Starts both bots in their respective threads.
	 * </p>
	 */
	public void start() {
		ircThread.start();
		slackThread.start();
	}

	/**
	 * <p>
	 * Loads the connection properties from the given filename.
	 * </p>
	 * 
	 * @param propertiesFile
	 *            Name of properties file
	 * @return Connection properties
	 * @throws FileNotFoundException
	 *             If the properties file can't be found
	 * @throws IOException
	 *             If the properties file can't be read
	 */
	public static Properties initProperties(String propertiesFile) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(new File(propertiesFile)));
		return properties;
	}

	/**
	 * <p>
	 * Does the processing of command events like <code>?listusers</code>.
	 * </p>
	 * 
	 * @param evt
	 *            Event
	 */
	private void processCommandEvent(PropertyChangeEvent evt) {
		Message message = (Message) evt.getNewValue();
		if (message.getContent().startsWith("?listusers")) {
			l.trace("Processing /listusers command event");
			processListusersCommandEvent(evt);
		}
	}

	/**
	 * <p>
	 * Does the processing of the <code>?listusers</code> command.
	 * </p>
	 * 
	 * @param evt
	 *            Event
	 */
	private void processListusersCommandEvent(PropertyChangeEvent evt) {
		if (evt.getSource().equals(ircBot)) {
			String[] usernames = slackBot.getChannelUsers();
			String msg = "Users in Slack: ";
			for (String username : usernames) {
				msg += username + ", ";
			}
			l.debug("msg.substring(0, msg.length() - 2) = " + msg.substring(0, msg.length() - 2));
			ircBot.sendMessage(new Message(null, msg.substring(0, msg.length() - 2)));
		} else if (evt.getSource().equals(slackBot)) {
			String[] usernames = ircBot.getChannelUsers();
			String msg = "Users in IRC: ";
			for (String username : usernames) {
				msg += username + ", ";
			}
			l.trace("msg.substring(0, msg.length() - 2) = " + msg.substring(0, msg.length() - 2));
			slackBot.sendMessage(new Message(null, msg.substring(0, msg.length() - 2)));
		}
	}

	/**
	 * <p>
	 * Implements <code>propertyChange()</code> of
	 * <code>PropertyChangeListener</code> to react upon all new messages in
	 * both networks.
	 * </p>
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		l.trace("evt.source.class = " + evt.getSource().getClass());
		if (evt.getPropertyName().equals("postedMessage")) {
			Message message = (Message) evt.getNewValue();
			if (message.getContent().startsWith("?")) {
				l.trace("Processing command event");
				processCommandEvent(evt);
			} else if (evt.getSource().equals(ircBot)) {
				slackBot.sendMessage((Message) evt.getNewValue());
			} else if (evt.getSource().equals(slackBot)) {
				ircBot.sendMessage((Message) evt.getNewValue());
			}
		}
	}

	/**
	 * <p>
	 * Parses the command line call and starts the bridge with the given
	 * properties.
	 * </p>
	 * <p>
	 * Example call: <code>java -jar <jarFile> --config=<configFile></code>
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String propertiesFile = null;

		if (args.length != 1) {
			printHelp();
		} else {
			Pattern p = Pattern.compile("\\x2D\\x2Dconfig\\x3D([\\w\\d\\x2E]+)");
			Matcher m = p.matcher(args[0]);
			if (m.find()) {
				propertiesFile = m.group(1);
			}
		}

		if (propertiesFile == null) {
			printHelp();
		} else {
			Slack2IrcBridge bridge = null;
			try {
				bridge = new Slack2IrcBridge(propertiesFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (bridge != null) {
				bridge.start();
			} else {
				System.out.println("Error starting the bridge.");
			}

		}
	}

	/**
	 * <p>
	 * Prints some usage help lines to stdout.
	 * </p>
	 */
	public static void printHelp() {
		System.out.println("Usage: java -jar <jarFile> --config=<configFile>");
		System.out.println(
				"Example: java -jar slack2irc-0.0.1-SNAPSHOT-jar-with-dependencies.jar --config=slack2irc.config");
	}

}
