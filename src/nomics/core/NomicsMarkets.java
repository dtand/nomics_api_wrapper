package nomics.core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The nomics market API provides a list of all currency pair (markets) available over 30+ different exchanges.
 * The class includes any additional filters on top of the vanilla API calls.  An example of an API
 * call is shown below:
 * 
 * HTTPS GET: https://api.nomics.com/v1/markets?key=1234
 * 
 * [
 * {
 *   "exchange":"bitfinex",
 *   "market":"avtbtc",
 *   "base":"AVT",
 *   "quote":"BTC"
 * },
 * ...
 * ]
 * 
 * @author danielanderson
 *
 */
public class NomicsMarkets {

	/**
	 * URL for grabbing all markets with associated exchanges
	 */
	private static final String URL = "https://api.nomics.com/v1/markets?key=%s";
	
	/**
	 * Public access to all market pairs accross 30 different exchanges.  The nomics API will
	 * return back a list of JSON objects of the following form:
	 *   
	 *   {
    	 *		"exchange":"bitfinex",
    	 *		"market":"avtbtc",
    	 *		"base":"AVT",
    	 *		"quote":"BTC"
  	 *	 }
  	 *
	 * @param key			The private API key for the API
	 * @return				String of JSON array, objects formatted like the one above
	 * @throws IOException
	 */
	public String getAllMarkets( String key ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key );
		return httpsClient.doGet( formattedURL );
	}
	
	/**
	 * Layer 2 filter for grabbing markets at the exchange level
	 * 
	 * @param key				The API key
	 * @param exchange			The exchange to filter off
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */	
	public String getMarketsByExchange( String key, String exchange ) throws JSONException, IOException
	{
		JSONArray markets = new JSONArray ( getAllMarkets( key ) );
		return filterByExchange( markets, exchange );
	}
	
	/**
	 * Layer 2 filter for grabbing markets when the format of the pair is unknown
	 * 
	 * @param key				The API key
	 * @param exchange			The exchange to filter off
	 * @param base				The base currency as string
	 * @param counter			The counter currency as string
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */	
	public String getMarketFromPair( String key, String exchange, String base, String counter ) throws JSONException, IOException
	{
		JSONArray markets = new JSONArray ( getMarketsByExchange( key, exchange ) );
		
		for( int i = 0; i < markets.length( ); i++ )
		{
			JSONObject market = markets.getJSONObject( i );
			
			if( base.equalsIgnoreCase( market.getString( "base" ) ) && counter.equalsIgnoreCase( market.getString( "quote" ) ) )
			{
				return market.getString( "market" );
			}
		}
		
		return "";
	}
	
	/**
	 * Method to grab a json array of markets at the exchange level - provided some JSONArray
	 * @param markets	JSONArray of market objects
	 * @return
	 * @throws JSONException 
	 */
	public String filterByExchange( JSONArray markets, String exchange ) throws JSONException
	{
		JSONArray marketsByExchange = new JSONArray( );
		
		for( int i = 0; i < markets.length( ); i++ )
		{
			JSONObject jsonObject = markets.getJSONObject( i );
			
			if( jsonObject.getString( "exchange").equals( exchange ) )
			{
				marketsByExchange.put( jsonObject );
			}
		}
		
		return marketsByExchange.toString( );
	}
	

	/**
	 * Layer two function for grabbing only markets from X exchanges that intersect
	 * @return JSON object with all intersecting markets
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public List< String > getMarketIntersections( String[] exchanges, String apiKey ) throws JSONException, IOException
	{
		JSONArray markets = new JSONArray( getAllMarkets( apiKey ) );
		return filterByIntersection( exchanges, markets );
	}
	
	/**
	 * Returns a list of strings where each string is all the intesections for exchange[i] and each
	 * string is a JSONArray
	 * @throws JSONException 
	 */
	public List< String > filterByIntersection( String[] exchanges, JSONArray markets ) throws JSONException 
	{
		int totalExchanges 			 = exchanges.length;
		Map< String, Integer > counts = new HashMap< String, Integer >( );
		
		//Iterate over provided exchanges
		for( int i = 0; i < totalExchanges; i++ )
		{
			JSONArray exchangeMarkets = new JSONArray( filterByExchange( markets, exchanges[ i ] ) );
			
			//Increment pair if it exists
			for( int j = 0; j < exchangeMarkets.length( ); j++ )
			{
				String base  = exchangeMarkets.getJSONObject( j ).getString( "base" );
				String quote = exchangeMarkets.getJSONObject( j ).getString( "quote" );
				
				if( !counts.containsKey( base + quote ) )
				{
					counts.put( base + quote , 1 );
				}
				else
				{
					counts.put( base + quote,  counts.get( base + quote ) + 1 );
				}
			}
		}
		
		List< String > marketsByExchange = new ArrayList< String >( );
		
		//Iterate over provided exchanges again to pull all intersected exchanges
		for( int i = 0; i < totalExchanges; i++ )
		{
			JSONArray exchangeMarkets = new JSONArray( filterByExchange( markets, exchanges[ i ] ) );
			JSONArray filteredJSON    = new JSONArray( );
			//Increment pair if it exists
			for( int j = 0; j < exchangeMarkets.length( ); j++ )
			{
				String base  = exchangeMarkets.getJSONObject( j ).getString( "base" );
				String quote = exchangeMarkets.getJSONObject( j ).getString( "quote" );
				
				if( counts.get( base + quote ) == totalExchanges )
				{
					filteredJSON.put( exchangeMarkets.getJSONObject( j ) );
				}
			}
			
			marketsByExchange.add( filteredJSON.toString( ) );
		}
		
		return marketsByExchange;
		
	}
	
	/**
	 * Call to get a List of strings containing the currently supported exchanges
	 * by the nomics API
	 * @return	List of strings with supported markets ie. [ "binance", "gdax" ... ]
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public List<String> getSupportedExchanges( String key ) throws JSONException, IOException
	{
		JSONArray response 		 		 = new JSONArray( getAllMarkets( key ) );
		HashSet< String > exchanges		 = new HashSet< String >( );
		List< String > supportedExchanges = new ArrayList< String >( );
		
		for( int i = 0; i < response.length( ); i++ )
		{
			String exchange = response.getJSONObject( i ).getString( "exchange" );
			
			if(  !exchanges.contains( exchange ) )
			{
				exchanges.add( exchange );
				supportedExchanges.add( exchange );
			}
		}
		
		return supportedExchanges;	
	}
	
	/**
	 * Internal method to generate API url with provided key
	 * @param key
	 * @return
	 */
	private String buildURL( String key )
	{
		return String.format( URL, key );
	}
	
	/**
	 * Call to test API
	 * @param args
	 */
	public static void main( String[] args )
	{
		NomicsMarkets nomicsMarkets = new NomicsMarkets( );
		
		try 
		{
			System.out.println( nomicsMarkets.getAllMarkets( args[0] ) );
			System.out.println( nomicsMarkets.getMarketsByExchange( args[0], "gdax" ) );
			System.out.println( nomicsMarkets.getMarketIntersections( new String[] {"binance", "bittrex", "poloniex"}, args[0] ) );
			System.out.println( nomicsMarkets.getSupportedExchanges( args[0] ) );
		} 
		
		catch ( IOException e ) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			e.printStackTrace( );
		}
	}
	
}
