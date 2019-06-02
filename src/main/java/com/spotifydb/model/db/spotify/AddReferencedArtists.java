package com.spotifydb.model.db.spotify;

import com.spotifydb.model.db.implementations.DatabaseConnection;
import com.spotifydb.model.db.implementations.mongo.MongoConnection;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;

import java.util.Set;



public class AddReferencedArtists {
    public static void main(String[] args) {

        DatabaseConnection db = new MongoConnection();
        SpotifyConnection sc = new SpotifyConnection();

        Set<String> allFeaturedIds = db.getAllFeaturedArtists();
        Set<String> allArtistIds = db.getAllArtistIds();
        allFeaturedIds.removeAll(allArtistIds);
        System.out.println("Attempting to insert " + allFeaturedIds.size() + " artists to the database.");
        long numDocsAtStart = db.getNumArtists();
        long startTime = System.currentTimeMillis();
        for(String id: allFeaturedIds) {
            Artist artist = sc.getArtistById(id);
            Album[] albums = sc.getAlbumsByArtist(artist);
            db.insertArtist(artist, albums);
        }
        long endTime = System.currentTimeMillis();
        long numDocsAtEnd = db.getNumArtists();
        System.out.println("Took " + (endTime - startTime)/1000 + " seconds to add " + (numDocsAtEnd-numDocsAtStart) + " artists.");

    }
}
