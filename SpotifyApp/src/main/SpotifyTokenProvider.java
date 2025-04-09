package main;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import org.json.JSONObject;

import exceptions.ResponseCode;
import exceptions.SpotifyApiException;

public class SpotifyTokenProvider {
	/*
	 * The start of the end point will be always the same. In case of a change in
	 * the start we only need to change this variable
	 */
	private final String ENDPOINT = "https://api.spotify.com/v1/";

	// private String codeNuevo =
	// "AQBbrBeOXtBiOgQFl364vmFEu2roaOqrrJXgMNjuxmCLOaMHy-itKFZR5Xvs_NhNmoQ3UaTL2XfUjthumsSKs0q7TAiOlHPfw0LL4CPOOD777n_t8SHFj2ooprq9Zf3rOABGThzRNjYrpbNEiHJMiyNsgepIc3GBe7rYycGrak5ghNXzoFGrKVrJ1iDl-hXjN6TbORNyVIz3Dmo159qZ-6UccQMhWcHZ797zNIw0pDRasBekl_0";
	private String codeNuevo = "AQCTgGSUWBomnN_XwDMonYj-vqzBTIAdmGPp2Oz-skgcDewiRCAhegNvxG-5ZcE9CrHNbsw26AHkwA8ffw2enyejpy0WKH0OiCWE7_gCxhvN_6daarcof28nHOz7kmJuaBI";

	private String clientID;
	private String client_Secret;
	private static SpotifyTokenProvider SpotifyTokenProvider;
	private String accessToken;

	private SpotifyTokenProvider(String clientID, String client_Secret) {
		this.clientID = clientID;
		this.client_Secret = client_Secret;
		this.accessToken = getAuth();
	}

	/**
	 * 
	 * @return The access token for the API
	 */
	public String getAccessToken() {
		return this.accessToken;
	}

	/**
	 * Overload method of AcessToken. This method initializes the singleton instance
	 * with the provided clientID and clientSecret
	 * 
	 * @param clientID      the client ID used to initialize the access token
	 * @param client_Secret the client secret used to initialize the access token
	 * @return the singleton instance of {@code AccessToken}
	 */
	public static SpotifyTokenProvider getInstance(String clientID, String client_Secret) {
		if (SpotifyTokenProvider == null) {
			SpotifyTokenProvider = new SpotifyTokenProvider(clientID, client_Secret);
		}
		return SpotifyTokenProvider;
	}

	/**
	 * This method should be called only after the instance has been initialized
	 * using {@link #getInstance(String, String)}. If it hasn't been initialized,
	 * this method will throw an exception.
	 *
	 * @return the singleton instance of {@code AccessToken}
	 * @throws IllegalStateException if the instance has not been initialized
	 */
	public static SpotifyTokenProvider getInstance() {
		if (SpotifyTokenProvider == null) {
			throw new IllegalStateException("La instancia no ha sido inicializada");
		}
		return SpotifyTokenProvider;
	}

	/**
	 * To check if the API works we give the method the AccessToken that we got
	 * 
	 * @param AccessToken
	 * @return
	 * @throws Exception
	 */
	public boolean checkApi(String artist) throws Exception {

		try {

			String artistModified = artist.replace(" ", "+");
			String endPoint = ENDPOINT + "search?q=" + artistModified + "&type=artist";
			URL url = new URL(endPoint);

			/* Comenzamos la conexión */

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			conn.setRequestProperty("Authorization", "Bearer " + this.accessToken);
			int responseCode = conn.getResponseCode();
			return ResponseCode.handleResponseCode(responseCode);

		} catch (IOException e) {
			throw new IOException("Error connecting to Spotify API: " + e.getMessage(), e);
		}
	}

	public String getAuth() {

		try {
			String auth = clientID + ":" + client_Secret;
			String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

			URL url = new URL("https://accounts.spotify.com/api/token");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "Basic " + encodeAuth);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String body = "grant_type=client_credentials";
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = body.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			StringBuilder response = new StringBuilder();
			String responseLine;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			JSONObject json = new JSONObject(response.toString());
			return json.getString("access_token");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param responseCode
	 * @return
	 * @throws SpotifyApiException
	 */
	public String getAuthConCode() {
		try {
			String auth = clientID + ":" + client_Secret;
			String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

			URL url = new URL("https://accounts.spotify.com/api/token");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "Basic " + encodeAuth);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String redirectUri = "http://localhost:8888/callback"; // Asegúrate de que es la misma que usaste para
																	// conseguir el code

			String body = "grant_type=authorization_code" + "&code=" + URLEncoder.encode(codeNuevo, "UTF-8")
					+ "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = body.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			StringBuilder response = new StringBuilder();
			String responseLine;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			JSONObject json = new JSONObject(response.toString());

			// Aquí tienes los tokens:
			String accessToken = json.getString("access_token");
			
			/* 
			 * String refreshToken = json.has("refresh_token") ? json.getString("refresh_token") : "no-refresh-token";
			 * System.out.println("Access Token: " + accessToken);
			 * System.out.println("Refresh Token: " + refreshToken);
			 */
			return accessToken;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String refreshAccessToken() {
		String refreshToken = codeNuevo;
		try {
			String auth = clientID + ":" + client_Secret;
			String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());

			URL url = new URL("https://accounts.spotify.com/api/token");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", "Basic " + encodeAuth);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String body = "grant_type=refresh_token" + "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8");

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = body.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			StringBuilder response = new StringBuilder();
			String responseLine;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			JSONObject json = new JSONObject(response.toString());
			String accessToken = json.getString("access_token");

			// System.out.println("Nuevo Access Token: " + accessToken);
			return accessToken;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	

}
