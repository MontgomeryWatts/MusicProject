package spring.services;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.implementations.DatabaseConnection;
import db.implementations.MongoConnection;
import db.queries.DatabaseQueries;
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
    public List<Document> getArtists(){
        return db.getArtistsByRandom();
    }
    public List<Document> getArtistsByGenre(String genre, int offset, int limit){
        return db.getArtistsByGenre(genre, offset, limit);
    }
    public List<Document> getArtistsByName(String name, int offset, int limit){
        return db.getArtistsByName(name, offset, limit);
    }
    public List<String> getAllGenres(){
        return db.getGenres();
    }
    public List<String> getGenresByLetter(char letter){ return db.getGenresByLetter(letter);}
    public long getNumArtistsByGenre(String genre){
        return db.getNumberOfArtistsByGenre(genre);
    }
    public String getRandomArtistURI(){return db.getRandomArtistUri();}
}
