package api;

public class EndPoint {

	public final static String SCOPES = "user-library-read playlist-modify-private playlist-modify-public playlist-read-private";
	public final static String AUTHORIZATION_ENDPOINT = "https://accounts.spotify.com/authorize";
	public final static String AUTH_URL = "https://accounts.spotify.com/api/token";
	public static final String LIBRARY_ENDPOINT = "https://api.spotify.com/v1/me/tracks?limit=50&offset=0";
	public static final String USER_ID_ENDPOINT = "https://api.spotify.com/v1/";
}
