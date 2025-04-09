package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import exceptions.ResponseCode;

public class Main {
	/**
	 * TODO This should be in a txt apart that the app creates on the first launch
	 * to ask the user for his client_ID if we wanted to take this a step further we
	 * should make it in a way that the user doesn't need to get this two strings,
	 * but for the moment it will stay like this
	 */
	private static final String CLIENTID = "a900160f92f8446e9e609de488310890";
	private static final String CLIENT_SECRET = "2a29c697d4654f2c94eaa5c821f24dfb";

	public static void main(String[] args) {

		SpotifyTokenProvider sptf = SpotifyTokenProvider.getInstance(CLIENTID, CLIENT_SECRET);
		try {
			if (sptf.checkApi("Bad Bunny")) {
				// System.out.println("Funciona");
				// System.out.println(getPlayListUser(sptf.refreshAccessToken()));
				// String Listas = getPlayListUser(sptf.refreshAccessToken());
				// System.out.println(getPlayListUser("AQCTgGSUWBomnN_XwDMonYj-vqzBTIAdmGPp2Oz-skgcDewiRCAhegNvxG-5ZcE9CrHNbsw26AHkwA8ffw2enyejpy0WKH0OiCWE7_gCxhvN_6daarcof28nHOz7kmJuaBI"));

				JSONObject json = getPlayListUser(sptf.refreshAccessToken());

				JSONArray playlists = json.getJSONArray("items");

				for (int i = 0; i < playlists.length(); i++) {
					JSONObject playlist = playlists.getJSONObject(i);
					JSONObject tracksObj = playlist.getJSONObject("tracks");
					int totalTracks = tracksObj.getInt("total");

					System.out.println("Playlist: " + playlist.getString("name") + " - Canciones: " + totalTracks);
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static JSONObject getPlayListUser(String acc) {
		
		try {

			String ENDPOINT = "https://api.spotify.com/v1/";
			String endPoint = ENDPOINT + "me/playlists";
			int responseCode;
			String inputLine;
			BufferedReader in;
			StringBuilder content = new StringBuilder();

			URL url = new URL(endPoint);

			/* Comenzamos la conexión */

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			conn.setRequestProperty("Authorization", "Bearer " + acc);
			responseCode = conn.getResponseCode();

			if (ResponseCode.handleResponseCode(responseCode)) {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				while ((inputLine = in.readLine()) != null) {

					content.append(inputLine);
				}
				return new JSONObject(content.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
