package db.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("unchecked")
public class DatabaseQueries {
    public static List<String> getArtistCollabs(MongoCollection<Document> collabCollection, String artistName){
        Document doc = collabCollection.find( eq("_id", artistName)).first();
        return (ArrayList<String>) doc.get("collabs");
    }

    public static Document getArtistDoc(MongoCollection<Document> artistCollection, String artistName){

        //Backslashes cause MongoDB to match against the entire phrase as opposed to individual words
        //Needed for artists that may contain the same words in their names e.g. Danger Doom and MF DOOM
        String searchPhrase = "\"" + artistName + "\"";

        Document filter = new Document("$text", new Document("$search", searchPhrase));
        Document doc = artistCollection.find(filter).first();

        return doc;
    }

    public static HashSet<Document> getArtistSongs(MongoCollection<Document> songsCollection, String artistName){
        return songsCollection.find( eq("artist", artistName) )
                .into(new HashSet<>());
    }

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
