package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import api.EndPoint;
import auth.TokenManager;
import exceptions.ResponseCode;
import exceptions.SpotifyApiException;
import models.Track;

public class LibraryService {

	
	
	/**
	 * Retrieves all the user's "Liked Songs" (Saved Tracks) from the Spotify Library.
	 * <p>
	 * This method handles pagination automatically by iterating through the "next" URL
	 * provided in the API response until all pages are fetched. It authenticates requests
	 * using the {@link TokenManager}.
	 * </p>
	 *
	 * @return A complete list of {@link Track} objects representing all the user's saved songs.
	 * @throws IOException If a network connection error occurs or the stream cannot be read.
	 * @throws SpotifyApiException If the Spotify API returns a non-success HTTP status code.
	 * @throws URISyntaxException If the API endpoint URL or the pagination URLs are malformed.
	 */
	public static List<Track> getLikedSongs() throws IOException, SpotifyApiException, URISyntaxException {
		TokenManager tm = TokenManager.getInstance();

		List<Track> allTracks = new ArrayList<>();

		String currentUrl = EndPoint.LIBRARY_ENDPOINT;

		while (currentUrl != null && !currentUrl.isEmpty()) {

			String accessToken = tm.getAccessToken();
			URL url = new URI(currentUrl).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setRequestProperty("Content-Type", "application/json");

			int responseCode = conn.getResponseCode();

			ResponseCode.handleResponseCode(responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder content = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();

			JSONObject jsonResponse = new JSONObject(content.toString());

			List<Track> pageTracks = parseJsonToTracks(jsonResponse);
			allTracks.addAll(pageTracks);

			//System.out.println("Descargadas " + pageTracks.size() + " canciones...");

			if (jsonResponse.has("next") && !jsonResponse.isNull("next")) {
				currentUrl = jsonResponse.getString("next");
			} else {
				currentUrl = null;
			}
		}

		//System.out.println("Total final: " + allTracks.size() + " canciones recuperadas.");
		return allTracks;
	}
	
	/**
	 * Parses a JSON object returned by the Spotify API into a list of Track objects.
	 * <p>
	 * This helper method extracts specific fields (track name, URI, and artist details)
	 * from the "items" array within the JSON structure. It handles the nested structure
	 * of Spotify's track object model.
	 * </p>
	 *
	 * @param jsonO The {@link JSONObject} containing a page of results from the Spotify API.
	 * @return A list of {@link Track} objects populated with data; returns an empty list if the "items" key is missing.
	 */
	private static List<Track> parseJsonToTracks(JSONObject jsonO) {

		List<Track> tracks = new ArrayList<>();

		if (!jsonO.has("items"))
			return tracks;

		JSONArray items = jsonO.getJSONArray("items");

		for (int i = 0; i < items.length(); i++) {

			JSONObject itemWrapper = items.getJSONObject(i);
			JSONObject trackObj = itemWrapper.getJSONObject("track");

			String name = trackObj.getString("name");
			String uri = trackObj.getString("uri");

			JSONArray artistsJSON = trackObj.getJSONArray("artists");

			ArrayList<String> artistNames = new ArrayList<>();
			ArrayList<String> artistIDs = new ArrayList<>();

			for (int j = 0; j < artistsJSON.length(); j++) {
				JSONObject artistObj = artistsJSON.getJSONObject(j);

				artistNames.add(artistObj.getString("name"));
				artistIDs.add(artistObj.getString("id"));

			}

			Track track = new Track(name, uri, artistIDs, artistNames);
			tracks.add(track);
		}

		return tracks;
	}
	
	

}
