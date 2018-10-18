package db.queries;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("unchecked")
public class DatabaseQueries {

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
     * Retrieves an artist's document from the artists collection, performing a case-insensitive text search
     * searching for an exact match on the given artist name. It is necessary to have a text index on the _id field
     * for this method to not throw an exception. This can be performed in the MongoDB shell by entering
     * db.artists.createIndex({'_id':"text"})
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

}
