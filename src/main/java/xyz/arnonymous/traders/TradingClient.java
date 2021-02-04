package xyz.arnonymous.traders;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.market.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.arnonymous.api.TelegramUtil;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class TradingClient {
  private static Logger logger = LoggerFactory.getLogger(TradingClient.class);

  private BinanceApiRestClient client;
  private String baseCurrency;
  public String tradeCurrency;
  private String symbol;

  TradingClient(BinanceApiRestClient client, String baseCurrency, String tradeCurrency) {
    this.baseCurrency = baseCurrency;
    this.tradeCurrency = tradeCurrency;
    this.client = client;
    this.symbol = tradeCurrency + baseCurrency;
  }

  // The bid price represents the maximum price that a buyer is willing to pay for a security.
  // The ask price represents the minimum price that a seller is willing to receive.
  public OrderBook getOrderBook() {
    return client.getOrderBook(symbol, 5);
  }

  public AssetBalance getBaseBalance() {
    return client.getAccount().getAssetBalance(baseCurrency);
  }

  public AssetBalance getTradingBalance() {
    return client.getAccount().getAssetBalance(tradeCurrency);
  }

  public double assetBalanceToDouble(AssetBalance balance) {
    return Double.valueOf(balance.getFree()) + Double.valueOf(balance.getLocked());
  }

  public double getAllTradingBalance() {
    AssetBalance tradingBalance = getTradingBalance();
    return assetBalanceToDouble(tradingBalance);
  }

  public boolean tradingBalanceAvailable(AssetBalance tradingBalance) {
    return assetBalanceToDouble(tradingBalance) > 1;
  }

  public List<AssetBalance> getBalances() {
    return client.getAccount().getBalances();
  }

  public List<Order> getOpenOrders() {
    OrderRequest request = new OrderRequest(symbol);
    return client.getOpenOrders(request);
  }

  public void cancelAllOrders() {
    getOpenOrders().forEach(order -> client.cancelOrder(new CancelOrderRequest(symbol, order.getOrderId())));
  }

  // * GTC (Good-Til-Canceled) orders are effective until they are executed or canceled.
  // * IOC (Immediate or Cancel) orders fills all or part of an order immediately and cancels the remaining part of the order.
  public NewOrderResponse buy(double quantity, double price) {
    String priceString = String.format("%.8f", price).replace(",", ".");
    logger.info(String.format("Buying %f for %s\n", quantity, priceString));
    NewOrder order = new NewOrder(symbol, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, "" + quantity, priceString);

    //TelegramUtil.sendTelegramMsg(String.format("Trying to buy %f %s for %s\n", quantity, tradeCurrency, priceString));
    return client.newOrder(order);
  }

  public void sell(double quantity, double price) {
    String priceString = "100000000000.0";
    String coin = symbol.substring(0,symbol.length()-3);
    //TODO: Precision based on: https://www.binance.com/api/v1/exchangeInfo
    // In stead of 5 digits for some coins hard coded
    if(symbol.toLowerCase().equals("hsrbtc") || symbol.toLowerCase().equals("saltbtc") || symbol.toLowerCase().equals("etcbtc") || symbol.toLowerCase().equals("btgbtc") || symbol.toLowerCase().equals("stratbtc") ) {
      BigDecimal bd = new BigDecimal(price).round(new MathContext(5, RoundingMode.HALF_UP));
      priceString = String.format("%.5f", bd.doubleValue()).replace(",", ".");
    }
    else //8 digits
    {
      priceString = String.format("%.8f", price).replace(",", ".");
    }
    logger.info(String.format("%s: selling %f for %s\n", coin, quantity, priceString));
    TelegramUtil.sendTelegramMsg(String.format("%s: Selling %f for %s\n", coin, quantity, priceString));
    NewOrder order = new NewOrder(symbol, OrderSide.SELL, OrderType.LIMIT, TimeInForce.GTC, "" + quantity, priceString);
    client.newOrder(order);
  }

  public void sellMarket(int quantity) {
    if (quantity > 0) {
      logger.info("Selling to MARKET with quantity " + quantity);
      NewOrder order = new NewOrder(symbol, OrderSide.SELL, OrderType.MARKET, null, "" + quantity);
      client.newOrder(order);
    } else {
      logger.info("not executing - 0 quantity sell");
    }
  }

  public Order getOrder(long orderId) {
    return client.getOrderStatus(new OrderStatusRequest(symbol, orderId));
  }

  public double lastPrice() {
    return Double.valueOf(client.get24HrPriceStatistics(symbol).getLastPrice());
  }


  public void cancelOrder(long orderId) {
    logger.info("Cancelling order " + orderId);
    //TelegramUtil.sendTelegramMsg("Cancelling order " + orderId);
    client.cancelOrder(new CancelOrderRequest(symbol, orderId));
  }

  public void panicSell(double lastKnownAmount, double lastKnownPrice) {
    logger.error("!!!! PANIC SELL !!!!");
    logger.warn(String.format("Probably selling %.8f for %.8f", lastKnownAmount, lastKnownPrice));
    cancelAllOrders();
    sellMarket(Double.valueOf(getTradingBalance().getFree()).intValue());
  }
}