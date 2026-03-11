package models;

import java.util.ArrayList;

public class Track {

	private String name;
	private String uri;
	private ArrayList<String> artistID;
	private ArrayList<String> artistName; 

	public Track(String name, String uri, ArrayList<String> artistID, ArrayList<String> artistName) {
		this.name = name;
		this.uri = uri;
		this.artistID = artistID;
		this.artistName = artistName; 
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public ArrayList<String>getArtisID() {
		return artistID;
	}
	
	public ArrayList<String> getArtiName() {
		return artistName; 
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();

	    sb.append("Song: ").append(name).append("\n");
	    sb.append("Artists:\n");

	    for (int i = 0; i < artistName.size(); i++) {
	        sb.append("  - ")
	          .append(artistName.get(i))
	          .append(" (ID: ")
	          .append(artistID.get(i))
	          .append(")\n");
	    }

	    sb.append("URI: ").append(uri);

	    return sb.toString();
	}



}
