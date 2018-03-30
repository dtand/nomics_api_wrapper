package nomics.core;
import java.io.IOException;

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
	 * Method to grab an array list of markets at the exchange level - provided some JSONArray
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
