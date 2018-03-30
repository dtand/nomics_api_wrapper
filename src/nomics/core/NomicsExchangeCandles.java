package nomics.core;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wrapper class for the nomics exchange candles API.  This class includes
 * the vanilla API call sample along with any layer 2 filters for additional
 * functionality.  The API returns a list of klines.  An API example is below:
 * 
 * HTTPS GET: https://api.nomics.com/v1/exchange_candles?key=1234&interval=1h&exchange=gdax&market=BTC-USD
 * [
 * 	{
 *   "timestamp": "2018-03-19T10:00:00Z",
 *   "low": "7024.32225",
 *   "open": "8276.19407",
 *   "close": "8281.17307",
 *   "high": "8566.43000",
 *   "volume": "59624801"
 * 	},
 * ]
 * @author danielanderson
 *
 */
public class NomicsExchangeCandles {

	/**
	 * URL for grabbing all the aggregated candles for a specific currency
	 */
	private static final String URL = "https://api.nomics.com/v1/exchange_candles?key=%s&interval=%s&exchange=%s&market=%s";
	
	/**
	 * Method to grab all the candles by exchange for provided currency and interval. Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param key			The API key
	 * @param interval		The iterval for the kline as a string: Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param exchange		The id for the exchange ie. "binance", "gdax" ...
	 * @param symbol			The symbol for the currency of iterest, ie: "ETH", "LTC", "BTC"
	 * @return				A string of klines representing a JSONArray
	 * @throws IOException
	 */
	public String getExchangeCandles( String key, String interval, String exchange, String symbol ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key, interval, exchange, symbol );
		return httpsClient.doGet( formattedURL );
	}
	
	/**
	 * Second level filter to grab the most recent candle from a given exchange and currency
	 * @param key			API key
	 * @param interval		Kline interval: Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param exchange		The id for the exchange ie. "binance", "gdax" ...
	 * @param symbol			The symbol for the currency of iterest, ie: "ETH", "LTC", "BTC"
	 * @return				A string of klines representing a JSONArray
	 * @throws JSONException
	 * @throws IOException
	 */
	public String getMostRecentCandle( String key, String interval, String exchange, String symbol ) throws JSONException, IOException
	{
		JSONArray allCandles = new JSONArray ( getExchangeCandles( key, interval, exchange, symbol ) );
		
		JSONObject lastCandle = allCandles.getJSONObject( allCandles.length( ) - 1 );
		
		return lastCandle.toString( ); 
	}
	
	/**
	 * Internal method for building the API call
	 * @param key
	 * @param interval
	 * @param exchange
	 * @param symbol
	 * @return
	 */
	private String buildURL( String key, String interval, String exchange, String symbol )
	{
		return String.format( URL, key, interval, exchange, symbol );
	}
	
	/**
	 * Public method to test internal functions using args for API key grabbing
	 * @param args
	 */
	public static void main( String args[] )
	{
		NomicsExchangeCandles nomicsExchangeCandles = new NomicsExchangeCandles( );
		
		try 
		{
			System.out.println( nomicsExchangeCandles.getMostRecentCandle( args[0], "1h", "gdax", "BTC-USD") );
		} 
		catch ( IOException e ) 
		{
			e.printStackTrace(); 
		}
		catch( JSONException e )
		{
			e.printStackTrace( );
		}
	}
	
}
