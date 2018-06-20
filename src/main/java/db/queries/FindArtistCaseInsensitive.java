package db.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * MongoDB by default is case sensitive when searching documents. I've used its built in createIndex function
 * to make a text index for the artist collection. This file is just to make sure it works how I think it does
 */

public class FindArtistCaseInsensitive {
    public static Document findArtist(MongoCollection<Document> col, String artistName){
        Document doc;

        //Backslashes cause MongoDB to match against the entire phrase as opposed to individual words
        //Needed for artists that may contain the same words in their names e.g. Danger Doom and MF DOOM
        String searchPhrase = "\"" + artistName + "\"";

        Document filter = new Document("$text", new Document("$search", searchPhrase));
        doc = col.find(filter).first();

        return doc;
    }
}
