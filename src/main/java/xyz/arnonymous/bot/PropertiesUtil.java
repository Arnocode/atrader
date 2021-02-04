package xyz.arnonymous.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PropertiesUtil {

    public static int numPings;
    public static int netVolume;
    public static Double netVolumePercentage;
    public static Double minBuyRatio;
    public static Double btcAmount;
    public static Double panicSellPercentage;
    public static Double tradeProfit;
    public static Double trade24High;
    public final static String baseCurrency = "BTC";
    public static String apiKey;
    public static String apiSecret;
    public static String telegramUrl;

    @PostConstruct
    public void init(){
        numPings = privNumPings;
        netVolume = privNetVolume;
        netVolumePercentage = privNetVolumePercentage;
        minBuyRatio = privMinBuyRatio;
        btcAmount = privBtcAmount;
        panicSellPercentage = privpanicSellPercentage;
        tradeProfit = privTradeProfit;
        trade24High = privTade24High;
        apiKey = privApiKey;
        apiSecret = privApiSecret;
        telegramUrl = privTelegramUrl;
    }

    @Value("${VM_PINGS}")
    private int privNumPings;

    @Value("${VM_NET_VOLUME_BTC}")
    private int privNetVolume;

    @Value("${VM_MAX_NET_VOL_PERCENTAGE}")
    private Double privNetVolumePercentage;

    @Value("${VM_BUY_RATIO}")
    private Double privMinBuyRatio;

    @Value("${TRADE_AMOUNT}")
    private Double privBtcAmount;

    @Value("${TRADE_PANIC_SELL}")
    private Double privpanicSellPercentage;

    @Value("${TRADE_PROFIT}")
    private Double privTradeProfit;

    @Value("${TRADE_24HIGH}")
    private Double privTade24High;

    @Value("${API_KEY}")
    private String privApiKey;

    @Value("${API_SECRET}")
    private String privApiSecret;

    @Value("${TELEGRAM_API}")
    private String privTelegramUrl;



}
