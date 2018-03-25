import java.io.IOException;

public class NomicsExchangeCandles {

	/**
	 * URL for grabbing all the aggregated candles for a specific currency
	 */
	private static final String URL = "https://api.nomics.com/v1/exchange_candles?key=interval=%s&exchange=%s&market=%s";
	

	public String getCandlesFromTimestamp( String key, String unixTimestamp, String symbol ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key, unixTimestamp, symbol );
		return httpsClient.doGet( formattedURL );
	}
	

	private String buildURL( String key, String unixTimestamp, String symbol )
	{
		return String.format( URL, key, unixTimestamp, symbol );
	}
	
}
