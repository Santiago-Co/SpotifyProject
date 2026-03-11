package main;



import config.ConfigIDs;
import services.LibraryService;
import services.PlayListService;

public class pruebas {

	public static void main(String[] args) {
		try {
			//PlayListService.generateUserId();
			//System.out.println(PlayListService.getUserPlaylists(ConfigIDs.getUserID()).toString());
			String idPlaylist = PlayListService.findPlaylistID(ConfigIDs.getUserID());
			PlayListService.deleteTracksFromPlaylist(idPlaylist, LibraryService.getLikedSongs());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
