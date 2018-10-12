package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

/**
 * Prints the name and number of the artist with the most and the artist with the least songs in the database.
 */

public class MaxMinSongs {
    private static final String FIELD_NAME = "num_songs";
    private static final Bson MINIMUM = Sorts.ascending(FIELD_NAME);
    private static final Bson MAXIMUM = Sorts.descending(FIELD_NAME);

    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        System.out.println("The artist with the least number of songs is " + getExtreme(songsCollection, MINIMUM));
        System.out.println("The artist with the most number of songs is " + getExtreme(songsCollection, MAXIMUM));
    }

    public static String getExtreme(MongoCollection<Document> songsCollection, Bson sortType){
        StringBuilder builder = new StringBuilder();
        Document doc = songsCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$artist", Accumulators.sum(FIELD_NAME, 1)),
                        Aggregates.sort( sortType ),
                        Aggregates.limit(1)
                )
        ).first();

        builder.append(doc.getString("_id"))
            .append(" with ")
            .append(doc.getInteger(FIELD_NAME))
            .append(" songs.");

        return builder.toString();
    }
}
