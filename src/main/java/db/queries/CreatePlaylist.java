package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.updates.spotify.SpotifyQueries;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.in;

@SuppressWarnings("unchecked")
public class CreatePlaylist {

    private static final int ONE_HOUR = 3600;
    private static final int DEFAULT_DURATION = ONE_HOUR * 3;

    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> usersCollection = db.getCollection("userinfo");
        MongoCollection<Document> artistCollection = db.getCollection("artists");

        String username = "loco__motives";
        String playlistName = "New Structure Test 4";
        Set<String> artists = new HashSet<>();
        artists.add("Isaiah Rashad");
        artists.add("Jaden Smith");
        Set<String> genres = new HashSet<>();
        genres.add("hip hop");
        genres.add("indie r&b");
        Set<String> uriSet = getTrackUris(artistCollection,artists, genres, DEFAULT_DURATION, true);
        String[] uris = uriSet.toArray(new String[0]);

        String playlistId = SpotifyQueries.createSpotifyPlaylist(usersCollection, username, playlistName);
        SpotifyQueries.addTracksToPlaylist(usersCollection, username, playlistId, uris);
    }

    private static Set<String> getTrackUris(MongoCollection<Document> artistCollection, Set<String> artists, Set<String> genres, int duration,
                                           boolean onlyExplicit){

        List<Document> songsList = artistCollection.aggregate(
                Arrays.asList(
                        match( or( in("genres", genres), in("_id.name", artists) )),
                        unwind("$albums"),
                        match( eq("albums.is_explicit", onlyExplicit)),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs")
                )
        ).into( new ArrayList<>());

        Set<String> playlistURIs = new HashSet<>();
        while ( duration > 180 && !songsList.isEmpty()){
            Document song = songsList.remove(Math.abs(new Random().nextInt()) % songsList.size());
            int songDuration = song.getInteger("duration");
            if (songDuration <= duration) {
                playlistURIs.add(song.getString("uri"));
                System.out.println(song.getString("title"));
                duration -= songDuration;
            }
        }

        return playlistURIs;
    }
}
