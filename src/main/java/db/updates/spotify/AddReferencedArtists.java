package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;

import static com.mongodb.client.model.Filters.eq;
import static db.updates.spotify.SpotifyQueries.*;


@SuppressWarnings("unchecked")
public class AddReferencedArtists {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");
        MongoCollection<Document> collabsCollection = db.getCollection("collabs");

        HashSet<String> artistNames = new HashSet<>();

        //Add all artists that have appeared on a song by someone already in the database
        for( Document collabDoc: collabsCollection.find() ){
            artistNames.addAll( (ArrayList<String>) collabDoc.get("collabs") );
        }

        for( String name: artistNames){
            Document artistDoc = artistsCollection.find( eq("_id", name) ).first();
            if(artistDoc == null){
                System.out.println("Attempting to create documents for: " + name);
                //addArtistAndSongs(songsCollection, artistsCollection, name);
            }
        }
    }
}
