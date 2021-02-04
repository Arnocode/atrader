package xyz.arnonymous.traders;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.market.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.arnonymous.api.TelegramUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import static xyz.arnonymous.bot.PropertiesUtil.*;


public class BinanceTrader {

    private static Logger logger = LoggerFactory.getLogger(BinanceTrader.class);

    private TradingClient client;
    private final String tradeCurrency;
    private final double tradeProfit;
    private final double tradeAmount;
    private Double currentlyBoughtPrice;
    private Long orderId;
    private int panicBuyCounter;
    private int partialBuyCounter;
    private double trackingLastPrice;
    private double buyPrice;
    public boolean finished;


    public BinanceTrader(BinanceApiRestClient restClient, double tradeProfit, String baseCurrency, String tradeCurrency, double buyPrice, double btcBalance, String signalText) {


        TelegramUtil.sendTelegramMsg(signalText);
        logger.info(signalText);

        client = new TradingClient(restClient, baseCurrency, tradeCurrency);

        btcBalance = Double.valueOf(client.getBaseBalance().getFree());

        String symbol = String.format("%sBTC", tradeCurrency);
        Double currentPrice = Double.valueOf(client.lastPrice());

        Double buyAmount = (btcAmount / currentPrice);
        logger.info(String.valueOf(buyAmount));
        BigDecimal bd = new BigDecimal(Double.toString(buyAmount));
        bd = bd.setScale(0, RoundingMode.HALF_UP);

        logger.info(String.format("BTCbalance: %.8f, max amount of %s to buy is: %.2f  current price: %.8f", btcBalance, tradeCurrency, buyAmount, currentPrice));
        //TelegramUtil.sendTelegramMsg(String.format("BTCbalance: %.8f, max amount of %s to buy is: %.2f.  current price: %.8f", btcBalance, ticker, maxRoundAmount));

        trackingLastPrice = client.lastPrice();
        this.tradeAmount = bd.doubleValue();
        this.tradeProfit = tradeProfit;
        this.tradeCurrency = tradeCurrency;
        this.buyPrice = buyPrice;
        this.finished = false;

        clear();
    }

    public void tick() {
        double lastPrice = 0;

        try {
            OrderBook orderBook = client.getOrderBook();
            lastPrice = client.lastPrice();
            AssetBalance tradingBalance = client.getTradingBalance();
            //double lastKnownTradingBalance = client.getAllTradingBalance();
            double lastBid = Double.valueOf(orderBook.getBids().get(0).getPrice()); // last buy price (bid)
            double lastAsk = Double.valueOf(orderBook.getAsks().get(0).getPrice()); // last sell price (ask)
            double sellPrice = lastAsk;
            double profitablePrice = buyPrice * tradeProfit;

            //logger.info(String.format("%s: SignalBuyPrice:%.8f sellPrice:%.8f bid:%.8f ask:%.8f price:%.8f profit:%.8f diff:%.8f\n", tradeCurrency, buyPrice, sellPrice, lastAsk, lastAsk, lastPrice, profitablePrice, (lastAsk - profitablePrice)));

            if (orderId == null) {
                logger.info("Nothing bought, let`s check");
                // find a burst to buy
                // but make sure price is ascending!
                //if (lastAsk >= profitablePrice) {
                if (lastBid <= (buyPrice * 1.01) || buyPrice < 0.0) {
                    if (lastPrice >= trackingLastPrice) {

                        logger.info(String.format("Buy opportunity detected. Buy price: %.8f, sell if the price is: %.8f", lastBid, profitablePrice));
                        //currentlyBoughtPrice = profitablePrice;
                        currentlyBoughtPrice = lastBid;
                        orderId = client.buy(tradeAmount, lastBid).getOrderId();
                        panicBuyCounter = 0;
                        partialBuyCounter = 0;
                    } else {
                        logger.warn("Price is falling! don`t buy");
                        this.finished = true;
                    }
                } else {
                    logger.info(String.format("Nothing bought price is already too high: %.8f\n", lastAsk));
                    currentlyBoughtPrice = null;
                }
            } else {
                Order order = client.getOrder(orderId);
                OrderStatus status = order.getStatus();
                if (status != OrderStatus.CANCELED) {
                    // not new and not canceled, check for profit
                    //logger.info("Tradingbalance: " + tradingBalance);
                    if ("0".equals("" + tradingBalance.getLocked().charAt(0)) &&
                            lastAsk >= currentlyBoughtPrice) {
                        if (status == OrderStatus.NEW) {
                            // nothing happened here, maybe cancel as well?
                            panicBuyCounter++;
                            logger.info(String.format("order still new, time %d\n", panicBuyCounter));
                            if (panicBuyCounter > 19) {
                                logger.info(String.format("Cancelling order. %s not bought for: %.8f", tradeCurrency, currentlyBoughtPrice));
                                client.cancelOrder(orderId);
                                clear();
                                this.finished = true;
                            }
                        } else {
                            if  (status == OrderStatus.PARTIALLY_FILLED || status == OrderStatus.FILLED) {
                                //logger.info("Order filled with status " + status);
                                //PROFIT
                                if(status == OrderStatus.PARTIALLY_FILLED){
                                    partialBuyCounter++;
                                    if(partialBuyCounter>19)
                                    {
                                        logger.warn("Only partial buy, cancelling remaining order and selling");
                                        TelegramUtil.sendTelegramMsg(String.format("%s: only partial buy, cancelling remaining order and selling", tradeCurrency));
                                        client.cancelOrder(orderId);
                                        client.sell(Math.floor(Double.valueOf(tradingBalance.getFree())), sellPrice);
                                        clear();
                                        this.finished = true;
                                    }
                                }
                                else if (lastAsk >= profitablePrice) {
                                    logger.info("gaining profitable profits SELL!!");
                                    client.sell(Math.floor(Double.valueOf(tradingBalance.getFree())), sellPrice);
                                    clear();
                                    this.finished = true;
                                    //(STOP) LOSS
                                } else if (lastAsk < (profitablePrice * panicSellPercentage)) {
                                    logger.info("Not gaining enough profit anymore, let`s sell");
                                    logger.info(String.format("Bought %.8f for %.8f and sell it for %.8f, this is %.8f coins profit", tradeAmount, currentlyBoughtPrice, sellPrice, (1.0 * currentlyBoughtPrice - sellPrice) * tradeAmount));
                                    client.sell(Double.valueOf(tradingBalance.getFree()), sellPrice);
                                    clear();
                                    this.finished = true;
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("Order was canceled, cleaning up.");
                    clear(); // Order was canceled, so clear and go on
                }
            }
        } catch (Exception e) {
            logger.error("Unable to perform ticker", e);
        }
        trackingLastPrice = lastPrice;

    }


    private void panicSellForCondition(double lastPrice, double lastKnownTradingBalance, boolean condition) {
        if (condition) {
            logger.info("panicSellForCondition");
            client.panicSell(lastKnownTradingBalance, lastPrice);
            clear();
        }
    }

    private void clear() {
        panicBuyCounter = 0;
        partialBuyCounter = 0;
        orderId = null;
        currentlyBoughtPrice = null;
    }

    List<AssetBalance> getBalances() {
        return client.getBalances();
    }
}