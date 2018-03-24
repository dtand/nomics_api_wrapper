import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class HttpsClient {
		
	private final String USER_AGENT = "Mozilla/5.0";
	
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
		
	   private String getContent( HttpsURLConnection con )
	   {
		if(con!=null){
				
			try {
				
			   System.out.println("****** Content of the URL ********");			
			   BufferedReader br = 
				new BufferedReader(
					new InputStreamReader(con.getInputStream()));
						
			   String input;
			   StringBuilder response = new StringBuilder( );
			   while ((input = br.readLine()) != null){
			      response.append( input );
			      response.append( "\n" );
			   }
			   br.close();
			   return response.toString( );
						
			} 
			catch (IOException e) 
			{
			   e.printStackTrace();
			}
				
	       }
		
		return null;
			
	   }
}
