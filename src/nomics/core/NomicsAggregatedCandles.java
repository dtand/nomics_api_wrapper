package nomics.core;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Wrapper for the nomics aggregated candles API.  This class includes the vanilla API 
 * call along with any additional layer two filters on top.  API example is shown below:
 * 
 * HTTPS GET: https://api.nomics.com/v1/candles?key=1234&interval=1h&currency=BTC
 * 
 * [
 * {
 *   "timestamp": "2018-03-19T10:00:00Z",
 *   "low": "7024.32225",
 *   "open": "8276.19407",
 *   "close": "8281.17307",
 *   "high": "8566.43000",
 *   "volume": "59624801"
 * },
 * ]
 * 
 * @author danielanderson
 *
 */
public class NomicsAggregatedCandles {

	/**
	 * URL for grabbing all the aggregated candles for a specific currency
	 */
	private static final String URL = "https://api.nomics.com/v1/candles?key=%s&interval=%s&currency=%s";
	
	/**
	 * Allow the user to grab a candle set froma  specific timestamp
	 * @param key
	 * @param interval
	 * @param symbol
	 * @param timestamp
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String getCandlesFromTimestamp( String key, String interval, String symbol, String timestamp ) throws JSONException, IOException, ParseException
	{
		//2018-03-19T10:00:00Z
		JSONArray candles 		= new JSONArray( getCandles( key, interval, symbol ) );
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
		
		return "";
	}
	/**
	 * Public method to grab all the aggregated candles for a given currency (symbol) from date == unixTimestamp
	 * @param key				The private API key
	 * @param unixTimestamp		The date to grab the candles from ie. 'YYYY-MM-DDTHH:mm:ss.sssZ'
	 * @param symbol				The symbol of queried currency
	 * @return					A String representing a JSON array of candles
	 * @throws IOException
	 */
	public String getCandles( String key, String interval, String symbol ) throws IOException
	{
		HttpsClient httpsClient = new HttpsClient( );
		String formattedURL     = buildURL( key, interval, symbol );
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
	
	/**
	 * Main method to test using args to grab key
	 * @param args
	 */
	public static void main( String[] args )
	{
		NomicsAggregatedCandles nomicsAggregatedCandles = new NomicsAggregatedCandles( );
		String key = args[ 0 ];
		
		try
		{
			//"2017-07-14T00:00:00Z"
			System.out.println( nomicsAggregatedCandles.getCandlesFromTimestamp( key, "1d", "BTC", "2017-07-14T00:00:00Z" ) );
		}
		catch( Exception e )
		{
			System.out.println( e.getStackTrace( ) );
		}
	}
	
}
