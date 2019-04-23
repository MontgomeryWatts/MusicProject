package com.spotifydb.application;


import com.spotifydb.model.Preview;
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
    public List<Preview> getArtistsByRandom(){
        return db.getArtistsByRandom();
    }

    public List<Preview> getArtists(String genre, String name, int offset, int limit){
        return db.getArtists(genre, name, offset, limit);
    }

    public List<Preview> getAlbums(String name, Integer year, int offset, int limit){
        return db.getAlbums(name, year, offset, limit);
    }

    public long getNumAlbumsBy(String name, Integer year){
        return db.getNumAlbumsBy(name, year);
    }

    public List<String> getAllGenres(){
        return db.getGenres();
    }
    public List<String> getGenresByLetter(char letter){ return db.getGenresByLetter(letter);}
    public long getNumArtistsBy(String genre, String name){
        return db.getNumArtistsBy(genre, name);
    }
    public String getRandomArtistURI(){return db.getRandomArtistUri();}
}
