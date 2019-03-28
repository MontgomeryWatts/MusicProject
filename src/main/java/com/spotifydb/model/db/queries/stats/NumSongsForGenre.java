package com.spotifydb.model.db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.unwind;

public class NumSongsForGenre {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");
        getNumSongsByGenre(artistsCollection, "pop");
    }

    public static void getNumSongsByGenre(MongoCollection<Document> artistsCollection, String genre){
        Document songsDoc = artistsCollection.aggregate(
                Arrays.asList(
                        match(Filters.eq("genres", genre)),
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        group(null, Accumulators.sum("count", 1))
                )
        ).first();
        System.out.println("The " + genre + " genre has " + songsDoc.getInteger("count") + " songs.");
    }
}
