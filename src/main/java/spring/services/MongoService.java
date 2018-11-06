package spring.services;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.DatabaseQueries;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoService {

    private MongoCollection<Document> collection;

    public MongoService(){
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        collection = db.getCollection("artists");
    }

    public Document getArtist(String uri){
        return DatabaseQueries.getArtist(collection, uri);
    }
    public List<Document> getArtists(){
        return DatabaseQueries.getArtists(collection);
    }
    public List<Document> getArtistsByGenre(String genre){
        return DatabaseQueries.getArtistsByGenre(collection, genre);
    }
}
