package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;

import static db.updates.spotify.SpotifyQueries.*;


@SuppressWarnings("unchecked")
public class AddReferencedArtists {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");

        Document namesDoc = artistsCollection.aggregate(
                Arrays.asList(
                        Aggregates.unwind("$albums"),
                        Aggregates.unwind("$albums.songs"),
                        Aggregates.replaceRoot("$albums.songs"),
                        Aggregates.match(Filters.exists("featured")),
                        Aggregates.unwind("$featured"),
                        Aggregates.group("All referenced artists", Accumulators.addToSet("ids", "$featured"))
                )
        ).first();

        ArrayList<String> artistIds = (ArrayList<String>) namesDoc.get("ids");

        for(String id: artistIds) {
            addArtistById(artistsCollection, id);
        }
    }
}
