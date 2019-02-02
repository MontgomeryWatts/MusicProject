package db.daos;

import org.bson.Document;

import java.util.List;
import java.util.Set;

public interface ArtistDAO {
    Document getArtistByUri(String artistUri);
    String getRandomArtistUri();
    long getNumberOfArtists();
    long getNumberOfArtistsByGenre(String genre);
    Set<String> getAllFeaturedArtists();
    Set<String> getAllArtistUris();
    List<Document> getArtistsByGenre(String genre, int offset, int limit);
    List<Document> getArtistsByName(String name, int offset, int limit);
    List<Document> getArtists(int offset, int limit);
    List<Document> getArtistsByRandom();

    boolean insertArtist(Document artistDoc);
}
