package config;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ConfigIDs {

	private static Properties properties = new Properties();

	private static final String ENV_FILE = ".env";
	private static final String CLIENT_ID = "SPOTIFY_CLIENT_ID";
	private static final String REDIRECT_URI = "REDIRECT_URI";
	private static final String CLIENT_SECRET = "SPOTIFY_CLIENT_SECRET";
	private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
	private static final String USER_ID = "USER_ID";

	static {
		try (FileInputStream fis = new FileInputStream(ENV_FILE)) {
			properties.load(fis);
		} catch (IOException e) {
			System.err.println("ERROR: Could not read the .env file");
		}
	}

	public static String getClientId() {
		return properties.getProperty(CLIENT_ID);
	}

	public static String getClientSecret() {
		return properties.getProperty(CLIENT_SECRET);
	}

	public static String getRedirectURI() {
		return properties.getProperty(REDIRECT_URI);
	}

	public static String getRefreshToken() {
		String refreshToken = properties.getProperty(REFRESH_TOKEN);

		if (refreshToken == null || refreshToken.isEmpty()) {
			throw new IllegalStateException("ERROR: REFRESH_TOKEN is not configured in the .env file. \" +\r\n"
					+ "                                        \"You must complete the authorisation process first.");
		}

		return refreshToken;
	}

	public static boolean hasRefreshToken() {
		String token = properties.getProperty(REFRESH_TOKEN);
		
		return token != null && !token.trim().isEmpty();
	}
	
	public static boolean hasUserID() {
		String token = properties.getProperty(USER_ID);
		return token != null && !token.trim().isEmpty();
	}
	
	public static String getUserID() {
		String userID = properties.getProperty(USER_ID);

		if (userID == null || userID.isEmpty()) {
			throw new IllegalStateException("ERROR: USER_ID is not configured in the .env file.");
		}
		return userID;
	}

	public static void setUserID(String userID) {
		properties.setProperty(USER_ID, userID);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENV_FILE))) {
			for (String key : properties.stringPropertyNames()) {
				String value = properties.getProperty(key);
				writer.write(key + "=" + value);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("ERROR: When writting the UserID into the .env" + e.getMessage());
		}

	}

	public static void setRefreshToken(String refreshToken) {

		properties.setProperty("REFRESH_TOKEN", refreshToken);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENV_FILE))) {
			for (String key : properties.stringPropertyNames()) {
				String value = properties.getProperty(key);
				writer.write(key + "=" + value);
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("ERROR: When writting the Refresh Token into the .env" + e.getMessage());
		}
	}

}
