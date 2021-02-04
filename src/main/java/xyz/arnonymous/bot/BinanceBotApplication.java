package xyz.arnonymous.bot;

import com.binance.api.client.BinanceApiClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.arnonymous.api.VolumeMonitorUtil;
import xyz.arnonymous.json.model.CryptopingSignal;
import xyz.arnonymous.traders.BinanceTrader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static xyz.arnonymous.bot.PropertiesUtil.*;


@SpringBootApplication
@EnableScheduling
@RestController("/")
public abstract class BinanceBotApplication implements ErrorController {


    private static Logger logger = LoggerFactory.getLogger(BinanceBotApplication.class);
    static List<BinanceTrader> traders = new ArrayList<>();
    BinanceApiClientFactory factory = null;
    public static String lastVolumeMonitorCoin = "";
    public static String coin = "";
    static ApplicationContext bbotApp;


    @Autowired
    PropertiesUtil propUtil;


    // tick every 8 seconds
    @Scheduled(fixedRate = 8000)
    private void scheduleBTraders() {

        Iterator<BinanceTrader> traderListIterator = traders.iterator();
        while (traderListIterator.hasNext()) {
            BinanceTrader trader = traderListIterator.next();

            if (!trader.finished) {
                trader.tick();
            } else {
                traderListIterator.remove();
            }
        }
    }


    // tick every 50 seconds
    @Scheduled(fixedRate = 50000)
    private void scheduleVolumeMonitor() {

        //Check for Signals on  https://agile-cliffs-23967.herokuapp.com/binance
        VolumeMonitorUtil vmUtil = new VolumeMonitorUtil();

        coin = vmUtil.processVolumeMonitorMsg(numPings, netVolume, netVolumePercentage, minBuyRatio);

        if (!coin.equals("")) {
            if (!coin.equals(lastVolumeMonitorCoin)) {
                logger.info("Starting a coinAnalyzer thread");
                Thread coinAnalyzer = new Thread(bbotApp.getBean(CoinAnalyzer.class));
                coinAnalyzer.start();
            } else {
                logger.warn(String.format("Coin %s not selected, because the coin is already bought the previous iteration!!", coin));
            }
        }

    }

    @RequestMapping("/*")
    private String index() {
        return "";
    }

    @PostMapping("/cryptoping/signal")
    public void process(@RequestBody byte[] payload)
            throws Exception {

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //convert json string to object
        CryptopingSignal signal = objectMapper.readValue(payload, CryptopingSignal.class);
        String ticker = signal.getTicker(); // coin
        Double btcPrice = signal.getPrice_btc(); //price during signal

        //Message to send to api later
        String signalText = String.format("Cryptoping %s signal: %s on %s. Price (signal): %.8f, Price changed: %.8f, volume (change): %s ", signal.getType().toUpperCase(), ticker, signal.getExchange(), btcPrice, signal.getPrice_pct_change(), signal.getVolume_pct_change());

        logger.info(signalText);


        if (signal.getExchange().toLowerCase().equals("binance")) {
            coin = ticker;
            Thread coinAnalyzer = new Thread(bbotApp.getBean(CoinAnalyzer.class));
            coinAnalyzer.start();
        }

    }


    public static void main(String[] args) throws Exception {

        bbotApp = SpringApplication.run(BinanceBotApplication.class);


    }

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "";
    }

}