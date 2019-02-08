package db.spotify;

import db.implementations.DatabaseConnection;
import db.implementations.MongoConnection;

import java.util.Set;

import static db.spotify.SpotifyQueries.*;


@SuppressWarnings("unchecked")
public class AddReferencedArtists {
    public static void main(String[] args) {

        DatabaseConnection db = new MongoConnection();

        Set<String> allFeatured = db.getAllFeaturedArtists();
        Set<String> inDatabase = db.getAllArtistUris();
        allFeatured.removeAll(inDatabase);
        System.out.println("Attempting to insert " + allFeatured.size() + " artists to the database.");
        long numDocsAtStart = db.getNumberOfArtists();
        long startTime = System.currentTimeMillis();
        for(String id: allFeatured) {
            db.insertArtist(getArtistDocById(id));
        }
        long endTime = System.currentTimeMillis();
        long numDocsAtEnd = db.getNumberOfArtists();
        System.out.println("Took " + (endTime - startTime)/1000 + " seconds to add " + (numDocsAtEnd-numDocsAtStart) + " artists.");
    }
}
