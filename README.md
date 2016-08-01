# Slack2IRC Bridge ![Build Status](http://wollekuel.spdns.de:8080/buildStatus/icon?job=slack2irc-bridge)

Slack2IRC Bridge uses the [Simple Slack API](https://github.com/Ullink/simple-slack-api) to get a connection to Slack. It also uses [PircBot](http://www.jibble.org/pircbot.php) to get a connection to any given IRC server. The main task of Slack2IRC Bridge is to forward every message of Slack to IRC and vice versa.

## Usage

Simply write a configuration file as described below and start Slack2IRC Bridge by building the maven project and then call `java -jar target/<jarFile> --config=<propertiesFile>` (e.g., `java -jar target/slack2irc-0.0.1-SNAPSHOT-jar-with-dependencies.jar --config=slack2irc.config`).

## Configuration

Slack2IRC needs a proper configuration file to get connected to Slack as well as to IRC.

```
slackAuthToken=
slackChannel=irc

ircVerbose=true
ircNick=slackbot
ircServer=irc.somenetwork.net
ircPort=6667
ircPassword=
ircChannel=#somechannel
```

* `slackAuthToken`: See the Slack page about [Bot Users](https://api.slack.com/bot-users) to get know what to do in order to set up a new bot user for your Slack team (and what to do in order to get an auth token).
* `slackChannel`: Name of the Slack channel the bridge will forward IRC messages to
* `ircVerbose`: Set to `true` if you want verbose output from PircBot (note that since this is all beta there is also a lot of output from the simple-slack-api)
* `ircNick`: Username (nick) of Slack2IRC Bridge on your IRC network
* `ircServer`: IRC server to connect to
* `ircPort`: Port of the IRC server to connect to
* `ircPassword`: Specify a password if you need to (e.g., for connecting to a BNC)
* `ircChannel`: Name of the IRC channel the bridge will join and forward Slack messages to (note that there has to be a `#` sign in this case)
