package exceptions;

public class SpotifyApiException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SpotifyApiException(String message) {
		super(message);
	}
}
