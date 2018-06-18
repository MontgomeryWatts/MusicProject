package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class FindSongByGenre {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songsCol = db.getCollection("songs");
        MongoCollection<Document> artistsCol = db.getCollection("artists");

        List<Document> artists = artistsCol.find(new Document("genres", "indie r&b")).into(new ArrayList<Document>());
        for(Document doc: artists){
            for( Document song: songsCol.find(new Document("artist", doc.getString("_id"))))
                System.out.println(song.getString("title"));
        }
    }
}
