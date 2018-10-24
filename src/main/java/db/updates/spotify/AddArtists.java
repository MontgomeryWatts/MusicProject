package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static db.updates.spotify.SpotifyQueries.*;


public class AddArtists {

    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistsCollection = db.getCollection("artists");

        String path = "src/main/resources/artistNames.txt";
        List<String> artists = getArtistNames(path);

        for(String artist: artists) {
            addArtistByName(artistsCollection, artist);
        }
    }
    /**
     * Reads in from a file a list of artists to add to the database
     * @param path The path to the file containing the artists' names
     * @return A List containing the artists names
     */

    private static List<String> getArtistNames(String path){
        List<String> artists = new ArrayList<>();

        try {
            File file = new File(path);
            Scanner read = new Scanner(file);
            while (read.hasNextLine())
                artists.add(read.nextLine());
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return artists;
    }

}
