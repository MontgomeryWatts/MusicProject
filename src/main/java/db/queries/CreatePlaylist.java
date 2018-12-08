package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import db.updates.spotify.SpotifyQueries;
import org.bson.Document;

import java.io.BufferedReader;
import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.in;

@SuppressWarnings("unchecked")
public class CreatePlaylist {

    private static final int ONE_HOUR = 3600;
    private static final int DEFAULT_DURATION = (int)(ONE_HOUR * 1.5);

    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> usersCollection = db.getCollection("userinfo");
        MongoCollection<Document> artistCollection = db.getCollection("artists");

        String username = "loco__motives";
        String playlistName;
        Set<String> artists = new HashSet<>();
        Set<String> genres = new HashSet<>();
        Scanner scanner = new Scanner(System.in);
        boolean condition = true;
        String input;
        do {
            System.out.println("1 - Add artist\n2 - Add genre\n3 - See criteria\n4 - Generate playlist\n5 - Quit");
            input = scanner.nextLine();

            switch (input){
                case "1":
                    System.out.println("Enter the name of an artist to add to the playlist");
                    input = scanner.nextLine();
                    artists.add(input);
                    break;
                case "2":
                    System.out.println("Enter the name of a genre to add to the playlist");
                    input = scanner.nextLine();
                    genres.add(input);
                    break;
                case "3":
                    System.out.println("all artists");
                    for (String s: artists)
                        System.out.println(s);
                    System.out.println("all genres");
                    for (String s: genres)
                        System.out.println(s);
                    break;
                case "4":
                    playlistName = String.valueOf(System.currentTimeMillis());
                    Set<String> uriSet = getTrackUris(artistCollection,artists, genres, DEFAULT_DURATION, true, 1900, 2020);
                    String[] uris = uriSet.toArray(new String[0]);

                    String playlistId = SpotifyQueries.createSpotifyPlaylist(usersCollection, username, playlistName);
                    SpotifyQueries.addTracksToPlaylist(usersCollection, username, playlistId, uris);
                    break;
                case "5":
                    condition = false;
                    break;
                default:
                        break;
            }
        } while(condition);
    }

    private static Set<String> getTrackUris(MongoCollection<Document> artistCollection, Set<String> artists, Set<String> genres, int duration,
                                           boolean onlyExplicit, int startYear, int endYear){

        List<Document> songsList = artistCollection.aggregate(
                Arrays.asList(
                        match( or( in("genres", genres), in("_id.name", artists) )),
                        unwind("$albums"),
                        match( and( eq("albums.is_explicit", onlyExplicit),
                                and( gte("albums.year", startYear), lte("albums.year", endYear)))) ,
                        unwind("$albums.songs"),
                        project(Projections.include("albums.songs"))
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
