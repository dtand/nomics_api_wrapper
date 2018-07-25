package nomics.core;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Stack;

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
	 * Method to grab all the candles by exchange for provided currency and interval. Valid values: 1d, 1h, 30m, 5m, 1m.
	 * This method can also accept 2h, 4h, 6h, 12h, but these are generated as a layer 2 function
	 * @param key			The API key
	 * @param interval		The iterval for the kline as a string: Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param exchange		The id for the exchange ie. "binance", "gdax" ...
	 * @param symbol			The symbol for the currency of iterest, ie: "ETH", "LTC", "BTC"
	 * @return				A string of klines representing a JSONArray
	 * @throws IOException
	 * @throws JSONException 
	 */
	public String getExchangeCandles( String key, String interval, String exchange, String symbol ) throws IOException, JSONException
	{
		HttpsClient httpsClient = new HttpsClient( );
		
		
		if( interval.equals( "2h" ) || interval.equals( "6h" ) || interval.equals( "12h" ) ) 
		{
			String formattedURL  = buildURL( key, "1h", exchange, symbol );
			String candles   	 = httpsClient.doGet( formattedURL );
			JSONArray candlesA	 = new JSONArray( replaceZeroCandles( candles ) );
			return createNewCandleSet( candlesA, interval );
		}
		
		String formattedURL     = buildURL( key, interval, exchange, symbol );
		return replaceZeroCandles( httpsClient.doGet( formattedURL ) );
	}
	
	/**
	 * Internal method to replace all 0 candles with their previous price
	 * @param candles
	 * @return
	 * @throws JSONException
	 */
	private String replaceZeroCandles( String candles ) throws JSONException {
		
		JSONArray candlesArray   	= new JSONArray( candles );
		JSONArray returnArray    	= new JSONArray( );
		JSONObject lastNonZeroCandle = null;
		
		for( int i = 0; i < candlesArray.length( ); i++ ) {
			
			JSONObject object = candlesArray.getJSONObject( i );
			Double price      = new Double( object.getString( "close") );

			if( price.equals( new Double( 0 ) ) ) {
				
				if( lastNonZeroCandle == null ){
					continue;
				}
				JSONObject newCandle = new JSONObject( lastNonZeroCandle.toString( ) );
				newCandle.put( "timestamp", object.getString( "timestamp" ) );
				returnArray.put( newCandle );
			}
			else{
				returnArray.put( object );
				lastNonZeroCandle = object;
			}
			
		}
		
		return returnArray.toString( );
		
	}
	
	/**
	 * Create a JSON array with a unqiue candle set not already provided by the API
	 * @param candles
	 * @param interval
	 * @return
	 * @throws JSONException 
	 */
	public String createNewCandleSet( JSONArray candles, String interval ) throws JSONException
	{
		JSONArray returnArray = new JSONArray( );
		int hour = 2;
		
		switch( interval )
		{
			case "2h":
				hour = 2;
				break;
			case "6h":
				hour = 6;
				break;
			case "12h":
				hour = 12;
				break;
		}
		
		String timestamp = "";
		BigDecimal open  = new BigDecimal( 0 );
		BigDecimal close = new BigDecimal( 0 );
		BigDecimal high  = new BigDecimal( 0 );
		BigDecimal low	 = new BigDecimal( Double.MAX_VALUE );
		BigDecimal volume = new BigDecimal( 0 );
		
		int c = 1;
		
		for( int i = 0; i < candles.length( ); i++ )
		{
			JSONObject kline = candles.getJSONObject( i ); 
			
			//Always grab the high and the low - we need to check these each time
			BigDecimal klineHigh = new BigDecimal( kline.getDouble( "high" ) );
			BigDecimal klineLow = new BigDecimal( kline.getDouble( "low" ) );
			
			high   = new BigDecimal( Math.max( klineHigh.doubleValue( ), high.doubleValue( ) ) );
			low    = new BigDecimal( Math.min( klineLow.doubleValue( ), low.doubleValue( ) ) );
			volume = volume.add( new BigDecimal( kline.getDouble("volume") ) );
			
			//This is the start of the candle
			if( c == 1 )
			{
				open      = new BigDecimal( kline.getDouble( "open" ) );
				timestamp = kline.getString( "timestamp" );
			}
			
			//This is the end of the candle
			if( c == hour )
			{
				close 			  = new BigDecimal( kline.getDouble( "close" ) );
				String thisCandle = createKline( timestamp, open.setScale( 8, BigDecimal.ROUND_DOWN ), 
												close.setScale( 8, BigDecimal.ROUND_DOWN ), 
												high.setScale( 8, BigDecimal.ROUND_DOWN ), 
												low.setScale( 8, BigDecimal.ROUND_DOWN ),
												volume.setScale( 8, BigDecimal.ROUND_DOWN ) );
				c = 0;
				returnArray.put( new JSONObject( thisCandle ) );
				timestamp = "";
				open      = new BigDecimal( 0 );
				close  	  = new BigDecimal( 0 );
				high  	  = new BigDecimal( 0 );
				low	 	  = new BigDecimal( Double.MAX_VALUE );
				volume 	  = new BigDecimal( 0 );
			}
			
			c++;
			
		}
		
		return returnArray.toString( );
	}
	
	/**
	 * Returns a kline candle JSON object
	 * @param open
	 * @param close
	 * @param high
	 * @param low
	 * @param volume
	 * @return
	 * @throws JSONException
	 */
	protected String createKline( String timestamp, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal volume ) throws JSONException
	{
		
		JSONObject candle = new JSONObject( );
		candle.put( "timestamp", timestamp );
		candle.put( "open", open );
		candle.put( "close", close );
		candle.put( "high", high );
		candle.put( "low", low );
		candle.put( "volume", volume );
		return candle.toString( );
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
		
		if( allCandles.length( ) == 0 )
			return "{}";
		
		JSONObject lastCandle = allCandles.getJSONObject( allCandles.length( ) - 1 );
		
		return lastCandle.toString( ); 
	}
	
	/**
	 * Second level filter to grab the most recent non-zero candle
	 * @param key			API key
	 * @param interval		Kline interval: Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param exchange		The id for the exchange ie. "binance", "gdax" ...
	 * @param symbol			The symbol for the currency of iterest, ie: "ETH", "LTC", "BTC"
	 * @return				A string of klines representing a JSONArray
	 * @throws JSONException
	 * @throws IOException
	 */
	public String getMostRecentNonZeroCandle( String key, String interval, String exchange, String symbol ) throws JSONException, IOException
	{
		JSONArray allCandles = new JSONArray ( getExchangeCandles( key, interval, exchange, symbol ) );
		
		if( allCandles.length( ) == 0 )
			return "{}";
		
		for( int i = allCandles.length( )-1; i > 0; i-- ) {
			if( new Double( allCandles.getJSONObject( i ).getString( "close") ) == 0 ) {
				continue;
			}
			else {
				return allCandles.getJSONObject( i ).toString( );
			}
		}
		
		return "{}";
	}
	
	/**
	 * Will grab the previous N Candles
	 * @param key
	 * @param interval
	 * @param exchang
	 * @param symbol
	 * @param numCandles
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public String getLastNCandles( String key, String interval, String exchange, String symbol, int numCandles ) throws JSONException, IOException
	{
		JSONArray allCandles   = new JSONArray ( getExchangeCandles( key, interval, exchange, symbol ) );
		JSONArray lastNCandles = new JSONArray( );
		
		if( allCandles.length( ) == 0 )
			return "{}";
		
		Stack< JSONObject > stack = new Stack< JSONObject >( );
		
		for( int i = allCandles.length( ) - 1; i >= allCandles.length( ) - numCandles; i-- )
		{
			stack.push( allCandles.getJSONObject( i ) );
		}
		
		//Pop all objects off into json array
		while( !stack.isEmpty( ) )
		{
			lastNCandles.put( stack.pop( ) );
		}
		
		return lastNCandles.toString( ); 
	}
	
	/**
	 * Returns the all time high of a given market pair at the exchange level
	 * @param key		The private key for the nomics API
	 * @param interval	Kline interval: Valid values: 1d, 1h, 30m, 5m, 1m
	 * @param exchange	The id for the exchange ie. "binance", "gdax" ...
	 * @param symbol		The symbol for the currency of iterest, ie: "ETH", "LTC", "BTC"
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public BigDecimal getAllTimeHigh( String key, String interval, String exchange, String symbol ) throws JSONException, IOException
	{
		JSONArray allCandles = new JSONArray ( getExchangeCandles( key, interval, exchange, symbol ) );
		BigDecimal maxPrice  = new BigDecimal( Double.MIN_VALUE );
		
		for( int i = 0; i < allCandles.length( ); i++ )
		{ 
			JSONObject kline = allCandles.getJSONObject( i );
			double high		 = Double.parseDouble( kline.getString( "high" ) );
			System.out.println( high );
			maxPrice 		 = new  BigDecimal ( Math.max( maxPrice.doubleValue( ), high ) );
		}
		
		return maxPrice.setScale( 8, BigDecimal.ROUND_DOWN );
	}
	/**
	 * Grab a candle from a given point in time
	 * @param key
	 * @param interval
	 * @param symbol
	 * @param timestamp
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String getCandlesFromTimestamp( String key, String interval, String exchange, String symbol, String timestamp ) throws JSONException, IOException, ParseException
	{
		//2018-03-19T10:00:00Z
		JSONArray candles 		= new JSONArray( getExchangeCandles( key, interval, exchange, symbol ) );
		JSONArray returnCandles = new JSONArray( );
		
		Boolean foundStamp = false;
		for( int i = 0; i < candles.length( ); i++ )
		{
			if( candles.getJSONObject( i ).getString( "timestamp" ).equalsIgnoreCase( timestamp ) )
			{
				foundStamp = true;
			}
			
			if( foundStamp )
			{
				returnCandles.put( candles.getJSONObject( i ) );
			}
		}
		
		return returnCandles.toString( );
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
	 * @throws ParseException 
	 */
	public static void main( String args[] ) throws ParseException
	{
		NomicsExchangeCandles nomicsExchangeCandles = new NomicsExchangeCandles( );
		
		try 
		{
			System.out.println( nomicsExchangeCandles.getLastNCandles( args[0], "5m", "bitfinex", "bcibtc", 25 ) );
			//System.out.println( nomicsExchangeCandles.getExchangeCandles( args[0], "2h", "bittrex", "BTC-TRX") );
			//System.out.println( nomicsExchangeCandles.getExchangeCandles( args[0], "4h", "bittrex", "BTC-TRX") );
			//System.out.println( nomicsExchangeCandles.getExchangeCandles( args[0], "6h", "bittrex", "BTC-TRX") );
			//System.out.println( nomicsExchangeCandles.getExchangeCandles( args[0], "12h", "bittrex", "BTC-TRX") );
			//System.out.println( nomicsExchangeCandles.getCandlesFromTimestamp( args[0], "1d", "gdax", "BTC-USD", "2015-01-09T00:00:00Z" ) );
			//System.out.println( nomicsExchangeCandles.getLastNCandles( args[0], "1m", "binance", "ETCETH", 100, true ) );
		} 
		catch ( IOException e ) 
		{
			e.printStackTrace( ); 
		}
		catch( JSONException e )
		{
			e.printStackTrace( );
		}
	}
	
}
