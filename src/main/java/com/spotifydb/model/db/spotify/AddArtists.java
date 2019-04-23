package com.spotifydb.model.db.spotify;

import com.spotifydb.model.db.implementations.DatabaseConnection;
import com.spotifydb.model.db.implementations.mongo.MongoConnection;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AddArtists {
    public static void main(String[] args) {
        DatabaseConnection db = new MongoConnection();
        SpotifyConnection sc = new SpotifyConnection();
        System.out.println(db.getNumArtists());

        for(String name: getArtistNames("src/main/resources/artistNames.txt")){
            Paging<Artist> artists = sc.getArtistsByName(name);
            for(Artist artist: artists.getItems() ){
                Album[] albums = sc.getAlbumsByArtist(artist);
                db.insertArtist(artist, albums);
            }

        }

        System.out.println(db.getNumArtists());
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
