package main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import api.EndPoint;
import auth.TokenManager;
import config.ConfigIDs;
import exceptions.SpotifyApiException;
import models.Track;
import models.TrackList;
import services.LibraryService;
import services.PlayListService;

public class App {

	public static void main(String[] args) throws Exception {
		
		String code;
		Scanner sc = new Scanner(System.in);
		TokenManager tm = TokenManager.getInstance();
		
		if(!ConfigIDs.hasRefreshToken()) {
			//mostrarVentana(acortarUrl(tm.getAuthUrl(EndPoint.SCOPES)));
			//TODO por algun motivo ya no funciona el acortarUrl me genera la misma url deprecated y deja de funcar idk why
			mostrarVentana(tm.getAuthUrl(EndPoint.SCOPES));
			System.out.print("Copy here the link: ");
			code = sc.nextLine();

			try {
				tm.exchangeCodeForTokens(code);
			} catch (IOException | SpotifyApiException e) {
				e.printStackTrace();
			}

			System.out.println("Successfully authenticated! Token received");
		}
		
		PlayListService.generateUserId();
		String userID = ConfigIDs.getUserID();
		List<Track> tracks = LibraryService.getLikedSongs();
		tracks = TrackList.trueShuffle(tracks);
		String idPlayList = PlayListService.findPlaylistID(userID);
		
		if(idPlayList != null) {
			PlayListService.deleteTracksFromPlaylist(idPlayList, tracks);
		}else {
			idPlayList = PlayListService.createPlaylist(ConfigIDs.getUserID());

		}		
		PlayListService.addTracksToPlaylist(idPlayList, tracks);
		sc.close();

	}

	// TODO Add notification for the user to know that if they want to continue they
	public static void mostrarVentana(String url) {
		JTextField textField = new JTextField(url);
		textField.setEditable(false);
		JOptionPane.showMessageDialog(null, textField, "Copy your url here", JOptionPane.INFORMATION_MESSAGE);
	}

	public static String acortarUrl(String urlLarga) {

		String consulta = "https://tinyurl.com/api-create.php?url=" + urlLarga;

		try {
			URL url = new URI(consulta).toURL();

			try (Scanner sc = new Scanner(url.openStream())) {
				if (sc.hasNextLine()) {
					return sc.nextLine();
				}
			}
		} catch (Exception e) {
			System.err.println("Error al acortar: " + e.getMessage());
		}

		return urlLarga;
	}
	
	public static void codigoIgnorandoExistenciaDeUnaListaYaCreada() throws URISyntaxException, IOException, SpotifyApiException {
		String code;
		Scanner sc = new Scanner(System.in);
		TokenManager tm = TokenManager.getInstance();
		
		if(!ConfigIDs.hasRefreshToken()) {
			//mostrarVentana(acortarUrl(tm.getAuthUrl(EndPoint.SCOPES)));
			//TODO por algun motivo ya no funciona el acortarUrl me genera la misma url deprecated y deja de funcar idk why
			mostrarVentana(tm.getAuthUrl(EndPoint.SCOPES));
			System.out.print("Copy here the link: ");
			code = sc.nextLine();

			try {
				tm.exchangeCodeForTokens(code);
			} catch (IOException | SpotifyApiException e) {
				e.printStackTrace();
			}

			System.out.println("Successfully authenticated! Token received");
		}
		
		PlayListService.generateUserId();
		List<Track> tracks = LibraryService.getLikedSongs();
		tracks = TrackList.trueShuffle(tracks);
		

		
		String idPLayList = PlayListService.createPlaylist(ConfigIDs.getUserID());
		
		PlayListService.addTracksToPlaylist(idPLayList, tracks);
	
		sc.close();
	}
	
}
