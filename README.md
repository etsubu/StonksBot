# StonksBot

StonksBot is a discord bot developed for a private server with features including fetching stock market data.

## Table of Contents

  * [Installation](#Installation)
    * [Prerequisites](#Prerequisites)
    * [Building](#Building)
    * [Configuration](#Configuration)
  * [Contributing](#Contributing)
  * [Commands](#Commands)

## Installation

### Prerequisites
You need to have Java 11 JDK installed on your system to run and compile the application.

### Building
```
git clone git@github.com:etsubu/StonksBot.git
cd StonksBot
./gradlew clean build
```

The application .jar file will be located in build/libs/

## Configuration

StonksBot requires you to provide discord oauth key in the configuration file. 
Place config.yaml file in the same directory with the .jar file and place the oauth key in there. 
Example: 
```
oauth: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
```

## Contributing

Some of the bots features are kind a niche such as lunch list which is due to usage and thus requirements of a 
private server. However, new commands can be easily added by creating new files in Core/Commands/ folder and 
implementing Command interface. The bot finds available commands during runtime meaning there is no need to register 
the commands in a separate place. 

Make sure the code compiles, tests pass, and verification plugins pass, before opening a Pull Request.

## Commands
TODO