import java.io.IOException;
import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NomicsPrices {

	/**
	 * URL for grabbing the the prices of all currencies - USD
	 */
	private static final String URL = "https://api.nomics.com/v1/prices?key=%s";
	
	/**
	 * Set precision for BigDecimal when provided quote currency
	 */
	private static final int PRECISION = 8;
	
	/**
	 * Public method for grabbing all prices at <em>this</em> point in time
	 * @param key				Priate key for the API
	 * @return					A List of Currency/Price JSON pairs
	 * @throws IOException
	 */
	public String getAllPrices( String key ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key );
		return httpsClient.doGet( formattedURL );
	}
	
	/**
	 * Filtered layer on top of getAllPrices to grab prices based 
	 * on a specific quote currency as the desired base ie. when quoteCurrency
	 * == ETH prices will be returned against ETH
	 * @param key				Private API key
	 * @param quoteCurrency		Quote currency to return against
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public String getAllPrices( String key, String quoteCurrency ) throws JSONException, IOException
	{
		
		//Grab all USD pairs
		JSONArray response = new JSONArray( getAllPrices( key ) );
		
		//Call method to get USD price of quotted currency
		BigDecimal quotePrice  = findPriceOfCurrency( response, quoteCurrency );
		
		//Convert all json pairs to new quote
		String responseUsingQuoteCurrency = convertWithNewBase( response, quotePrice );
		
		//Return json array as string
		return responseUsingQuoteCurrency;
	}
	
	/**
	 * Grab the price in USD of a specific symbol from the JSON response
	 * @param response			JSON array returned from Nomics API
	 * @param symbol				The symbol of the currency to grab the price of
	 * @return					A BigDecimal with the currency's value in USD
	 * @throws JSONException
	 */
	public BigDecimal findPriceOfCurrency( JSONArray response, String symbol ) throws JSONException
	{
		//Find desired quote currency price and grab it
		for( int i = 0; i < response.length( ); i++ )
		{
			JSONObject pair = response.getJSONObject( i );
			
			if( pair.getString( "currency").equals( symbol ) )
			{
				return new BigDecimal( pair.getString( "price" ) ).setScale( PRECISION, BigDecimal.ROUND_DOWN );
			}
		}
		
		return null;
	}
	/**
	 * Internal method that converts all prices to be quoted in 
	 * @param prices						JSONArray of original prices	
	 * @param quoteCurrencyValueInUSD	The USD value of the new quote currency as a BigDecimal
	 * @return							String representation of new JSON array
	 * @throws JSONException 
	 */
	private String convertWithNewBase( JSONArray prices, BigDecimal quoteCurrencyValueInUSD ) throws JSONException
	{
		JSONArray quotedPrices = new JSONArray( );
		
		for( int i = 0; i < prices.length( ); i++ )
		{
			//Grab price in USD
			BigDecimal price = new BigDecimal( prices.getJSONObject( i ).getString( "price" ) )
								  				.setScale( PRECISION, BigDecimal.ROUND_DOWN );
			
			BigDecimal quotedPrice = price.divide( quoteCurrencyValueInUSD, BigDecimal.ROUND_DOWN ).setScale( PRECISION, BigDecimal.ROUND_DOWN );
			
			//Convert price to new quote price
			JSONObject currencyPricePair = new JSONObject( );
			currencyPricePair.put( "currency", prices.getJSONObject( i ).getString( "currency" ) );
			currencyPricePair.put( "price", quotedPrice.toString( ) );
			
			quotedPrices.put( currencyPricePair );
		}
		
		return quotedPrices.toString( );
		
	}
	
	/**
	 * Internal method to format URL with private KEY
	 * @param key
	 * @return
	 */
	private String buildURL( String key )
	{
		return String.format( URL, key );
	}
	
	/**
	 * Use to test different functions please use args to control API key
	 * @param args
	 */
	public static void main( String args[] )
	{
		NomicsPrices nomicsPrices = new NomicsPrices( );
		String key = args[ 0 ];
		
		try
		{
			System.out.println( nomicsPrices.getAllPrices( key, "BTC" ) );
		}
		catch( Exception e )
		{
			System.out.println( e.getStackTrace( ) );
		}
	}
}
