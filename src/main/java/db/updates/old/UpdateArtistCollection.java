package db.updates.old;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;

/**
 * OBSOLETE! Use AddArtistAndSongs
 * Initially I setup the songs collection first, then queried to see how many distinct artists were listed
 * Then I created a document for each one.
 */

public class UpdateArtistCollection {
    public static void main(String[] args) {

        //Connect to MongoDB, and necessary collections
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");
        MongoCollection<Document> artists = db.getCollection("artists");

        Document doc;

        //Gets list of distinct artists listed in songs
        for(String artist: songs.distinct("artist", String.class).into(new ArrayList<String>())){

            //If there isn't a document for the artist
            if ( artists.find(new Document("_id", artist)).first() == null){
                doc = new Document("_id", artist);
                System.out.println("Creating a new document for " + doc.getString("_id"));
                artists.insertOne(doc);
            }

        }

    }
}
