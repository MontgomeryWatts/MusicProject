package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.DatabaseQueries;
import org.bson.Document;


public class TotalSongs {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");

        System.out.println(DatabaseQueries.getNumSongs(artistCollection));
    }
}
