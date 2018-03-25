import java.io.IOException;

public class NomicsAggregatedCandles {

	/**
	 * URL for grabbing all the aggregated candles for a specific currency
	 */
	private static final String URL = "https://api.nomics.com/v1/candles?key=%s&interval=%s&currency=%s";
	
	/**
	 * Public method to grab all the aggregated candles for a given currency (symbol) from date == unixTimestamp
	 * @param key				The private API key
	 * @param unixTimestamp		The date to grab the candles from ie. 'YYYY-MM-DDTHH:mm:ss.sssZ'
	 * @param symbol				The symbol of queried currency
	 * @return					A String representing a JSON array of candles
	 * @throws IOException
	 */
	public String getCandlesFromTimestamp( String key, String unixTimestamp, String symbol ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key, unixTimestamp, symbol );
		return httpsClient.doGet( formattedURL );
	}
	
	/**
	 * Internal method to format URL with private KEY
	 * @param key
	 * @return
	 */
	private String buildURL( String key, String unixTimestamp, String symbol )
	{
		return String.format( URL, key, unixTimestamp, symbol );
	}
	
	public static void main( String[] args )
	{
		NomicsAggregatedCandles nomicsAggregatedCandles = new NomicsAggregatedCandles( );
		String key = args[ 0 ];
		
		try
		{
			System.out.println( nomicsAggregatedCandles.getCandlesFromTimestamp( key, "2017-01-01", "ETH" ) );
		}
		catch( Exception e )
		{
			System.out.println( e.getStackTrace( ) );
		}
	}
	
}
