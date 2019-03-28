package com.spotifydb.model.db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.*;

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
        MongoCollection<Document> artistsCollection = db.getCollection("artists");
        System.out.println("The artist with the least number of songs is " + getExtreme(artistsCollection, MINIMUM));
        System.out.println("The artist with the most number of songs is " + getExtreme(artistsCollection, MAXIMUM));
    }

    private static String getExtreme(MongoCollection<Document> artistsCollection, Bson sortType){
        StringBuilder builder = new StringBuilder();
        Document doc = artistsCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        group("$_id", Accumulators.sum(FIELD_NAME, 1)),
                        sort( sortType ),
                        limit(1)
                )
        ).first();

        Document idDoc = (Document) doc.get("_id");

        builder.append(idDoc.getString("name"))
            .append(" with ")
            .append(doc.getInteger(FIELD_NAME))
            .append(" songs.");

        return builder.toString();
    }
}
