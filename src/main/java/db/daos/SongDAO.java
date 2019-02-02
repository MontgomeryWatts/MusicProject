package db.daos;

import org.bson.Document;

import java.util.List;
import java.util.Set;

public interface SongDAO {
    int getNumberOfSongs();
    int getTotalDuration();
    List<Document> getSongsByCriteria(Set<String> artists, Set<String> genres, boolean allowExplicit, int startYear, int endYear);
    List<Document> createPlaylist(List<Document> potentialSongs, int playlistDuration);
}
