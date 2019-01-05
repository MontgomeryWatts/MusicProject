package db.queries;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

@SuppressWarnings("unchecked")
public class DatabaseQueries {
    private static final int SMALL_SAMPLE_SIZE = 25;
    private static final int LARGE_SAMPLE_SIZE = SMALL_SAMPLE_SIZE * 10;

    public static MongoClientURI getMongoClientUri(){
        String envVariable = System.getenv("MONGODB_URI");
        return (envVariable != null) ? new MongoClientURI(envVariable) : new MongoClientURI("mongodb://localhost:27017/spotifydb");
    }

    public static List<Document> createPlaylist(List<Document> potentialSongs, int duration){
        List<Document> playlistDocs = new ArrayList<>();
        while ( duration > 180 && !potentialSongs.isEmpty()){
            Document song = potentialSongs.remove(Math.abs(new Random().nextInt()) % potentialSongs.size());
            int songDuration = song.getInteger("duration");
            if (songDuration <= duration) {
                playlistDocs.add(song);
                duration -= songDuration;
            }
        }
        return playlistDocs;
    }


    public static Document getAlbum(MongoCollection<Document> artistCollection, String artistUri, String albumUri){
        return artistCollection.aggregate(Arrays.asList(
                match( eq("_id.uri", artistUri) ),
                unwind("$albums"),
                match( eq("albums.uri", albumUri)),
                replaceRoot("$albums")
        )).first();
    }

    public static Document getArtist(MongoCollection<Document> artistCollection, String uri){
        return artistCollection.find( eq("_id.uri", uri)).first();
    }

    public static List<Document> getArtistsByGenre(MongoCollection<Document> artistCollection, String genre, int skip){
        return artistCollection.aggregate(Arrays.asList(
                match( eq("genres", genre)),
                skip(SMALL_SAMPLE_SIZE * (skip-1) ),
                limit(SMALL_SAMPLE_SIZE)
        )).into(new ArrayList<>());
    }

    public static List<Document> getArtistsByName(MongoCollection<Document> artistCollection, String artistName){
        String searchPhrase = "\"" + artistName.trim() + "\"";
        Document filter = new Document("$text", new Document("$search", searchPhrase));
        return  artistCollection.find(filter).into(new ArrayList<>());
    }


    public static List<Document> getArtistsByRandom(MongoCollection<Document> artistCollection){
        return artistCollection.aggregate(Arrays.asList(
                sample(LARGE_SAMPLE_SIZE),
                match( exists("_id.image")),
                limit(SMALL_SAMPLE_SIZE)
        )).into(new ArrayList<>());
    }

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

    /**
     * Retrieves an artist's document from the artists collection, performing a case-insensitive text search
     * searching for an exact match on the given artist name. It is necessary to have a text index on the _id field
     * for this method to not throw an exception. This can be performed in the MongoDB shell by entering
     * db.artists.createIndex({'_id.name':"text"}, {default_language:"none"})
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


    public static List<String> getGenresByLetter(MongoCollection<Document> artistCollection, char letter){
        if(!Character.isLetter(letter))
            return new ArrayList<>();

        String pattern = "^" + Character.toLowerCase(letter);
        Document genresDoc = artistCollection.aggregate(Arrays.asList(
                unwind("$genres"),
                match(Filters.regex("genres", pattern, "i" )),
                group("$genres", new ArrayList<>()),
                sort(Sorts.ascending("_id")),
                group(0, Accumulators.push("genres", "$_id"))
        )).first();


        return (genresDoc != null) ? (ArrayList<String>) genresDoc.get("genres") : new ArrayList<>();
    }

    public static int getNumSongs(MongoCollection<Document> artistCollection){
        Document songsDoc = artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs"),
                        group(null, Accumulators.sum("total_songs", 1))
                )
        ).first();

        return songsDoc.getInteger("total_songs");
    }

    public static List<String> getRandomGenres(MongoCollection<Document> artistCollection){
        Document genresDoc = artistCollection.aggregate(Arrays.asList(
                unwind("$genres"),
                group("$genres"),
                sample(SMALL_SAMPLE_SIZE),
                group( null, addToSet("genres","$_id"))
        )).first();
        return (ArrayList<String>)genresDoc.get("genres");
    }

    public static int getTotalDuration(MongoCollection<Document> artistCollection){
        Document songsDoc = artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs"),
                        group(null, Accumulators.sum("duration", "$duration"))
                )
        ).first();

        return songsDoc.getInteger("duration");
    }

    public static List<Document> getAllSongsByCriteria(MongoCollection<Document> artistCollection, Set<String> artists, Set<String> genres,
                                                    boolean allowExplicit, int startYear, int endYear){

        List<Bson> aggregatePipeline = new ArrayList<>();

        //If the user provided artist or genre criteria
        if(artists.size() + genres.size() != 0) {
            aggregatePipeline.add(match(or(in("genres", genres), in("_id.name", artists))));
        }
        else{
            aggregatePipeline.add(sample(LARGE_SAMPLE_SIZE));
        }

        aggregatePipeline.add(unwind("$albums"));

        //If the user wants to filter explicit songs
        if(!allowExplicit) {
            aggregatePipeline.add(match(eq("albums.is_explicit", allowExplicit)));
        }

        //The driver won't let me make the _id field a document when using Aggregates.group
        //Have to make an ugly document like this to do what I want
        Document groupDoc = new Document("$group",
                new Document ("_id", new Document("uri", "$albums.songs.uri")
                        .append("title", "$albums.songs.title")
                        .append("artist", "$_id.name")
                        .append("duration", "$albums.songs.duration")) );

        aggregatePipeline.addAll(
                Arrays.asList(
                    match(and( gte("albums.year", startYear), lte("albums.year", endYear))),
                    unwind("$albums.songs"),
                    project(include("albums.songs")),
                    groupDoc,
                    replaceRoot("$_id")
                )
        );

        return artistCollection.aggregate(aggregatePipeline).into( new ArrayList<>());
    }

    public static Set<Document> getSongsFeaturedOn(MongoCollection<Document> artistCollection, String artistId){
        return artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        match( and( exists("albums.songs.featured"), eq("albums.songs.featured", artistId))),
                        replaceRoot("$albums.songs")
                )
        ).into(new HashSet<>());
    }
}
