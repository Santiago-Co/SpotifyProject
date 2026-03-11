package auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.JSONObject;

import api.EndPoint;
import config.ConfigIDs;
import exceptions.ResponseCode;
import exceptions.SpotifyApiException;
import logSystem.LogSystem;

public class TokenManager {

	private String clientID;
	private String clientSecret;
	private String redirectUri;

	private String accessToken;
	private String refreshToken;

	private static TokenManager instance;
	
	private long expiresAt; 
	
	private TokenManager(String clientID, String clientSecret, String redirectUri) {
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		this.expiresAt = Long.MAX_VALUE;
		if(ConfigIDs.hasRefreshToken()) {
			this.refreshToken = ConfigIDs.getRefreshToken();
		}

	}
	/**
	 * Retrieves the singleton instance of the TokenManager.
	 * Creates a new instance if one does not already exist.
	 *
	 * @return The singleton instance of the TokenManager.
	 */
	public static TokenManager getInstance() {
		if (instance == null) {
			instance = new TokenManager(ConfigIDs.getClientId(), ConfigIDs.getClientSecret(), ConfigIDs.getRedirectURI());
		}
		return instance;
	}
	/**
	 * Retrieves a valid access token.
	 * <p>
	 * If the current token is missing or expired, this method attempts to refresh it using the
	 * stored refresh token. If no refresh token is available or the refresh fails, it returns null,
	 * indicating that the user needs to log in again.
	 * </p>
	 *
	 * @return The valid access token string, or {@code null} if the user is not authenticated.
	 */
	public String getAccessToken() {
	    if (this.accessToken == null || isTokenExpired()) {
	        if (this.refreshToken != null) {
	            try {
	                refreshAccessToken();
	            } catch (IOException | SpotifyApiException e) {
	                LogSystem.flujo("Could not refresh the accessToken", "");
	                //throw new IllegalStateException("Could not refresh the aaccessToken"); TODO
	                return null; 
	            }
	        } else {
	            LogSystem.flujo("No refresh token available", "User must log in");
	            //throw new IllegalStateException("User must log in"); TODO
	            return null;
	        }
	    }
	    return this.accessToken;
	}
	/**
	 * Exchanges the authorization code (or the full callback URL) for access and refresh tokens.
	 * Parses the code from the URL if necessary and updates the internal state with the new tokens.
	 *
	 * @param code The authorization code or the full callback URL received from Spotify.
	 * @throws IOException If there is an error during the network request to exchange tokens.
	 * @throws SpotifyApiException If the Spotify API returns an error response (non-200/201 status).
	 */
	public void exchangeCodeForTokens(String code) throws IOException, SpotifyApiException {

		code = modifyURL(code);
		String body = "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8.toString()) 
				+ "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
				+ "&grant_type=authorization_code";
		
		JSONObject json = sendPostRequest(body);

		this.accessToken = json.getString("access_token");
		if (json.has("refresh_token")) {
			this.refreshToken = json.getString("refresh_token");
			ConfigIDs.setRefreshToken(this.refreshToken);
		}
		//System.out.println("Tokens obtained correctly");

	}
	
	/**
	 * Refreshes the current access token using the stored refresh token.
	 * It updates the access token and the expiration time internally.
	 *
	 * @throws IllegalStateException If no refresh token is available.
	 * @throws IOException If there is an error during the network request.
	 * @throws SpotifyApiException If the Spotify API returns an error response.
	 */
	public void refreshAccessToken() throws IOException, SpotifyApiException {
		
		if (this.refreshToken == null) {
			throw new IllegalStateException("Theres not a Refresh Token avalible");
		}

		String body = "grant_type=refresh_token" + "&refresh_token="
				+ URLEncoder.encode(this.refreshToken, StandardCharsets.UTF_8.toString());
		JSONObject json = sendPostRequest(body);

		this.accessToken = json.getString("access_token");
		int expiresInSeconds = json.getInt("expires_in"); 
		this.expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);
		
		if (json.has("refresh_token")) {
			this.refreshToken = json.getString("refresh_token");
			ConfigIDs.setRefreshToken(this.refreshToken);
			//TODO Add log system
		}
		//System.out.println("Acess Token refreshed"); 
	}
	
	/**
	 * Sends a POST request to the Spotify Authentication API.
	 * This helper method handles the headers, body encoding, and response parsing.
	 *
	 * @param body The URL-encoded string containing the parameters for the request.
	 * @return A JSONObject containing the API response.
	 * @throws IOException If the connection fails or input/output errors occur.
	 * @throws SpotifyApiException If the HTTP response code from Spotify indicates a failure.
	 */
	private JSONObject sendPostRequest(String body) throws IOException, SpotifyApiException {
		URL url = URI.create(EndPoint.AUTH_URL).toURL();
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setDoOutput(true);

		String authHeader = getBase64Credentials();

		conn.setRequestProperty("Authorization", "Basic " + authHeader);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		try (OutputStream os = conn.getOutputStream()) {
			byte[] input = body.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}

		int responseCode = conn.getResponseCode();

		ResponseCode.handleResponseCode(responseCode);

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		StringBuilder response = new StringBuilder();
		String responseLine;
		while ((responseLine = br.readLine()) != null) {
			response.append(responseLine.trim());
		}
		return new JSONObject(response.toString());
	}
	
	/**
	 * Generates the Base64 encoded string of the "ClientID:ClientSecret" credentials.
	 * This is required for the "Authorization" header in Spotify API requests.
	 *
	 * @return The Base64 encoded authorization string.
	 */
	private String getBase64Credentials() {
		String auth = clientID + ":" + clientSecret;
		return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Generates the URL required to redirect the user to Spotify's authorization page.
	 *
	 * @param scopes A space-separated string of permissions (scopes) required by the application.
	 * @return The full authorization URL including client ID, redirect URI, and scopes.
	 */
	public String getAuthUrl(String scopes) {

		String authorizationEndpoint = EndPoint.AUTHORIZATION_ENDPOINT;

		try {
			return authorizationEndpoint + "?client_id=" + this.clientID + "&response_type=code" + "&redirect_uri="
					+ URLEncoder.encode(this.redirectUri, StandardCharsets.UTF_8.toString()) + "&scope="
					+ URLEncoder.encode(scopes, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "Error generando la URL";
		}
	}
	
	/**
	 * Extracts the authorization code from the full callback URL or returns the code itself
	 * if no parsing is needed.
	 *
	 * @param code The full URL received from the callback or the raw authorization code.
	 * @return The clean authorization code string.
	 */
	private String modifyURL(String code) {
		return (code.contains("code=") ? code.split("code=")[1].split("&")[0] : code);
	}
	
	/**
	 * Checks if the current access token is expired or about to expire.
	 * Includes a 60-second safety buffer to prevent race conditions during requests.
	 *
	 * @return true if the token is expired or close to expiration, false otherwise.
	 */
	private boolean isTokenExpired() {
		return System.currentTimeMillis() > (expiresAt - 60000); 
	}
}
