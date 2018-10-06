package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static db.updates.spotify.SpotifyQueries.*;

/**
 * This file goes through the entire artists collection and checks if there are any new songs from each of the
 * artists and attempts to add it to the songs collection.
 */

public class UpdateSongs {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");

        for(Document artistDoc: artistsCollection.find() ){
            String artistName = artistDoc.getString("_id");
            System.out.println("Attempting to add new songs for " + artistName);
            addSongs(songsCollection, artistName);

        }
    }
}
