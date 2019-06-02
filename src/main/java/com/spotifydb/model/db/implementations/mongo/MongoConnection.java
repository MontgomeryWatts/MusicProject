package com.spotifydb.model.db.implementations.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.spotifydb.model.Preview;
import com.spotifydb.model.PreviewPage;
import com.spotifydb.model.db.implementations.DatabaseConnection;
import com.spotifydb.model.db.spotify.AddReferencedArtists;
import com.wrapper.spotify.model_objects.specification.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.spotifydb.ui.controllers.ArtistController;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

@Service
@Primary
public class MongoConnection extends DatabaseConnection {
    private MongoCollection<Document> collection;

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
        String artistId = doc.getString("artistId");
        String text = doc.getString("name");

        return new Preview(Preview.Type.ARTIST,  artistId, artistId, imageUrl, text);
    }

    private static Preview createPreviewFromAlbumDoc(Document doc){

        Document embeddedAlbumDoc = (Document) doc.get("album");

        String albumID = embeddedAlbumDoc.getString("albumId");
        String text = embeddedAlbumDoc.getString("title");
        String imageUrl = embeddedAlbumDoc.getString("image");
        if (imageUrl == null){
            imageUrl = BLANK_ALBUM;
        }

        return new Preview(Preview.Type.ALBUM, albumID, albumID, imageUrl, text);
    }


    /**
     * The collection attribute has its documents designed around artists. That is,
     * each document represents one artist in the database.
     * @return The number of artists stored in the database.
     */
    @Override
    public int getNumArtists(){
        Document aggregate = collection.aggregate(Arrays.asList(
                group("$artistId", new ArrayList<>()), //There are no accumulators
                count("totalArtists")
        )).first();

        return aggregate != null ? aggregate.getInteger("totalArtists") : 0;
}

    /**
     * Retrieves the URI of a random artist. This is used for a redirect in {@link ArtistController#getRandom()}
     * @return String representing the URI of a random artist.
     */

    @Override
    public String getRandomArtistId(){
        Document randomArtistDoc = collection.aggregate(Arrays.asList(
                sample(1),
                project(include("artistId"))
        )).first();
        return randomArtistDoc.getString("artistId");
    }

    /**
     * Gets a set of the URIs of all artists that have featured on any song ever (in the database). Currently used by
     * {@link AddReferencedArtists} in order to 'organically' grow the database.
     * @return A Set of the URIs of all artists featured
     */
    @Override
    public Set<String> getAllFeaturedArtists(){
        return collection.distinct("album.songs.featured", String.class).into(new HashSet<>());
    }

    /**
     * Gets a set of the URIs of all artists that are currently in the database. Currently used by {@link AddReferencedArtists}
     * in conjunction with {@link #getAllFeaturedArtists()} in order to only add artists not in the database already.
     * @return A Set of the URIs of all artists
     */

    @Override
    public Set<String> getAllArtistIds(){
        return collection.distinct("artistId", String.class).into(new HashSet<>());
    }


    /**
     * Returns a list of up to {@value #RESULTS_PER_PAGE} artist Documents selected by random.
     * @return A List of random artist Previews
     */
    @Override
    public PreviewPage getArtistsByRandom(){
        PreviewPage page = new PreviewPage();
        List<Preview> previews = collection.aggregate(Arrays.asList(
                sample(RESULTS_PER_PAGE),
                project(include("images", "name", "artistId"))
        )).map( MongoConnection::createPreviewFromArtistDoc ).into(new ArrayList<>());

        page.setPreviews(previews);
        return page;
    }

    /**
     * Retrieves an artist's Document or null if no document exists. Used to display an artist page
     * @param artistId The id of the artist to retrieve
     * @return The matching document, or null
     */

    @Override
    public Document getArtistById(String artistId) {
        return collection.aggregate(Arrays.asList(
                match(eq("artistId", artistId)),
                sort(descending("album.release_date")),
                group("$artistId",
                        first("name", "$name"),
                        first("genres", "$genres"),
                        first("images", "$images"),
                        push("albums", "$album"))
        )).first();
    }

    @Override
    public Document getAlbumPage(String albumID){
        return collection.aggregate(Arrays.asList(
           match(eq("album.albumId", albumID)),
           group(null,
                   first("album", "$album"),
                   push("credits", new Document("name", "$name").append("artistId", "$artistId"))
        ))).first();
    }

    private Bson createArtistMatchDoc(String genre, String name){
        if (name != null  && name.length() > 0){
            Pattern namePattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
            Bson nameMatchDoc = regex("name", namePattern);
            if (genre != null && genre.length() > 0){  //Name not null and genre not null
                Bson genreMatchDoc = eq("genres", genre);
                return match( and( genreMatchDoc, nameMatchDoc));
            } else { //Name not null and genre null
                return match(nameMatchDoc);
            }
        } else {
            if (genre != null && genre.length() > 0) { //Name is null, genre is not null
                Bson genreMatchDoc = eq("genres", genre);
                return match(genreMatchDoc);
            }
        }
        return null;
    }

    private Bson createAlbumMatchDoc(String name, Integer year){
        if (name != null  && name.length() > 0){
            Pattern namePattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
            Bson nameMatchDoc = regex("album.title", namePattern);
            if (year != null){  //Name not null and genre not null
                Bson yearMatchDoc = eq("album.year", year);
                return match( and( yearMatchDoc, nameMatchDoc));
            } else { //Name not null and genre null
                return match(nameMatchDoc);
            }
        } else {
            if (year != null) { //Name is null, genre is not null
                Bson yearMatchDoc = eq("album.year", year);
                return match(yearMatchDoc);
            }
        }
        return null;
    }

    @Override
    public PreviewPage getAlbums(String name, Integer year, int offset, int limit){
        List<Bson> aggregationStages = new ArrayList<>();

        Bson albumMatcher = createAlbumMatchDoc(name, year);
        if (albumMatcher != null) {
            aggregationStages.add(albumMatcher);
        }

        aggregationStages.addAll(
                Arrays.asList(
                        group("$album.albumId", first("album", "$album")),
                        facet(new Facet("total", count()),
                                new Facet("paging", skip(offset), limit(limit)))
                )
        );

        PreviewPage page = new PreviewPage();

        Document result = collection.aggregate(aggregationStages).first();

        int totalResults = 0;
        List<Document> totalFacet = (ArrayList<Document>) result.get("total");

        if (totalFacet.size()> 0 ){
            Document countDoc = totalFacet.get(0);
            totalResults = countDoc.getInteger("count");
        }

        List<Document> matchedDocs = (ArrayList<Document>) result.get("paging");
        List<Preview> previews = matchedDocs.stream()
                .map(MongoConnection::createPreviewFromAlbumDoc)
                .collect(Collectors.toList());

        page.setPreviews(previews);

        if (totalResults > matchedDocs.size() + offset){
            page.setNext(true);
        }


        return page;
    }

    @Override
    public PreviewPage getArtists(String genre, String name, int offset, int limit) {
        List<Bson> aggregationStages = new ArrayList<>();

        Bson artistMatcher = createArtistMatchDoc(genre, name);
        if (artistMatcher != null) {
            aggregationStages.add(artistMatcher);
        }

        aggregationStages.addAll(
                Arrays.asList(
                        group("$artistId",
                                first("images", "$images"),
                                first("name", "$name"),
                                first("artistId", "$artistId")),
                        facet(new Facet("total", count()),
                            new Facet("paging", skip(offset), limit(limit)))
                )
        );

        PreviewPage page = new PreviewPage();

        Document result = collection.aggregate(aggregationStages).first();

        int totalResults = 0;
        List<Document> totalFacet = (ArrayList<Document>) result.get("total");

        if (totalFacet.size()> 0 ){
            Document countDoc = totalFacet.get(0);
            totalResults = countDoc.getInteger("count");
        }

        List<Document> matchedDocs = (ArrayList<Document>) result.get("paging");
        List<Preview> previews = matchedDocs.stream()
                .map(MongoConnection::createPreviewFromArtistDoc)
                .collect(Collectors.toList());

        page.setPreviews(previews);

        if (totalResults > matchedDocs.size() + offset){
            page.setNext(true);
        }


        return page;
    }

    @Override
    public Iterable<String> getSimilarArtistNames(String name, int offset, int limit) {
        name = name.replace('+', ' '); // jQuery POSTs replace spaces with +'s
        Pattern namePattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        return  collection.aggregate(
                Arrays.asList(
                        group("$artistId", first("name", "$name")),  //Look into making this faster
                        match(Filters.regex("name", namePattern)),
                        project(include("name")),
                        skip(offset),
                        limit(limit)
                )).map((Document document) -> document.getString("name"));
    }

    /**
     * Attempt to insert an artist document into the collection.
     * @return True if the document was inserted, else false.
     */

    @Override
    public void insertArtist(Artist artist, Album[] albums) {
        if(albums.length == 0) //Only add artists with music, only implemented because storage costs money!
            return;

        List<Document> artistDocs = new ArrayList<>();
        for(Album album: albums){
             artistDocs.add(createArtistDoc(artist, album));
        }

        try {
            collection.insertMany(artistDocs);
        } catch (MongoWriteException mwe) {
            mwe.printStackTrace();
        }
    }

    private Document createArtistDoc(Artist artist, Album album){
        String id = artist.getId();
        List<String> genres = Arrays.asList(artist.getGenres());
        List<String> imageUrls = new ArrayList<>();
        for(Image image: artist.getImages())
            imageUrls.add(image.getUrl());

        Document artistDoc = new Document("artistId", id)
                .append("name", artist.getName())
                .append("images", imageUrls);

        if(genres.size() != 0){
            artistDoc.append("genres", genres);
        }

        artistDoc.append("album", createAlbumDocument(id, album));

        return artistDoc;
    }

    private Date parseAlbumDate(Album album){
        String releaseDate = album.getReleaseDate();
        SimpleDateFormat dateFormat;
        try{
            switch (album.getReleaseDatePrecision()){
                case DAY:
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    break;
                case MONTH:
                    dateFormat = new SimpleDateFormat("yyyy-MM");
                    break;
                case YEAR:
                default:
                    dateFormat = new SimpleDateFormat("yyyy");
                    break;
            }
            return dateFormat.parse(releaseDate);
        } catch (ParseException pe) {
            //should never happen
            return new Date();
        }
    }

    private Document createAlbumDocument(String artistId, Album album){
        Document albumDoc = new Document("title", album.getName())
                .append("albumId", album.getId())
                .append("release_date", parseAlbumDate(album));

        Image[] albumImages = album.getImages();
        if(albumImages.length != 0) {
            albumDoc.append("image", albumImages[0].getUrl()); //The first image in the images array is the largest
        }

        List<Document> songDocuments = createSongDocuments(artistId, album);
        albumDoc.append("songs", songDocuments);

        return albumDoc;
    }


    private List<Document> createSongDocuments(String artistId, Album album){
        List<Document> songDocuments = new ArrayList<>();

        TrackSimplified[] tracks = album.getTracks().getItems();
        for(TrackSimplified track: tracks){
            Document songDoc = new Document("trackId", track.getId())
                    .append("title", track.getName())
                    .append("duration", track.getDurationMs() / 1000)
                    .append("explicit", track.getIsExplicit());

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
                sort(ascending("_id")),
                group(0, push("genres", "$_id"))
        )).first();


        return (genresDoc != null) ? (ArrayList<String>) genresDoc.get("genres") : new ArrayList<>();
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
                        project(computed("numSongs",
                                new Document("$size", "$album.songs"))
                        ),
                        group(null, sum("totalSongs", "$numSongs"))
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
                        unwind("$album.songs"),
                        project(include("album.songs.duration")),
                        group(null, sum("duration", "$album.songs.duration"))
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
        } else{
            aggregatePipeline.add(sample(RESULTS_PER_PAGE * 10)); //No artist or genre criteria, sample random artists
        }

        //TODO re-add ability to filter songs by release date
        //important to remember that an int is no longer used to represent this info
        //match(and( gte("albums.year", startYear), lte("albums.year", endYear)))
        aggregatePipeline.add(unwind("$album.songs"));


        if (!allowExplicit){
            aggregatePipeline.add(match(eq("album.songs.explicit", allowExplicit)));
        }

        aggregatePipeline.add(
                group("$album.songs.trackId",
                    first("duration", "$album.songs.duration"),
                    first("title", "$album.songs.title"),
                    first("artist", "$name"),
                    first("image", "$album.image")
                ));

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
