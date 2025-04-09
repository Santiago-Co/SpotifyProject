package exceptions;

public class ResponseCode {
	public static boolean handleResponseCode(int responseCode) throws SpotifyApiException {
		switch (responseCode) {
		case 200:
			/*
			 * OK - The request has succeeded. The client can read the result of the request
			 * in the body and the headers of the response.
			 */
			return true;
		case 201:
			System.out.println("Esto no debería ser un error pero más adelante se modifica"); 
			throw new SpotifyApiException(
					"Created - The request has been fulfilled" + "and resulted in a new resource being created.");
		case 202:

			throw new SpotifyApiException("Accepted - The request has been accepted "
					+ "for processing, but the processing has not been completed.");

		case 204:

			throw new SpotifyApiException("No Content - The request has succeeded but returns no message body.");

		case 304:

			throw new SpotifyApiException("Not modified");

		case 400:

			throw new SpotifyApiException("Bad Request - The request could not be understood by the server due to"
					+ "malformed syntax. The message body will contain more information");

		case 401:

			throw new SpotifyApiException("Unauthorized - The request requires user authentication or, if the request"
					+ "included authorization credentials, authorization has been refused for those" + "credentials");

		case 403:

			throw new SpotifyApiException(
					"Forbidden - The server understood the request, " + "but is refusing to fulfill it.");

		case 404:

			throw new SpotifyApiException("Not Found - The requested resource "
					+ "could not be found. This error can be due to a temporary or permanent condition.");

		case 429:

			throw new SpotifyApiException("Too Many Requests - Rate limiting has been applied.");

		case 500:

			throw new SpotifyApiException(
					"Internal Server Error. You should never receive this error because our clever "
							+ "coders catch them all ... but if you are unlucky enough to get one, please "
							+ "report it to us through a comment at the bottom of this page.");

		case 502:

			throw new SpotifyApiException("Bad Gateway - The server was acting as a gateway or proxy and received an "
					+ "invalid response from the upstream server.");

		case 503:

			throw new SpotifyApiException("Service Unavailable - The server is currently unable to handle the request "
					+ "due to a temporary condition which will be alleviated after some delay. You "
					+ "can choose to resend the request again.");
		default:
			return false;
		}
	}
}
