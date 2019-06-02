package com.spotifydb.model.db.implementations;

import com.spotifydb.model.PreviewPage;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public abstract class DatabaseConnection {
    protected static String BLANK_PROFILE = "/images/blank_profile_pic.png";
    protected static String BLANK_ALBUM = "/images/no_album_art.png";
    public static int RESULTS_PER_PAGE = 20;

    //Artist specific methods
    public abstract Document getArtistById(String artistId);
    public abstract String getRandomArtistId();
    public abstract int getNumArtists();
    public abstract Set<String> getAllFeaturedArtists();
    public abstract Set<String> getAllArtistIds();
    public abstract Iterable<String> getSimilarArtistNames(String name, int offset, int limit);

    public abstract PreviewPage getArtists(String genre, String name, int offset, int limit);
    public abstract PreviewPage getArtistsByRandom();
    public abstract void insertArtist(Artist artist, Album[] albums);

    public abstract Document getAlbumPage(String albumID);
    public abstract PreviewPage getAlbums(String name, Integer year, int offset, int limit);

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
