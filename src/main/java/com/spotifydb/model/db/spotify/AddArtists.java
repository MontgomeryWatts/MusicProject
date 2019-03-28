package com.spotifydb.model.db.spotify;

import com.spotifydb.model.db.implementations.MongoConnection;
import org.bson.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AddArtists {

    public static void main(String[] args) {
        MongoConnection db = new MongoConnection();
        System.out.println(db.getNumberOfArtists());

        List<Document> artistDocs = new ArrayList<>();
        for(String name: getArtistNames("src/main/resources/artistNames.txt"))
            artistDocs.addAll(SpotifyQueries.getArtistDocsByName(name));

        for(Document artist: artistDocs)
            db.insertArtist(artist);

        System.out.println(db.getNumberOfArtists());
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
