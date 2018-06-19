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

        Document filter = new Document("$text", new Document("$search", artistName));
        doc = col.find(filter).first();

        return doc;
    }
}
