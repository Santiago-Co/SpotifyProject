package models;

import java.util.Collections;
import java.util.List;

public class TrackList {
	private List<Track> tracks;
	
	public TrackList(List<Track> tracks) {
		this.tracks = tracks; 
	}
	/**
	 * Shuffles the tracks into a new order 
	 * @param tracks
	 * @return shuffle of the current tracks
	 */
	public static List<Track> trueShuffle(List<Track> tracks) {
	    Collections.shuffle(tracks);
	    return tracks;
	}
	
	   @Override
	    public String toString() {
	        StringBuilder sb = new StringBuilder();

	        for (Track song : tracks) {
	            sb.append(song).append("\n");
	            sb.append("--------------------------------------------------\n");
	        }

	        return sb.toString();
	    }
}