package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

import static db.updates.spotify.SpotifyHelpers.*;
import static db.updates.spotify.SpotifyHelpers.addSongs;
import static db.updates.spotify.SpotifyHelpers.getArtistNames;


public class AddArtistAndSongs {

    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");
        MongoCollection<Document> artistsCol = db.getCollection("artists");

        String path = "src/main/resources/artistNames.txt";
        List<String> artists = getArtistNames(path);

        for(String artist: artists) {
            addSongs(songs, artist);
            addArtist(artistsCol, artist);
        }
    }

}
