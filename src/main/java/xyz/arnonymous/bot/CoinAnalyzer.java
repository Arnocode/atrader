package xyz.arnonymous.bot;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import xyz.arnonymous.traders.BinanceTrader;
import static xyz.arnonymous.bot.BinanceBotApplication.*;
import static xyz.arnonymous.bot.PropertiesUtil.*;

@Component
class CoinAnalyzer implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean done = false;

    CoinAnalyzer() {
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        done = false;
        while (!done) {
            this.checkCoin(BinanceBotApplication.coin);
        }
    }

    @Override
    public void destroy() {
        done = true;
    }



    private static Logger logger = LoggerFactory.getLogger(CoinAnalyzer.class);

    public void checkCoin(String coin) {

        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, apiSecret);
        BinanceApiRestClient client = factory.newRestClient();

        String coin2Buy = coin.toUpperCase();
        String symbol = String.format("%sBTC", coin2Buy);
        Double highest24hPrice = Double.valueOf(client.get24HrPriceStatistics(symbol).getHighPrice());

        Double currentBalance = Double.valueOf(client.getAccount().getAssetBalance(coin2Buy).getFree()) + Double.valueOf(client.getAccount().getAssetBalance(coin2Buy).getLocked());
        logger.info(String.format("Current balance of %s is: %.8f", coin2Buy, currentBalance));
        Double priceSignalToken = 0.0;

        if (currentBalance > 1.0) {
            logger.info(String.format("Coin %s already in portfolio. No action taken.", coin2Buy));
        }
        //Check if there is 0.10 BTC available for the trade
        else if (Double.valueOf(client.getAccount().getAssetBalance(baseCurrency).getFree()) < btcAmount) {
            logger.warn(String.format("Not enough (available) BTC to trade. Balance: %s", client.getAccount().getAssetBalance(baseCurrency).getFree()));
        } else //BUY ATTEMP
        {
            priceSignalToken = Double.valueOf(client.get24HrPriceStatistics(symbol).getBidPrice());
            Double btcBalance = 0.0; //not calling the binance API from here to speed up the API response

            if (priceSignalToken > (highest24hPrice * trade24High)) {
                logger.info(String.format("%s not bought, price is too close to 24h high", coin));
            } else if (BinanceBotApplication.traders.size() >= 2) {
                logger.warn("Already 2 traders active at the same time, nothing bought");
                // TelegramUtil.sendTelegramMsg("3 traders active at the same time!, Please check if the bot is still doing ok..");
                //System.exit(1);

            } else {
                String signalText = String.format("Trying to buy %s on positive Volume Monitor Signal. Current Price: %.8f", coin, priceSignalToken);
                logger.info(signalText);
                //TelegramUtil.sendTelegramMsg(signalText);
                traders.add(new BinanceTrader(client, tradeProfit, baseCurrency, coin, priceSignalToken, btcBalance, signalText));
            }

        }

        this.destroy();

    }
}
