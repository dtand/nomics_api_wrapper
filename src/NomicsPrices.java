import java.io.IOException;

import org.json.JSONObject;
public class NomicsPrices {

	/**
	 * URL for grabbing the the prices of all currencies - USD
	 */
	private static final String URL = "https://api.nomics.com/v1/prices?key=%s";
	
	/**
	 * Filtered layer on top of getAllPrices to grab prices based 
	 * on a specific quote currency as the desired base ie. when quoteCurrency
	 * == ETH prices will be returned against ETH
	 * @param key				Private API key
	 * @param quoteCurrency		Quote currency to return against
	 * @return
	 */
	public String getAllPrices( String key, String quoteCurrency )
	{
		JSONObject response = new JSONObject( getAllPrices( key, quoteCurrency ) );
	}
	
	public String getAllPrices( String key ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key );
		return httpsClient.doGet( formattedURL );
	}
	
	private String buildURL( String key )
	{
		return String.format( URL, key );
	}
	
	public static void main( String args[] )
	{
		NomicsPrices nomicsPrices = new NomicsPrices( );
		String key = args[1];
		try
		{
			System.out.println( nomicsPrices.getAllPrices( key ) );
		}
		
		catch( Exception e )
		{
			
		}
	}
}
