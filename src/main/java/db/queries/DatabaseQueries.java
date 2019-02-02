package db.queries;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;

@SuppressWarnings("unchecked")
public class DatabaseQueries {
    public static final int SMALL_SAMPLE_SIZE = 20;
    private static final int LARGE_SAMPLE_SIZE = SMALL_SAMPLE_SIZE * 10;

    /**
     * Parses the collabs set from the Document retrieved from the collabCollection
     * @param artistCollection The MongoCollection containing collab Documents
     * @param artistUri The uri of the artist whose document we are attempting to retrieve
     * @return A List of Strings representing the names of all of the artists the given artist has worked with.
     */
    public static List<String> getArtistCollabNames(MongoCollection<Document> artistCollection, String artistUri){
        Document artistFeaturedDoc = artistCollection.aggregate(Arrays.asList(
                match( eq("_id.uri", artistUri)),
                unwind("$albums"),
                unwind("$albums.songs"),
                match( exists("albums.songs.featured")),
                unwind("$albums.songs.featured"),
                group( "$_id.name", addToSet("featured","$albums.songs.featured"))
        )).first();

        return (artistFeaturedDoc == null ) ? new ArrayList<>() :(ArrayList<String>) artistFeaturedDoc.get("featured");
    }

    public static List<Document> getSongsFeaturedOn(MongoCollection<Document> artistCollection, String artistId){
        return artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        match( eq("albums.songs.featured", artistId)),
                        replaceRoot("$albums.songs")
                )
        ).into(new ArrayList<>());
    }
}
