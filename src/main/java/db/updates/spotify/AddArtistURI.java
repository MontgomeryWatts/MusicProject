package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wrapper.spotify.SpotifyApi;
import org.bson.Document;

/**
 * Written before I added a spotify field to be added by default in AddArtists
 * Looks for artist documents without a spotify field, creates one, and fills it with the correct URI
 */

public class AddArtistURI {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artists = db.getCollection("artists");
        SpotifyApi spotifyApi = SpotifyHelpers.createSpotifyAPI();
        Document filter = new Document( "spotify", new Document("$exists", false));

        //Iterates through every artist without a spotify field and adds one
        for (Document doc: artists.find(filter)){
            String uri = SpotifyHelpers.getArtistURI(spotifyApi, doc.getString("_id"));
            Document replacement = new Document("$set", new Document("spotify", uri));
            artists.updateOne(doc, replacement);
        }

    }
}
