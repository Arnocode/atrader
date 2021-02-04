package xyz.arnonymous.api;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class VolumeMonitorUtil {
    private static Logger logger = LoggerFactory.getLogger(VolumeMonitorUtil.class);
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String VmURL = "https://agile-cliffs-23967.herokuapp.com/ok";


    public String processVolumeMonitorMsg(int numPings, int netVolume, double netVolumePercentage, double minBuyRatio) {
        String coinName = ""; // responsevalue
        HttpResponse response = null;
        try {
            GenericUrl url = new GenericUrl(VmURL);
            HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildGetRequest(url);

            HttpHeaders headers = request.getHeaders();
            headers.set("host", "agile-cliffs-23967.herokuapp.com");
            headers.set("origin", "https://agile-cliffs-23967.herokuapp.com");
            headers.set("referer", "https://agile-cliffs-23967.herokuapp.com/binance");
            int resCode = 0;

            try {
                response = request.setHeaders(headers).execute();
                resCode = response.getStatusCode();
            }
            catch(Exception e)
            {
                logger.error("Error calling API https://agile-cliffs-23967.herokuapp.com/ok");


            }

            if (resCode != 200) {
                logger.error(String.format("Volume Monitor endpoint: %s responded with HTTP code: %d", VmURL, resCode));

            } else { // 200 OK
                InputStream is = null;
                if (response.getHeaders().getContentLength() < 17) {
                    response = request.setHeaders(headers).execute();
                    //logger.info("No signal this minute");
                    response.disconnect();
                } else {

                    String responseMsg = StringUtils.substringBetween(response.parseAsString(), "[", "]");
                    logger.info(responseMsg);

                    List<String> signalsList = Pattern.compile(",")
                            .splitAsStream(responseMsg)
                            .collect(Collectors.toList());

                    coinName = procesSignalsMessage(signalsList, numPings, netVolume, netVolumePercentage, minBuyRatio);

                    response.disconnect();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return coinName;
    }

    //Input is a list with one of multiple (coin) signals
    private String procesSignalsMessage(List<String> signalsList, int numPings, int netVolume, double netVolumePercentage, double minBuyRatio) {
        String coinResult = "";
        int numOfSignals = signalsList.size() - 1;
        int coinSelected = 0; // first item in the list of (1 to n) coins

        if (numOfSignals > 1) //multiple signals
        {
            logger.warn("Mutliple signals in one msg, trying to process..");
            //Check if there is a coin to select. If there are multiple matches the last machting coin from the list will be selected
            for (int i = 0; i < numOfSignals; i++) {
                String result = "";
                List<String> signal = Pattern.compile("\\|")
                        .splitAsStream(signalsList.get(i))
                        .collect(Collectors.toList());

                //Additional result variable to prevent that the latest 'no-match' from the list is selected as final result
                result = procesSignalsList(signal, numPings,netVolume, netVolumePercentage, minBuyRatio);
                if (!result.equals("")) {
                    coinResult = result;
                }
            }

            if (coinResult.equals("")) {
                logger.warn("No suitable coins found in this 'mult-signal list'..");
            } else {
                logger.info(String.format("%s selected as suitable buy from 'mult-signal list'..", coinResult));
            }


        } else //one signal
        {
            List<String> signalOne = Pattern.compile("\\|")
                    .splitAsStream(signalsList.get(coinSelected))
                    .collect(Collectors.toList());
            coinResult = procesSignalsList(signalOne, numPings, netVolume, netVolumePercentage, minBuyRatio);
        }

        return coinResult;
    }

    //Input is a signalmessage for one coin
    private String procesSignalsList(List<String> signalList, int numPings, double netVolume, double netVolPercentage, double minBuyRatio) {
        String coinSelected = "";
        String coin = signalList.get(0).replace("\"", ""); //remove quote at the start
        coin = coin.replaceAll("\\s+", ""); //also remove white spaces

        int pings = Integer.parseInt(signalList.get(1));
        if (pings < numPings & !signalList.get(3).startsWith("-")) { //no more then 7 pings, no negative volume

            double netVolumePercentage = Double.parseDouble(signalList.get(3).substring(0,signalList.get(3).length()-1)); //net volume percentage
            double recentTotVolume = Double.parseDouble(signalList.get(4)); //recent total volume
            double recentNetVolume = Double.parseDouble(signalList.get(6)); //recent net volume
            double buyRatio = recentNetVolume / recentTotVolume;

            if (netVolumePercentage <= netVolPercentage & recentNetVolume > netVolume & buyRatio > minBuyRatio) {
                logger.info(String.format("%s selected as buy option! Volume %.2f percent is OK", coin, netVolumePercentage));
                coinSelected = coin;

            } else {
                logger.info(String.format("%s: Volume %.02f percent or netvolume %.8f too high or buyVolume vs Total: %.8f, is too low, skipping this one", coin, netVolumePercentage, recentNetVolume, buyRatio));
            }

        } else {
            logger.info(String.format("%s: Skipping signal, because of too many pings or negative volume", coin));
        }

        return coinSelected;
    }


            /* EXAMPLE
        HTTP POST https://agile-cliffs-23967.herokuapp.com/ok
        {
    "resu": [
        "VIBE|2|1.77402615|2.7%|0.75438254|0.9%|0.73992494|16:00:24 04/01/18",
        1088
    ]
    ok2
    {
    "resu": [
        "VIBE|2|1.77402615|2.7%|0.75438254|0.9%|0.73992494|16:00:24 04/01/18",
        1088
    ]
}
bla:
{
    "resu": [
        "XRP|56|78.46032773|24.8%|2.66570827|0.7%|2.62088967|16:01:25 04/01/18",
        "RDD|10|-18.19523789|-29.9%|1.42692359|1.0%|-0.28258277|16:01:25 04/01/18",
        1577
    ]
}

}
*/


}
