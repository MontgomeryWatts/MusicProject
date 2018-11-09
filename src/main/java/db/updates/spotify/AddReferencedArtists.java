package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

import static db.updates.spotify.SpotifyQueries.*;


@SuppressWarnings("unchecked")
public class AddReferencedArtists {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");

        Set<String> allFeatured = artistsCollection.distinct("albums.songs.featured", String.class).into(new HashSet<>());
        Set<String> inDatabase = artistsCollection.distinct("_id.uri", String.class).into(new HashSet<>());
        allFeatured.removeAll(inDatabase);
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
