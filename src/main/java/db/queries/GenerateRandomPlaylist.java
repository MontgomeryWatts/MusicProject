package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateRandomPlaylist {
    public static void main(String[] args) {

        //Setup MongoDB connection
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");

        int duration = 3600; //1 hour long or as close to as possible
        List<Document> playlist = new ArrayList<Document>();
        List<Document> possibleSongs;

        while ( (possibleSongs = songs.find(new Document("duration", new Document("$lte", duration))).into(new ArrayList<Document>())).size() != 0  ){
            Document doc = possibleSongs.remove(Math.abs(new Random().nextInt()) % possibleSongs.size());
            playlist.add(doc);
            duration -= doc.getInteger("duration");
        }

        Collections.shuffle(playlist);

        for (Document doc: playlist){
            System.out.println(doc.getString("title") + " - " + doc.getString("artist"));
        }

    }
}
