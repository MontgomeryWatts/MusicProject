package spring.services;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.DatabaseQueries;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MongoService {

    private MongoCollection<Document> collection;

    public MongoService(){
        MongoClientURI uri = DatabaseQueries.getMongoClientUri();
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        collection = db.getCollection("artists");
    }

    public List<Document> createPlaylist(Set<String> artists, Set<String> genres, int duration,
                                         boolean onlyExplicit, int startYear, int endYear){

        List<Document> potentialSongs = DatabaseQueries.getAllSongsByCriteria(collection, artists, genres, onlyExplicit, startYear, endYear);

        return DatabaseQueries.createPlaylist(potentialSongs, duration);

    }

    public Document getArtist(String uri){
        return DatabaseQueries.getArtist(collection, uri);
    }
    public List<Document> getArtists(){
        return DatabaseQueries.getArtistsByRandom(collection);
    }
    public List<Document> getArtistsByGenre(String genre, int toSkip){
        return DatabaseQueries.getArtistsByGenre(collection, genre, toSkip);
    }
    public List<Document> getArtistsByName(String name){
        return DatabaseQueries.getArtistsByName(collection, name);
    }
    public List<String> getGenres(){
        return DatabaseQueries.getRandomGenres(collection);
    }
    public List<String> getGenresByLetter(char letter){ return DatabaseQueries.getGenresByLetter(collection, letter);}
    public long getNumArtistsByGenre(String genre){
        return DatabaseQueries.getNumArtistsByGenre(collection, genre);
    }
    public String getRandomArtistURI(){return DatabaseQueries.getRandomArtistURI(collection);}
}
