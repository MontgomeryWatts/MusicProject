package db.updates.spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;


public class AddArtistAndSongs {

    public static List<String> getArtistNames(String path){
        List<String> artists = new ArrayList<String>();

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

    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");
        MongoCollection<Document> artistsCol = db.getCollection("artists");

        String path = "src/main/resources/artistNames.txt";
        List<String> artists = getArtistNames(path);

        for(String artist: artists) {
            AddSongs.addAllSongs(artist, songs);
            AddArtists.addAllArtists(artist, artistsCol);
        }
    }

}
