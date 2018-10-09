package db.queries;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("unchecked")
public class DatabaseQueries {

    //How many songs should be returned by a random search
    private static final int SONGS_TO_RETURN = 100;

    /**
     * Parses the collabs set from the Document retrieved from the collabCollection
     * @param collabCollection The MongoCollection containing collab Documents
     * @param artistName The name of the artist whose document we are attempting to retrieve
     * @return A Set of Strings representing the names of all of the artists the given artist has worked with.
     */
    public static Set<String> getArtistCollabNames(MongoCollection<Document> collabCollection, String artistName){
        Document doc = collabCollection.find( eq("_id", artistName)).first();
        return (doc == null) ? new HashSet<>() : new HashSet<>( (ArrayList<String>) doc.get("collabs") );
    }

    /**
     * Parses the genres set from the Document retrieved by getArtistDoc.
     * @param artistCollection The MongoCollection containing artist Documents
     * @param artistName The name of the artist whose document we are attempting to retrieve
     * @return A Set of Strings representing all of the artist's attributed genres.
     */

    public static Set<String> getArtistGenres(MongoCollection<Document> artistCollection, String artistName){
        Document artistDoc = getArtistDoc(artistCollection, artistName);
        return (artistDoc == null) ? new HashSet<>() : new HashSet<>( (ArrayList<String>) artistDoc.get("genres") );
    }

    /**
     * Retrieves an artist's document from the artists collection, performing a case-insensitive text search
     * searching for an exact match on the given artist name.
     * @param artistCollection The MongoCollection containing artist Documents
     * @param artistName The name of the artist whose document we are attempting to retrieve
     * @return The first Document whose _id contains keywords in the searchPhrase, or null.
     */

    public static Document getArtistDoc(MongoCollection<Document> artistCollection, String artistName){

        //Backslashes cause MongoDB to match against the entire phrase as opposed to individual words
        //Needed for artists that may contain the same words in their names e.g. Danger Doom and MF DOOM
        String searchPhrase = "\"" + artistName + "\"";

        Document filter = new Document("$text", new Document("$search", searchPhrase));
        return  artistCollection.find(filter).first();
    }

    /**
     * Returns {@value SONGS_TO_RETURN} song Documents from the songCollection, chosen at random
     * @param songsCollection The MongoCollection containing song Documents
     * @return A Set of {@value SONGS_TO_RETURN} random song Documents
     */
    public static Set<Document> getSongsByRandom(MongoCollection<Document> songsCollection){
        return songsCollection.aggregate(Collections.singletonList(Aggregates.sample(SONGS_TO_RETURN))).into(new HashSet<>());
    }

    /**
     * Retreives all song Documents in the database from a given artist
     * @param songsCollection A MongoCollection containing the song Documents
     * @param artistName The name of the artist to search for
     * @return A Set of all song Documents by the given artist
     */

    public static Set<Document> getSongsByArtist(MongoCollection<Document> songsCollection, String artistName){
        return songsCollection.find( eq("artist", artistName) )
                .into(new HashSet<>());
    }

    public static Set<Document> getSongsByGenre(MongoCollection<Document> artistCollection, MongoCollection<Document> songsCollection, Set<String> genres){
        Set<Document> songs = new HashSet<>();
        Set<String> artistNames = new HashSet<>();
        for (String genre: genres){
            for(Document artistDoc: artistCollection.find(new Document("genres", genre))){
                artistNames.add( artistDoc.getString("_id") );
            }
        }

        for(String artistName: artistNames){
            songs.addAll(getSongsByArtist(songsCollection, artistName));
        }

        return songs;
    }
}
