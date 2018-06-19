package db.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class FindSongsByGenre {

    public static List<Document> getSongsByGenre(MongoCollection<Document> col, String... genres){
        List<Document> songs = new ArrayList<>();
        for (String genre: genres){
            for(Document song: col.find(new Document("genres", genre))){
                if (!songs.contains(song))
                    songs.add(song);
            }
        }

        return songs;
    }

}
