package com.spotifydb.model.db.implementations;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.spotifydb.model.Preview;
import com.spotifydb.model.db.spotify.AddReferencedArtists;
import com.wrapper.spotify.model_objects.specification.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.spotifydb.ui.controllers.ArtistController;

import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Projections.*;

public class MongoConnection extends DatabaseConnection {
    private MongoCollection<Document> collection;
    private static final int SMALL_SAMPLE_SIZE = 20;
    private static final int LARGE_SAMPLE_SIZE = SMALL_SAMPLE_SIZE * 10;

    public MongoConnection(){
        MongoClientURI uri = getMongoClientUri();
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        collection = db.getCollection("artists");
    }

    /**
     * Create a MongoClientURI so that the MongoConnection knows where to attempt to connect.
     * Tries to retrieve an environment variable, otherwise uses localhost.
     * @return MongoClientURI specifying connection details
     */

    private static MongoClientURI getMongoClientUri(){
        String envVariable = System.getenv("MONGODB_URI");
        return (envVariable != null) ? new MongoClientURI(envVariable) : new MongoClientURI("mongodb://localhost:27017/spotifydb");
    }

    private static Preview createPreviewFromArtistDoc(Document doc){
        List<String> imageUrls = (ArrayList<String>) doc.get("images");
        String imageUrl = (imageUrls.size() != 0) ? imageUrls.get(0) : BLANK_PROFILE;
        String id = doc.getString("_id");
        String text = doc.getString("name");
        return new Preview(id, imageUrl, text);
    }


    /**
     * The collection attribute has its documents designed around artists. That is,
     * each document represents one artist in the database.
     * @return The number of artists stored in the database.
     */
    @Override
    public long getNumberOfArtists(){
        return collection.count();
    }


    /**
     *
     * @param genre String representing the genre to search for
     * @return The number of artists that have that genre in their genres array
     */
    @Override
    public long getNumberOfArtistsByGenre(String genre){
        return collection.count(eq("genres", genre));
    }

    /**
     * Retrieves the URI of a random artist. This is used for a redirect in {@link ArtistController#getRandom()}
     * @return String representing the URI of a random artist.
     */

    @Override
    public String getRandomArtistUri(){
        Document randomArtistDoc = collection.aggregate(Arrays.asList(
                sample(SMALL_SAMPLE_SIZE),
                project(include("_id"))
        )).first();
        return randomArtistDoc.getString("_id");
    }

    /**
     * Gets a set of the URIs of all artists that have featured on any song ever (in the database). Currently used by
     * {@link AddReferencedArtists} in order to 'organically' grow the database.
     * @return A Set of the URIs of all artists featured
     */
    @Override
    public Set<String> getAllFeaturedArtists(){
        return collection.distinct("albums.songs.featured", String.class).into(new HashSet<>());
    }

    /**
     * Gets a set of the URIs of all artists that are currently in the database. Currently used by {@link AddReferencedArtists}
     * in conjunction with {@link #getAllFeaturedArtists()} in order to only add artists not in the database already.
     * @return A Set of the URIs of all artists
     */

    @Override
    public Set<String> getAllArtistUris(){
        return collection.distinct("_id", String.class).into(new HashSet<>());
    }


