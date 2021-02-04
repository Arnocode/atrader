package xyz.arnonymous.json.model;

    public class CryptopingSignal {

        /*
        ticker	            string	Crypto asset ticker on exchanges
        exchange	        string	Name of exchange in lower case
        type	            string	Signal type, can be either "up" or "down"
        price_btc	        float	Price of an asset on the exchange when the signal was generated, to Bitcoin Core (BTC), expressed as decimal.
        volume_btc_change	float	Volume change that contributed to signal generation, in BTC (reference).
        volume_pct_change	float	Change of respective kind of volume (see above) during the last hour compared to the average hourly volume for previous 24 hours, expressed as decimal.
        price_pct_change	float	Change of the asset price to its average price on the exchange for previous 24 hours, expressed as decimal. Positive for up signals, negative for down signals.
        btc_usd	            float	Price of Bitcoin Core (BTC) to USD at the time of signal generation, taken from CryptoCompare.
        count	            integer	Amount of signals of same type for same ticker on same exchange, generated for the last 7 days.
        created_at	        string	Timestamp of signal generation event, expressed as ISO 8601-formatted date/time string.

         {
          "btc_usd": 14101.1,
          "count": 1,
          "created_at": "2018-01-12T11:47:42Z+00:00",
          "exchange": "poloniex",
          "price_btc": 4.022e-05,
          "price_pct_change": -0.000263778,
          "ticker": "TICKR",
          "type": "down",
          "volume_btc_change": 2.013723252,
          "volume_pct_change": 1.332458710
        }
       */

        private String ticker;
        private String exchange;
        private String type;
        private double price_btc;
        private double btc_usd;
        private int count;
        private double volume_btc_change;
        private double volume_pct_change;
        private double price_pct_change;
        private String created_at;

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getPrice_btc() {
            return price_btc;
        }

        public void setPrice_btc(double price_btc) {
            this.price_btc = price_btc;
        }

        public double getBtc_usd() {
            return btc_usd;
        }

        public void setBtc_usd(double btc_usd) {
            this.btc_usd = btc_usd;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getVolume_btc_change() {
            return volume_btc_change;
        }

        public void setVolume_btc_change(double volume_btc_change) {
            this.volume_btc_change = volume_btc_change;
        }

        public double getVolume_pct_change() {
            return volume_pct_change;
        }

        public void setVolume_pct_change(double volume_pct_change) {
            this.volume_pct_change = volume_pct_change;
        }

        public double getPrice_pct_change() {
            return price_pct_change;
        }

        public void setPrice_pct_change(double price_pct_change) {
            this.price_pct_change = price_pct_change;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }



}
