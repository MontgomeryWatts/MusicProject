package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;

import java.util.Arrays;

public class MostPopularGenre {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");
        for(int i = 1; i <=10; i++)
            getNthPopularGenre(artistsCollection, i);
    }

    public static void getNthPopularGenre(MongoCollection<Document> artistsCollection, int n){
        Document countDoc = artistsCollection.aggregate(Arrays.asList(
                Aggregates.unwind("$genres"),
                Aggregates.sortByCount("$genres"),
                Aggregates.skip(n-1),
                Aggregates.limit(1)
        )).first();
        System.out.println("The #" + n + " genre is " + countDoc.getString("_id") + " with " + countDoc.getInteger("count") + " artists.");
    }
}
