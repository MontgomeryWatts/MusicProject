package db.implementations;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

public class MongoConnection extends DatabaseConnection {
    private MongoCollection<Document> collection;
    private final int SMALL_SAMPLE_SIZE = 20;
    private final int LARGE_SAMPLE_SIZE = SMALL_SAMPLE_SIZE * 10;

    public MongoConnection(){
        MongoClientURI uri = getMongoClientUri();
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        collection = db.getCollection("artists");
    }

    private static MongoClientURI getMongoClientUri(){
        String envVariable = System.getenv("MONGODB_URI");
        return (envVariable != null) ? new MongoClientURI(envVariable) : new MongoClientURI("mongodb://localhost:27017/spotifydb");
    }

    @Override
    public long getNumberOfArtists(){
        return collection.count();
    }

    @Override
    public long getNumberOfArtistsByGenre(String genre){
        return collection.count(eq("genres", genre));
    }

    /**
     * Retrieves the URI of a random artist.
     * @return String representing the URI of a random artist.
     */

    @Override
    public String getRandomArtistUri(){
        Document randomArtistDoc = collection.aggregate(Arrays.asList(
                sample(SMALL_SAMPLE_SIZE),
                project(Projections.include("_id"))
        )).first();
        return randomArtistDoc.getString("_id");
    }

    @Override
    public Set<String> getAllFeaturedArtists(){
        return collection.distinct("albums.songs.featured", String.class).into(new HashSet<>());
    }

    @Override
    public Set<String> getAllArtistUris(){
        return collection.distinct("_id", String.class).into(new HashSet<>());
    }

    @Override
    public List<Document> getArtistsByRandom(){
        return collection.aggregate(Arrays.asList(
                sample(LARGE_SAMPLE_SIZE),
                limit(SMALL_SAMPLE_SIZE)
        )).into(new ArrayList<>());
    }

    /**
     * Retrieves an artist's Document or null if no document exists
     * @param artistUri The URI of the artist to retrieve
     * @return The matching document, or null
     */

    @Override
    public Document getArtistByUri(String artistUri) {
        return collection.find( eq("_id", artistUri)).first();
    }

    @Override
    public List<Document> getArtistsByGenre(String genre, int offset, int limit) {
        return collection.aggregate(Arrays.asList(
                match( eq("genres", genre)),
                skip(offset),
                limit(limit)
        )).into(new ArrayList<>());
    }


    /**
     * Retrieves a List of artist documents from the artists collection, performing a case-insensitive text search
     * It is necessary to have a text index on the _id field for this method to not throw an exception.
     * This can be performed in the MongoDB shell by entering db.artists.createIndex({'name':"text"}, {default_language:"none"})
     * @param name The name of the artist we are searching for
     * @param offset How many artists to skip
     * @param limit How many artists to show up to
     * @return A List of Documents whose artist name match the passed name
     */

    @Override
    public List<Document> getArtistsByName(String name, int offset, int limit) {
        String searchPhrase = "\"" + name.trim() + "\"";
        Document nameFilter = new Document("$text", new Document("$search", searchPhrase));
        return  collection.aggregate(
                Arrays.asList(
                        match(nameFilter),
                        skip(offset),
                        limit(limit)
                )).into(new ArrayList<>());
    }

    @Override
    public List<Document> getArtists(int offset, int limit) {
        return collection.aggregate(
                Arrays.asList(
                        skip(offset),
                        limit(limit)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Attempt to insert an artist document into the collection.
     * @param artistDoc The document to insert
     * @return True if the document was inserted, else false.
     */

    @Override
    public boolean insertArtist(Document artistDoc) {
        if(artistDoc != null) {
            try {
                collection.insertOne(artistDoc);
                return true;
            } catch (MongoWriteException mwe) {
                mwe.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns all genres in the collection in alphabetical order.
     * @return A List of all genres
     */

    @Override
    public List<String> getGenres() {
        ArrayList<String> genres = collection.distinct("genres", String.class)
                .filter(new Document("genres",new Document("$ne",null)))
                .into(new ArrayList<>());
        Collections.sort(genres);
        return genres;
    }

    /**
     * Gets all genres that begin with a certain letter
     * @param c The starting letter
     * @return A List of all genres that start with @v
     */

    @Override
    public List<String> getGenresByLetter(char c) {
        if(!Character.isLetter(c))
            return new ArrayList<>();

        String pattern = "^" + Character.toLowerCase(c);
        Document genresDoc = collection.aggregate(Arrays.asList(
                unwind("$genres"),
                match(Filters.regex("genres", pattern, "i" )),
                group("$genres", new ArrayList<>()),
                sort(Sorts.ascending("_id")),
                group(0, Accumulators.push("genres", "$_id"))
        )).first();


        return (genresDoc != null) ? (ArrayList<String>) genresDoc.get("genres") : new ArrayList<>();
    }

    @Override
    public String getNthPopularGenre(int n){
        Document countDoc = collection.aggregate(Arrays.asList(
                unwind("$genres"),
                sortByCount("$genres"),
                skip(n-1),
                limit(1)
        )).first();
        return countDoc != null ? countDoc.getString("_id"): null;
    }

    @Override
    public int getNumberOfSongs() {
        Document songsDoc = collection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        project(Projections.computed("numSongs",
                                new Document("$size", "$albums.songs"))
                        ),
                        group(null, Accumulators.sum("totalSongs", "$numSongs"))
                )
        ).first();

        return songsDoc.getInteger("totalSongs");
    }

    @Override
    public int getTotalDuration() {
        Document songsDoc = collection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs"),
                        group(null, Accumulators.sum("duration", "$duration"))
                )
        ).first();

        return songsDoc.getInteger("duration");
    }

    @Override
    public List<Document> getSongsByCriteria(Set<String> artists, Set<String> genres, boolean allowExplicit, int startYear, int endYear) {
        List<Bson> aggregatePipeline = new ArrayList<>();

        //If the user provided artist or genre criteria
        if(artists.size() + genres.size() != 0) {
            aggregatePipeline.add(match(or(in("genres", genres), in("name", artists))));
        }
        else{
            aggregatePipeline.add(sample(LARGE_SAMPLE_SIZE)); //No artist or genre criteria, sample random artists
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
                        .append("artist", "$name")
                        .append("duration", "$albums.songs.duration")) );

        aggregatePipeline.addAll(
                Arrays.asList(
                        match(and( gte("albums.year", startYear), lte("albums.year", endYear))),
                        unwind("$albums.songs"),
                        groupDoc,
                        replaceRoot("$_id")
                )
        );

        return collection.aggregate(aggregatePipeline).into( new ArrayList<>());
    }

    @Override
    public List<Document> createPlaylist(List<Document> potentialSongs, int playlistDuration) {
        List<Document> playlistDocs = new ArrayList<>();
        while ( playlistDuration > 180 && !potentialSongs.isEmpty()){
            Document song = potentialSongs.remove(Math.abs(new Random().nextInt()) % potentialSongs.size());
            int songDuration = song.getInteger("duration");
            if (songDuration <= playlistDuration) {
                playlistDocs.add(song);
                playlistDuration -= songDuration;
            }
        }
        return playlistDocs;
    }
}
