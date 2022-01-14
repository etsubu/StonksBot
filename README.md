# StonksBot

StonksBot is a discord bot developed for community server of investors and traders. 
The features focus on fetching stock market related data and calculating different key ratios or values.

## Table of Contents

  * [Installation](#Installation)
    * [Prerequisites](#Prerequisites)
    * [Building](#Building)
    * [Configuration](#Configuration)
  * [Contributing](#Contributing)
  * [Commands](#Commands)
  * [Search](#Search)

## Installation

### Prerequisites
You need to have Java 17 JDK installed on your system to run and compile the application.

### Building

```
git clone git@github.com:etsubu/StonksBot.git
cd StonksBot
./gradlew clean build
./gradlew shadowJar
```

The application .jar file will be located in build/libs/ with -all.jar suffix

For example build/libs/StonksBot-1.0-SNAPSHOT-all.jar

## Configuration

StonksBot requires you to provide discord oauth key in the configuration file. 
Place config.yaml file in the same directory with the .jar file and place the oauth key in there. 
About the available configs:
* globalAdmins:
  * This list of user ids defines admin users across all servers, the bot does not check server specific
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

Note that instead of names, the configs require you to use ID values. The benefit is that if channel/user/server names change 
this does not affect the unique id values. In order to be able to copy ID values you need to enable developer mode in discord.
Instructions on how to do this are provided at https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-
    
Example: 

```
# Example configuration file which is also used for testing
oauth: oath_key_for_the_discord_bot_goes_here
# bot admins
globalAdmins:
  - user_id_that_is_admin_in_every_server
omxhNews:
  enabled: true/false
shareville:
  enabled: true/false
servers:
  # configs for a single server
  - name: server_id
    newsChannel: channel_id_to_post_disclosure_items
    whitelistedChannels:
      - channel_id_that_everyone_is_allowed_to_use_for_commands
    adminGroup: admin_group_id
    trustedGroup: 00
    recommendationChannel: some_channel_id
    filters:
      patterns:
        - (http|https|www).*(some-blackisted-word-in-url).*
      notifyChannel: channel_id_to_post_filter_action_notifications
    shareville:
      sharevilleProfiles:
        - some_user_id
      sharevilleChannel: some_channel_id
    requiredRoleOrders:
      - some_role_id_that_should_be_above
      - some_role_id_that_should_be_below
```

## Contributing

Some of the bots features are kind a niche such as lunch list which is due to usage and thus requirements of a 
private server. However, new commands can be easily added by creating new files in com.stonksbot.core/Commands/ folder and 
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
* `!ratios/suhdeluvut/r`
   * Display key ratios about liquidity and value of the requested stock
   * Example: `!ratios msft`
       * Example response:
          ```
         Microsoft Corporation - MSFT
         Current ratio: 2.53
         ROA: 15.78%
         FCF yield: 33.05 / 3.03%
         Gross Profit Margin: 68.27%
         P/S: 11.21
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
* `!fscore/fluku/f`
  * Display [Piotroski Score](https://www.investopedia.com/terms/p/piotroski-score.asp), also known as F-score for the given stock.
  * Example: `!fscore msft`
       * Example response:
            ```
            Microsoft Corporation - MSFT
            Positive net income: 1
            Positive operating cash flow: 1
            Operating cash flow > net income: 1
            Positive ROA: 1
            Decrease of long term debt: 1
            Higher current ratio: 0
            No new shares issued: 1
            Higher gross profit margin: 1
            Higher asset turnover ratio: 0
            Score: 7
            Checked criterias: 9
            ```

* `!bio/kuvaus/b`
    * Display long business description for the requested company
    * Example: `!bio microsoft`
        * Example response:
            ```
            Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide. Its Productivity and Business Processes segment offers Office, Exchange, SharePoint, Microsoft Teams, Office 365 Security and Compliance, and Skype for Business, as well as related Client Access Licenses (CAL); Skype, Outlook.com, OneDrive, and LinkedIn; and Dynamics 365, a set of cloud-based and on-premises business solutions for small and medium businesses, large organizations, and divisions of enterprises. Its Intelligent Cloud segment licenses SQL and Windows Servers, Visual Studio, System Center, and related CALs; GitHub that provides a collaboration platform and code hosting service for developers; and Azure, a cloud platform. It also offers support services and Microsoft consulting services to assist customers in developing, deploying, and managing Microsoft server and desktop solutions; and training and certification to developers and IT professionals on various Microsoft products. Its More Personal Computing segment provides Windows original equipment manufacturer (OEM) licensing and other non-volume licensing of the Windows operating system; Windows Commercial, such as volume licensing of the Windows operating system, Windows cloud services, and other Windows commercial offerings; patent licensing; Windows Internet of Things; and MSN advertising. It also offers Surface, PC accessories, PCs, tablets, gaming and entertainment consoles, and other devices; Gaming, including Xbox hardware, and Xbox content and services; video games and third-party video game royalties; and Search, including Bing and Microsoft advertising. It sells its products through OEMs, distributors, and resellers; and directly through digital marketplaces, online stores, and retail stores. It has a strategic collaboration with DXC Technology. The company was founded in 1975 and is headquartered in Redmond, Washington.
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
        
## Search

The commands are able to fetch stock data by either their name, ticker or ISIN code, the search functionality uses 
yahoo finance's search API with exception being !recommendation command which uses inderes API.
Using ticker or ISIN is the most precise way to define what stock should be queried but using prefix of the name 
is also supported but the results might be unexpected.