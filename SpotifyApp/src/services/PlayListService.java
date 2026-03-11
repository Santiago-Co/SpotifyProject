package services; 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import api.EndPoint;
import auth.TokenManager;
import config.ConfigIDs;
import exceptions.ResponseCode;
import exceptions.SpotifyApiException;
import logSystem.LogSystem;
import models.Track;

public class PlayListService {

    private final static String DEFAULT_PLAYLIST_NAME = "True shuffle";
	
	private static String userID;
    
    /**
     * Fetches the current user's profile from Spotify and caches the User ID.
     * <p>
     * This method performs a GET request to the user profile endpoint, parses the JSON response,
     * and stores the obtained ID into the {@link ConfigIDs} configuration for future use.
     * </p>
     *
     * @throws URISyntaxException If the endpoint URL is malformed.
     * @throws IOException If there is an error connecting to the Spotify API.
     * @throws SpotifyApiException If the API returns a non-success status code.
     */
    public static void generateUserId() throws URISyntaxException, IOException, SpotifyApiException {
    	URL url = new URI(EndPoint.USER_ID_ENDPOINT + "me").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        LogSystem.flujo("URL to get user ID", url.toString());
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        ResponseCode.handleResponseCode(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        JSONObject info = new JSONObject(content.toString());
        
        userID = info.getString("id"); 
        ConfigIDs.setUserID(userID);
    }
        
    /**
     * Convenience method to create a playlist with the default name "True Shuffle".
     * Delegates the creation logic to {@link #createPlaylist(String, String)}.
     *
     * @param userId The Spotify ID of the user creating the playlist.
     * @return The unique identifier (ID) of the newly created playlist.
     * @throws IOException If a network error occurs.
     * @throws SpotifyApiException If the API creation request fails.
     * @throws URISyntaxException If the URL construction fails.
     */
    public static String createPlaylist(String userId) throws IOException, SpotifyApiException, URISyntaxException {
    	return createPlaylist(userId, DEFAULT_PLAYLIST_NAME);
    }
    
    /**
     * Creates a new private playlist for the specified user.
     * <p>
     * The playlist is created with "public" set to {@code false} and a default description.
     * It sends a POST request with the JSON body containing the playlist metadata.
     * </p>
     *
     * @param userId The Spotify ID of the user.
     * @param name The name of the playlist to be created.
     * @return The unique identifier (ID) of the newly created playlist.
     * @throws IOException If the connection fails or input/output errors occur.
     * @throws SpotifyApiException If the Spotify API refuses the creation request.
     * @throws URISyntaxException If the endpoint URL is invalid.
     */
    public static String createPlaylist(String userId, String name) throws IOException, SpotifyApiException, URISyntaxException {

        String endpoint =  EndPoint.USER_ID_ENDPOINT + "users/" + userId + "/playlists";
        URL url = new URI(endpoint).toURL();
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true); 
        
        conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");

        
        JSONObject playlistData = new JSONObject();
        playlistData.put("name", name);
        playlistData.put("description", "Created with True Shuffle");
        playlistData.put("public", false); 

        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = playlistData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        
        int responseCode = conn.getResponseCode();
        ResponseCode.handleResponseCode(responseCode);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        
        in.close(); 
        
        JSONObject info = new JSONObject(content.toString());
        
        
        return info.getString("id"); 
    }
    
