package com.spotifydb.application;


import com.spotifydb.model.Preview;
import com.spotifydb.model.PreviewPage;
import com.spotifydb.model.db.implementations.DatabaseConnection;
import com.spotifydb.model.db.implementations.mongo.MongoConnection;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DatabaseService {

    private DatabaseConnection db;

    public DatabaseService(){
        db = new MongoConnection();
    }

    public List<Document> createPlaylist(Set<String> artists, Set<String> genres, int duration,
                                         boolean onlyExplicit, int startYear, int endYear){

        List<Document> potentialSongs = db.getSongsByCriteria(artists, genres, onlyExplicit, startYear, endYear);
        return db.createPlaylist(potentialSongs, duration);
    }

    public Document getArtist(String uri){
        return db.getArtistByUri(uri);
    }
    public Iterable<String> getSimilarArtistNames(String name, int offset, int limit) { return db.getSimilarArtistNames(name, offset, limit);}
    public PreviewPage getArtistsByRandom(){
        return db.getArtistsByRandom();
    }

    public PreviewPage getArtists(String genre, String name, int offset, int limit){
        return db.getArtists(genre, name, offset, limit);
    }

    public PreviewPage getAlbums(String name, Integer year, int offset, int limit){
        return db.getAlbums(name, year, offset, limit);
    }

    public List<String> getAllGenres(){
        return db.getGenres();
    }
    public List<String> getGenresByLetter(char letter){ return db.getGenresByLetter(letter);}
    public String getRandomArtistURI(){return db.getRandomArtistUri();}
}
