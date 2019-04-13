package com.spotifydb.model.db.implementations;

import com.spotifydb.model.Preview;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import org.bson.Document;

import java.util.List;
import java.util.Set;

public abstract class DatabaseConnection {
    protected static String BLANK_PROFILE = "/images/blank_profile_pic.png";

    //Artist specific methods
    public abstract Document getArtistByUri(String artistUri);
    public abstract String getRandomArtistUri();
    public abstract long getNumberOfArtists();
    public abstract long getNumberOfArtistsByGenre(String genre);
    public abstract Set<String> getAllFeaturedArtists();
    public abstract Set<String> getAllArtistUris();
    public abstract Iterable<String> getSimilarArtistNames(String name, int offset, int limit);
    public abstract List<Preview> getArtistsByGenre(String genre, int offset, int limit);
    public abstract List<Preview> getArtistsByName(String name, int offset, int limit);
    public abstract List<Preview> getArtistsByRandom();
    public abstract boolean insertArtist(Artist artist, Album[] albums);

    //Genre specific methods
    public abstract List<String> getGenres();
    public abstract List<String> getGenresByLetter(char c);
    public abstract String getNthPopularGenre(int n);

    //Song specific methods
    public abstract int getNumberOfSongs();
    public abstract int getTotalDuration();
    public abstract List<Document> getSongsByCriteria(Set<String> artists, Set<String> genres, boolean allowExplicit, int startYear, int endYear);
    public abstract List<Document> createPlaylist(List<Document> potentialSongs, int playlistDuration);
}
