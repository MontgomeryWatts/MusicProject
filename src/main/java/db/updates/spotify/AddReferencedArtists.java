package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.DatabaseQueries;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

import static db.updates.spotify.SpotifyQueries.*;


@SuppressWarnings("unchecked")
public class AddReferencedArtists {
    public static void main(String[] args) {
        MongoClientURI uri = DatabaseQueries.getMongoClientUri();
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        MongoCollection<Document> artistsCollection = db.getCollection("artists");

        Set<String> allFeatured = artistsCollection.distinct("albums.songs.featured", String.class).into(new HashSet<>());
        Set<String> inDatabase = artistsCollection.distinct("_id.uri", String.class).into(new HashSet<>());
        allFeatured.removeAll(inDatabase);
        System.out.println("Attempting to insert " + allFeatured.size() + " artists to the database.");
        long numDocsAtStart = artistsCollection.count();
        long startTime = System.currentTimeMillis();
        for(String id: allFeatured) {
            addArtistById(artistsCollection, id);
        }
        long endTime = System.currentTimeMillis();
        long numDocsAtEnd = artistsCollection.count();
        System.out.println("Took " + (endTime - startTime)/1000 + " seconds to add " + (numDocsAtEnd-numDocsAtStart) + " artists.");
    }
}
