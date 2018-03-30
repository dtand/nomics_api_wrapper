package core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Vanilla HTTPS client class for performing GET
 * requests provided some URL
 * @author danielanderson
 *
 */
public class HttpsClient {
		
	private final String USER_AGENT = "Mozilla/5.0";

	/**
	 * Public method to perform GET request and return
	 * the response as a string
	 * @param getURL			URL to GET to
	 * @return				respose as a String
	 * @throws IOException
	 */
	public String doGet( String getURL ) throws IOException
	{
		
		URL obj = new URL( getURL );
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + getURL);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		in.close();

		//print result
		return response.toString( );
   }
}
