import java.io.IOException;

public class NomicsPrices {

	/**
	 * URL for grabbing the the prices of all currencies - USD
	 */
	private static final String URL = "https://api.nomics.com/v1/prices?key=%s";
	
	//public String getAllPrices( String quoteCurrency )
	
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
