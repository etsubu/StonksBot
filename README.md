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
./gradlew shadowJar
```

The application .jar file will be located in build/libs/ with -all.jar suffix

For example build/libs/StonksBot-1.0-SNAPSHOT-all.jar

## Configuration

StonksBot requires you to provide discord oauth key in the configuration file. 
Place config.yaml file in the same directory with the .jar file and place the oauth key in there. 
About the available configs:
* globalAdmins:
  * This list of user names defines admin users across all servers, the bot does not check server specific
     permissions for these users and allows them to send DM messages and commands as well
* servers:
  * This list contains separate configurations for different servers where the bot has joined
  * whitelistedChannels:
    * This list defines what channels users can use for running commands with the bot
  * reactions:
    * This list contains regex patterns which are compared to all messages sent in the server and if matched the bot will
    add a reaction to that message
  * adminGroup:
    * This list defines server specific admins which can bypass the whitelistedChannels restriction
  * trustedGroup:
    * Less permissive version of the adminGroup allows to bypass whitelistedChannels restriction
    
Example: 

```
# Example configuration file which is also used for testing
oauth: AAAAAAAAAAAAAAAAAAAAAAAAAAA
globalAdmins:
  - nagrodus
servers:
  - name: NagrodusDev
    whitelistedChannels:
      - bot-kyselyt
    reactions:
      - message: .*test.*
        react: STONKS
    adminGroup: admin
    trustedGroup: trusted
```

## Contributing

Some of the bots features are kind a niche such as lunch list which is due to usage and thus requirements of a 
private server. However, new commands can be easily added by creating new files in Core/Commands/ folder and 
implementing Command interface. The bot finds available commands during runtime meaning there is no need to register 
the commands in a separate place. 

Make sure the code compiles, tests pass, and verification plugins pass, before opening a Pull Request.

## Commands

Stock names are resolved by using search functionality in yahoo finance and the result is cached. This 
means that you can either use the stock ticker for searching or the prefix of the full stock name.
* `!price/hinta/p [stock]`
    * This will query the intraday price data for the given stock
    * Example: `!price msft`
        * This will query price data for Microsoft
        * Example response: 
            ```
          MSFT
          Price: 213.02
          Change: -0.73%
          Open: 213.86
          High: 216.25
          Low: 212.85
          Volume: 36 152 200
          ```
* `!calendar/kalenteri/c [stock]`
    * This command will query calendar event such earnings date, dividend date and EPS forecasts
    Note that only fields that are available are displayed
    * Example: `!calendar msft`
        * This will query calendar events for Microsoft
        * Example response:
            ```
          Earnings date: 21-10-2020
          Forecast EPS avg: 1,54
          Forecast EPS high: 1,61
          Forecast EPS low: 1,49
          Forecast Revenue avg: 35 689 500 000
          Forecast Revenue high: 36 394 000 000
          Forecast Revenue low: 35 299 100 000
          Previous dividend date: 19-08-2020
          Next dividend date: 10-09-2020
          ```
* `!commands/komennot`
   * This command lists all available commands in the bot. 
   * Example: `!commands`
        * Example response:
            ```
          !about/tietoja
          !calendar/kalenteri
          !help/apua
          !commands/komennot
          !lunch/lounas
          !price/hinta
          !stats/tilastot
          ```
* `!help/apua [command name]`
   * Displays help text for the given command, describing what it does and how to use it.
   * Example: `!help price`
        * Example response:
            ```
          Displays intraday price data for the given stock
          Usage !price/hinta [stockname/ticker]
          Example: !price msft
          ```
* `!about/tietoja`
   * Displays name and version number of the bot, author name and link to the github project.
   * Example: `!about`
        * Example response:
            ```
          StonksBot - 1.0-SNAPSHOT
          Author Jarre Leskinen
          Source code and documentation: https://github.com/etsubu/StonksBot
          ```
* `!stats/tilastot/s`
   * Displays key statistics about the given stock
   * Example: `!stats msft`
        * Example response:
            ```
          Microsoft Corporation - MSFT
          EV/EBITDA: 23.87
          Forward P/E: 29.02
          P/B: 13.63
          Beta: 0.9
          Profit margin: 30.96%
          Short percent of float: 0.49%
          Recent change in short ratio: -15.44%
          Held by insiders: 1.42%
          Held by institutions: 74.09%
          52 week price change: 57.27%
          ```
* `!suositus/recommendation`
   * Displays price target and recommendation for the given stock by [Inderes](https://www.inderes.fi/). 
   Note that the stock needs to be followed by inderes so stocks that are not listed in OMXH or First North 
   don't have the information available.
   * Example: `!suositus neste`
        * Example response:
            ```
          (Inderes)
          Nimi: Neste
          Suosituksen päivämäärä: 24.7.2020
          Tavoitehinta: 40€
          Nykyinen hinta: 43.88€
          Nousuvara: -8.84%
          Suositus: vähennä
          ```
* `!lunch/lounas`
    * This command will display the lunch list for a couple of restaurants at University of Jyväskylä. 
    As mentioned previously, some of the bot's features are really niche. Before 18PM EEST the command 
    returns the current days lunch list, and after 18PM it displays next days lunch list.
    * Example `!lunch`
        * This returns the lunch list
        * Example response: 
            ```
          Ravintola Piato
          2020-08-19
          KASVISLOUNAS
              Pinaattiohukaisia (* ,A)
              Puolukkahilloa (G ,L ,M ,Veg)
          LOUNAS
              Sinappi-porsaspataa (* ,A ,L)
          LOUNAS
              Katkarapuja ja kasviksia itämaisessa kastikkeessa (G ,L ,M)
          PAISTOPISTE
          JÄLKIRUOKA
              Passionrahkaa (A ,G ,L)
          LOUNAS
              Paahdettua kirjolohta A, G, L ()
          
          Ravintola Maija
          2020-08-19
          KASVISLOUNAS
              Kikherne-bataattipataa (* ,A ,G ,L ,M ,Veg ,VS)
              Höyrytettyä tummaa riisiä (* ,G ,L ,M ,Veg)
          LOUNAS
              Porsas-paprikakastiketta (* ,A ,G ,L)
              Keitettyjä perunoita (* ,G ,L ,M ,Veg)
          LOUNAS
              Broileri-fetapizzaa BBQ (A ,VL ,VS)
              Basilikaöljyä (* ,G ,L ,M ,Veg)
          KEITTOLOUNAS
              Aasialaista lohi-seitikeittoa (* ,A ,G ,L ,M)
          JÄLKIRUOKA
              Mansikkarahkaa (A ,G ,L)
          ```
        
