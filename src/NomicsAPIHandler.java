
/**
 * The NomicsAPIHandler class wraps the general functionality provided by
 * the Nomics API.  The API Handler holds the private key which is extracted from
 * a local configuration file.  This class should be used as the central
 * delegator for performing all API calls.
 * @author danielanderson
 *
 */
public class NomicsAPIHandler 
{
	
	/**
	 * Internal membor to hold the private API key
	 */
	private String privateApiKey;

	/**
	 * Call to set the authentication key for future
	 * API calls, this will return true if the key
	 * is valid and false otherwise.
	 * @param privateApiKey
	 */
	public void authenticate( String privateApiKey )
	{
		this.privateApiKey = privateApiKey;
		//attempt API call
		return true;
	}
	
}
