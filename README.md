# Trading bot for Binance

Inspired by [https://github.com/unterstein/binance-trader]
Adjusted to trade based upon signals from a Binance Volume monitor: https://agile-cliffs-23967.herokuapp.com/binance


This is an experimental bot for auto trading on the [binance.com](https://binance.com) platform.
# DISCLAIMER

**This is a simple experminental bot, and I am not responsible for any money you lose!**

# How it works

The bot buys and sells using the parameters in application.properties (starting with TRADE_ and VM_) related to the Binance Volume Monitor.

**Don`t use this bot if you are holding coins in the trading currency, because the bot will sell all coins after unexpected events!**

# Prerequisites
1. You need to [create an API key](https://www.binance.com/userCenter/createApi.html) for binance.com
2. You need to have BTC (at least 0.2 is advised) and BNB (a few) balance on your account to trade an keep the transactions costs as low as possible.
3. You need to clone this repo (you need maven and java installed) and run the code
4. Encrypt your api key and secret with the command: 
```
org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="jkljklj90u390u90u90u90u90u" password=yourpwd algorithm=PBEWithMD5AndDES
```

# Configuration
```
API_KEY - the api key for you binance account - no default
API_SECRET - the api secret for you binance account - no default
TRADE_PROFIT - the profit in % 1.03 = 3%
TRADE_AMOUNT - the trading amount per action in BTC
...


```
# Build and run using Maven
```
git clone https://github.com/binance-exchange/binance-java-api.git
cd binance-java-api
mvn clean install
cd ..
git clone https://github.com/....
cd binance-trader
mvn spring-boot:run -Djasypt.encryptor.password=yourpassword
```


# Run on desktop to package:
```
mvn clean package
```

# Run on server
```
nohup java -jar -Djasypt.encryptor.password=yourpwd -Dserver.port=8888 atrader-0.0.2.jar &
```
