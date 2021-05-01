package xyz.arnonymous.api;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import static xyz.arnonymous.bot.PropertiesUtil.*;

public class TelegramUtil {

    private static final Logger logger;

    static {
        logger = LoggerFactory.getLogger(TelegramUtil.class);
    }

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public static void sendTelegramMsg(String msg) {
        try {
            String telegramApiURL = String.format("%s&parse_mode=HTML&text=%s", telegramUrl, msg);
            GenericUrl url = new GenericUrl(telegramApiURL);
            HttpRequest request = HTTP_TRANSPORT.createRequestFactory().buildGetRequest(url);
            HttpResponse response = request.execute();
            System.out.println(response.getStatusCode());

            InputStream is;
            is = response.getContent();

            int ch;
            while ((ch = is.read()) != -1) {
                System.out.print((char) ch);
            }
            response.disconnect();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