    /**
     * Returns a list of up to {@value #SMALL_SAMPLE_SIZE} artist Documents selected by random.
     * @return A List of random artist Documents
     */
    @Override
    public List<Preview> getArtistsByRandom(){
        return collection.aggregate(Arrays.asList(
                sample(LARGE_SAMPLE_SIZE),
                project(include("images", "name")),
                limit(SMALL_SAMPLE_SIZE)
        )).map( MongoConnection::createPreviewFromArtistDoc ).into(new ArrayList<>());
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

    /**
     * Gets a list of artist Documents by genre. Offset and limit are passed for use in pagination.
     * @param genre The genre to match on
     * @param offset How many artist Documents to skip
     * @param limit How many artist Documents to return up to
     * @return A list of artist Documents that have the given genre
     */

    @Override
    public List<Preview> getArtistsByGenre(String genre, int offset, int limit) {
        return collection.aggregate(Arrays.asList(
                match( eq("genres", genre)),
                project(include("images", "name")),
                skip(offset),
                limit(limit)
        )).map( MongoConnection::createPreviewFromArtistDoc ).into(new ArrayList<>());
    }

    @Override
    public Iterable<String> getSimilarArtistNames(String name, int offset, int limit) {
        name = name.replace('+', ' '); // jQuery replaces spaces with +'s
        Pattern namePattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        return  collection.aggregate(
                Arrays.asList(
                        match(Filters.regex("name", namePattern)),
                        project(include("name")),
                        skip(offset),
                        limit(limit)
                )).map((Document document) -> document.getString("name"));
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
    public List<Preview> getArtistsByName(String name, int offset, int limit) {
        String searchPhrase = "\"" + name.trim() + "\"";
        Document nameFilter = new Document("$text", new Document("$search", searchPhrase));
        return  collection.aggregate(
                Arrays.asList(
                        match(nameFilter),
                        project(include("images", "name")),
                        skip(offset),
                        limit(limit)
                )).map( MongoConnection::createPreviewFromArtistDoc )
                .into(new ArrayList<>());
    }

    /**
     * Attempt to insert an artist document into the collection.
     * @return True if the document was inserted, else false.
     */

    @Override
    public boolean insertArtist(Artist artist, Album[] albums) {
        if(albums.length == 0) //Only add artists with music, only implemented because storage costs money!
            return false;

        Document artistDoc = createArtistDoc(artist, albums);

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

    private Document createArtistDoc(Artist artist, Album[] albums){
        String id = artist.getId();
        List<String> genres = Arrays.asList(artist.getGenres());
        List<String> imageUrls = new ArrayList<>();
        Image[] images = artist.getImages();
        for(Image image: images)
            imageUrls.add(image.getUrl());

        Document artistDoc = new Document("_id", id)
                .append("name", artist.getName())
                .append("images", imageUrls);

        if(genres.size() != 0){
            artistDoc.append("genres", genres);
        }

        if(albums.length != 0) {
            List<Document> albumDocs = createAlbumDocuments(id, albums);
            artistDoc.append("albums", albumDocs);
        }

        return artistDoc;
    }

    private List<Document> createAlbumDocuments(String artistId, Album[] albums){
        List<Document> albumDocuments = new ArrayList<>();
        for (Album album: albums) {
            int year = Integer.parseInt(album.getReleaseDate().substring(0, 4)); //Dates are in YYYY-MM-DD format
            Document albumDoc = new Document("title", album.getName())
                    .append("uri", album.getUri())
                    .append("year", year);


            Image[] albumImages = album.getImages();
            if(albumImages.length != 0) {
                albumDoc.append("image", albumImages[0].getUrl()); //The first image in the images array is the largest
            }

            List<Document> songDocuments = createSongDocuments(artistId, album);
            albumDoc.append("songs", songDocuments);
            albumDocuments.add(albumDoc);
        }
        return albumDocuments;
    }


    private List<Document> createSongDocuments(String artistId, Album album){
        List<Document> songDocuments = new ArrayList<>();

        TrackSimplified[] tracks = album.getTracks().getItems();
        for(TrackSimplified track: tracks){
            Document songDoc = new Document("title", track.getName())
                    .append("duration", track.getDurationMs() / 1000)
                    .append("explicit", track.getIsExplicit())
                    .append("uri", track.getUri());

            Set<String> featured= getFeatured(artistId, track);
            if(!featured.isEmpty()) {
                songDoc.append("featured", featured);
            }

            songDocuments.add(songDoc);
        }
        return songDocuments;
    }

    private Set<String> getFeatured(String artistId, TrackSimplified track) {
        Set<String> featured = new HashSet<>();
        for (ArtistSimplified a : track.getArtists()) {
            featured.add(a.getId());
        }
        featured.remove(artistId);
        return featured;
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
     * @return A List of all genres that start with
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
    public Iterable<String> getSimilarGenres(String genre, int offset, int limit){
        genre = genre.replace('+', ' '); // jQuery replaces spaces with +'s
        Pattern genrePattern = Pattern.compile(genre, Pattern.CASE_INSENSITIVE);
        return collection.aggregate(
                Arrays.asList(
                        unwind("$genres"),
                        match(Filters.regex("genres", genrePattern)),
                        project(include("genres")),
                        group("$genres", new ArrayList<>()),
                        skip(offset),
                        limit(limit)
                )).map((Document doc) -> doc.getString("_id"));
    }


    /**
     * Determines which genre is the 'nth' most popular, e.g. passing 1 will find the most popular genre.
     * @param n The place of the genre you're interested in finding
     * @return String representing the most popular genre
     */
    @Override
    public String getNthPopularGenre(int n){
        Document countDoc = collection.aggregate(Arrays.asList(
                unwind("$genres"),
                sortByCount("$genres"),
                skip(n-1),
                limit(1)
        )).first();
        return (countDoc != null) ? countDoc.getString("_id") : null;
    }

    /**
     * Straightforward.
     * @return int specifying how many songs are in the database.
     */
    @Override
    public int getNumberOfSongs() {
        Document songsDoc = collection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        project(computed("numSongs",
                                new Document("$size", "$albums.songs"))
                        ),
                        group(null, Accumulators.sum("totalSongs", "$numSongs"))
                )
        ).first();

        return songsDoc.getInteger("totalSongs");
    }

    /**
     * Tells you how many seconds it would take to listen to every single song in the database.
     * @return int representing the total song duration.
     */
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

    /**
     * Gets song documents for playlist creation based on given criteria.
     * @param artists A Set of artist names
     * @param genres A Set of genres
     * @param allowExplicit whether explicit albums should be allowed on the album
     * @param startYear The earliest year songs may be added from
     * @param endYear The latest year songs may be added from
     * @return A List of song documents that match the criteria.
     */
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

        aggregatePipeline.addAll(
                Arrays.asList(
                        unwind("$albums"),
                        match(and( gte("albums.year", startYear), lte("albums.year", endYear))),
                        unwind("$albums.songs")
                )
        );

        if (!allowExplicit){
            aggregatePipeline.add(match(eq("albums.songs.explicit", allowExplicit)));
        }

        //The driver won't let me make the _id field a document when using Aggregates.group
        //Have to make an ugly document like this to do what I want
        Document groupDoc = new Document("$group",
                new Document ("_id", new Document("uri", "$albums.songs.uri")
                        .append("title", "$albums.songs.title")
                        .append("image", "$albums.image")
                        .append("artist", "$name")
                        .append("duration", "$albums.songs.duration")) );

        aggregatePipeline.addAll(
                Arrays.asList(
                        groupDoc,
                        replaceRoot("$_id")
                )
        );

        return collection.aggregate(aggregatePipeline).into( new ArrayList<>());
    }

    /**
     * Creates a playlist from a passed list of song documents and a desired playlist length.
     * @param potentialSongs A List of songs from {@link #getSongsByCriteria(Set, Set, boolean, int, int)}
     * @param playlistDuration A desired playlist length in seconds
     * @return A list of song documents containing the final playlist
     */
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