    /**
     * Retrieves a mapping of the user's playlists, containing their names and IDs.
     * <p>
     * This method fetches the first 50 playlists (limit=50, offset=0) from the user's library.
     * It parses the JSON response and returns a Key-Value pair where the key is the
     * playlist Name and the value is the playlist ID.
     * </p>
     *
     * @param userID The Spotify ID of the user whose playlists are to be retrieved.
     * @return A {@link Map} where keys are playlist names and values are playlist IDs.
     * @throws IOException If a network connection error occurs.
     * @throws URISyntaxException If the constructed URL is malformed.
     * @throws SpotifyApiException If the Spotify API returns an error code.
     */
    public static Map<String, String> getUserPlaylists(String userID) throws IOException, URISyntaxException, SpotifyApiException {
        
        String endpoint = EndPoint.USER_ID_ENDPOINT + "users/" + userID + "/playlists?limit=50&offset=0";
        URL url = new URI(endpoint).toURL();
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        ResponseCode.handleResponseCode(responseCode);
                
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close(); 
        
        JSONObject response = new JSONObject(content.toString());
        JSONArray items = response.getJSONArray("items");
        
        Map<String, String> playlists = new HashMap<>();
        
        for (int i = 0; i < items.length(); i++) {
            JSONObject playlistObj = items.getJSONObject(i);
            String name = playlistObj.getString("name");
            String id = playlistObj.getString("id");
            
            playlists.put(name, id);
        }
        
        return playlists; 
    }
    
    
    /**
     * Searches for the ID of an existing playlist that matches the application's default name.
     * <p>
     * This method iterates through the user's playlists using pagination (checking the "next" URL)
     * to ensure all playlists are scanned, not just the first page. It stops and returns
     * the ID as soon as a match with {@code playlistNameTF} is found.
     * </p>
     *
     * @param userID The Spotify ID of the user.
     * @param playlistNameTF The name of the playlist to search for.
     * @return The playlist ID if found, or {@code null} if no playlist with the default name exists.
     * @throws IOException If a network error occurs during pagination.
     * @throws URISyntaxException If the API endpoint URLs are invalid.
     * @throws SpotifyApiException If the Spotify API returns a non-success status.
     */
    public static String findPlaylistID(String playlistNameTF, String userID) throws IOException, URISyntaxException, SpotifyApiException {
         
        String currentUrl = EndPoint.USER_ID_ENDPOINT + "users/" + userID + "/playlists?limit=50&offset=0";
        
        while (currentUrl != null && !currentUrl.isEmpty()) {
            
            URL url = new URI(currentUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
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
            JSONArray items = jsonResponse.getJSONArray("items");
            
            for (int i = 0; i < items.length(); i++) {
                JSONObject playlist = items.getJSONObject(i);
                String playlistName = playlist.getString("name");
                if (playlistNameTF.equals(playlistName)) {
                    return playlist.getString("id"); 
                }
            }
            
            if (jsonResponse.has("next") && !jsonResponse.isNull("next")) {
                currentUrl = jsonResponse.getString("next"); 
            } else {
                currentUrl = null; 
            }
        }
        return null;
    }
    
    /**
     * Searches for the ID of the playlist with the default application name.
     * <p>
     * This is a convenience method that delegates the search to {@link #findPlaylistID(String, String)},
     * automatically passing the constant {@code DEFAULT_PLAYLIST_NAME} as the target name.
     * </p>
     *
     * @param userID The Spotify ID of the user.
     * @return The playlist ID if the default playlist exists, or {@code null} otherwise.
     * @throws IOException If a network error occurs.
     * @throws URISyntaxException If the URI is malformed.
     * @throws SpotifyApiException If the API returns an error.
     */
    public static String findPlaylistID(String userID) throws IOException, URISyntaxException, SpotifyApiException {
    	return findPlaylistID(DEFAULT_PLAYLIST_NAME ,userID);
    }
    
    /**
     * Removes a specified list of tracks from an existing playlist.
     * <p>
     * This method validates the playlist ID and handles Spotify's API limitation by processing
     * the deletion in batches of 100 tracks. It constructs the specific JSON structure required
     * by the DELETE endpoint (wrapping track URIs inside individual objects within a "tracks" array).
     * </p>
     *
     * @param playlistID The unique identifier of the target playlist. Cannot be {@code null}.
     * @param tracks The list of {@link Track} objects to be removed.
     * @throws IllegalArgumentException If the provided {@code playlistID} is null.
     * @throws Exception If a network error occurs, the URI is invalid, or the API returns an error response.
     */
    public static void deleteTracksFromPlaylist(String playlistID, List<Track> tracks) throws Exception {
        
    	if(playlistID == null)  {
    		LogSystem.flujo("playlistID_NULL", "playlistID can not be null");
    		throw new IllegalArgumentException("playlistID can not be null");
    	}
    	
    	List<String> uris = getUris(tracks); 
        
        for(int i = 0; i < uris.size(); i+= 100) {
            int end = Math.min(i+100, uris.size());
            List<String> batch = uris.subList(i, end);
            
            String endpoint = EndPoint.USER_ID_ENDPOINT + "playlists/" + playlistID + "/tracks";
            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE"); // Método correcto
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            /*
             * No entiendo una mierda pero bueno, creamos un json object de tracks le metemos la uri lo metemos en un 
             * json array y luego ese json array a otro objeto
             */
            JSONObject body = new JSONObject();
            JSONArray tracksArray = new JSONArray();

            
            for (String uri : batch) {
                JSONObject trackObj = new JSONObject();
                trackObj.put("uri", uri);
                tracksArray.put(trackObj);
            }
            
            body.put("tracks", tracksArray); 

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            ResponseCode.handleResponseCode(responseCode); 
        }
    }
    
    /**
     * Adds a list of tracks to a specific playlist.
     * <p>
     * This method automatically handles Spotify's API limit by splitting the track list
     * into batches of 100 songs per request. It iterates through the list and sends
     * multiple POST requests until all tracks are added.
     * </p>
     *
     * @param playlistId The ID of the target playlist.
     * @param tracks The list of {@link Track} objects to be added.
     * @throws URISyntaxException If the API endpoint URI is incorrect.
     * @throws IOException If a network error occurs during any of the batch requests.
     * @throws SpotifyApiException If the Spotify API returns an error for any batch.
     */
    public static void addTracksToPlaylist(String playlistID, List<Track> tracks) throws URISyntaxException, IOException, SpotifyApiException {
        
        
        List<String> uris = getUris(tracks);
        
        for (int i = 0, batchCounter = 1; i < uris.size(); i += 100, batchCounter++) {
            
            int end = Math.min(i + 100, uris.size());
            
            List<String> batch = uris.subList(i, end);
            
            LogSystem.flujo("Sending bath of songs", "Sending batch " + batchCounter + " of " + batch.size() + " songs");

            String endpoint = EndPoint.USER_ID_ENDPOINT + "/playlists/" + playlistID + "/tracks";
            
            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getInstance().getAccessToken());
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("uris", batch); 

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            ResponseCode.handleResponseCode(responseCode); 
        }
        
        LogSystem.flujo("Songs added successfully", "All songs have been added successfully!");
    }
    
    /**
     * Helper method to extract Spotify URIs from a list of Track objects.
     * <p>
     * It iterates through the provided tracks and collects their unique URI strings
     * into a new list. This transformation is necessary to prepare the data for API
     * endpoints that require a list of ID strings rather than full objects.
     * </p>
     *
     * @param tracks The list of {@link Track} objects to process.
     * @return A list of Strings containing only the URI of each track.
     */
    private static List<String> getUris(List<Track> tracks){
    	List<String> uris = new ArrayList<>();
        for (Track t : tracks) {
            uris.add(t.getUri());
        }
        return uris; 
    }
    
}