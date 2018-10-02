package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * The purpose of this program is to create a collection that indicates which artists have collaborated on
 * songs with each other without having to do multiple queries at runtime to gather the same information.
 * Currently intended to be run once after all artists and songs have been added to the database.
 */

public class AddCollabs {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");
        MongoCollection<Document> artists = db.getCollection("artists");
        MongoCollection<Document> collabs = db.getCollection("collabs");

        for(Document artistDoc: artists.find()){
            String artistName = artistDoc.getString("_id");
            ArrayList<String> featuredArtists = new ArrayList<>();
            for (Document songDoc: songs.find( and(eq("artist", artistName), exists("featured") )) ){
                for (String featured: (List<String>) songDoc.get("featured")){
                    if(!featuredArtists.contains(featured)){ //Assuming few elements so linear search isn't costly
                        featuredArtists.add(featured);
                    }
                }
            }
            Document collabDoc = new Document("_id", artistName)
                    .append("collabs", featuredArtists);
            collabs.insertOne(collabDoc);
        }
    }
}
