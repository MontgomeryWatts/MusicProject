package com.spotifydb.model.db.implementations;

import com.spotifydb.model.Preview;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import org.bson.Document;

import java.util.List;
import java.util.Set;

public interface DatabaseConnection {

    //Artist specific methods
    Document getArtistByUri(String artistUri);
    String getRandomArtistUri();
    long getNumberOfArtists();
    long getNumberOfArtistsByGenre(String genre);
    Set<String> getAllFeaturedArtists();
    Set<String> getAllArtistUris();
    Iterable<String> getSimilarArtistNames(String name, int offset, int limit);
    Iterable<Preview> getArtistsByGenre(String genre, int offset, int limit);
    Iterable<Preview> getArtistsByName(String name, int offset, int limit);
    Iterable<Preview> getArtistsByRandom();
    boolean insertArtist(Artist artist, Album[] albums);

    //Genre specific methods
    List<String> getGenres();
    List<String> getGenresByLetter(char c);
    String getNthPopularGenre(int n);

    //Song specific methods
    int getNumberOfSongs();
    int getTotalDuration();
    List<Document> getSongsByCriteria(Set<String> artists, Set<String> genres, boolean allowExplicit, int startYear, int endYear);
    List<Document> createPlaylist(List<Document> potentialSongs, int playlistDuration);
}
