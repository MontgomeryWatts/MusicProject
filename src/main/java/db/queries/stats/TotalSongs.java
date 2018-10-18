package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;

import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.replaceRoot;
import static com.mongodb.client.model.Aggregates.unwind;

public class TotalSongs {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");


        Document songsDoc = artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs"),
                        group(null, Accumulators.sum("total_songs", 1))
                )
        ).first();

        System.out.println( songsDoc.getInteger("total_songs"));
    }
}
