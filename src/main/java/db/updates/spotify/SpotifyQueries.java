package db.updates.spotify;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import org.bson.Document;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.*;

public class SpotifyQueries {

    /**
     * Attempts to create and add an artist Document to a given collection.
     * @param artistCollection The MongoCollection to add the Document to
     * @param artistName The name of the artist to look up
     */

    static void addArtist(MongoCollection<Document> artistCollection, String artistName) {
        //Don't bother doing anything if the document already exists
        Document artistDoc = artistCollection.find( eq("_id", artistName) ).first();
        if(artistDoc != null)
            return;

        SpotifyApi spotifyApi = createSpotifyAPI();
        String id = getArtistID(spotifyApi, artistName);

        if(id == null)
            return;

        String spotifyUri = "spotify:artist:" + id;
        List<String> genres = getArtistGenres(spotifyApi, artistName);

        Document doc = new Document("_id", artistName)
                .append("genres", genres)
                .append("spotify", spotifyUri);

        try{
            artistCollection.insertOne(doc);
        } catch (MongoWriteException mwe){
            mwe.printStackTrace();
        }
    }

    /**
     * Attempts to create and add song Documents to a given collection.
     * @param songsCollection The MongoCollection to add the Documents to
     * @param artistName The name of the artist whose songs are being looked up
     */

    static void addSongs(MongoCollection<Document> songsCollection, String artistName) {

        SpotifyApi spotifyApi = createSpotifyAPI();
        String artistID = getArtistID(spotifyApi, artistName);
        if(artistID == null)
            return;

        Paging<AlbumSimplified> albums = getAlbums(spotifyApi, artistID);

        String id;
        int duration;
        String title;
        Document doc;
        List<String> featured;

        for (AlbumSimplified album : albums.getItems()) {

            if(album == null)
                continue;

            for (TrackSimplified track : getTracks(spotifyApi, album.getId()).getItems()) {
                title = track.getName();
                id = track.getUri();
                duration = track.getDurationMs() / 1000;
                featured = getFeatured(artistName, track);

                Document songDoc = songsCollection.find( eq("_id", id) ).first();

                //This check should prevent any potential MongoWriteExceptions
                if(songDoc == null) {
                    try {

                        doc = new Document("_id", id)
                                .append("artist", artistName)
                                .append("album", album.getName())
                                .append("title", title)
                                .append("duration", duration);

                        if (featured.size() != 0)
                            doc.append("featured", featured);

                        songsCollection.insertOne(doc);

                    } catch (MongoWriteException mwe) {
                        mwe.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Creates a SpotifyApi Object with my given client info that can be used to generate requests.
     * @return A SpotifyApi Object that can make requests
     */

    private static SpotifyApi createSpotifyAPI(){

        return createSpotifyAPI(null);
    }

    public static SpotifyApi createSpotifyAPI(URI uri){

        SpotifyApi spotifyApi = null;

        try {
            File file = new File("src/main/resources/clientInfo.txt");
            Scanner scanner = new Scanner(file);
            SpotifyApi.Builder builder = SpotifyApi.builder()
                    .setClientId(scanner.nextLine())
                    .setClientSecret(scanner.nextLine());
            if(uri != null)
                    builder.setRedirectUri(uri);

            spotifyApi = builder.build();
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        final ClientCredentialsRequest request = spotifyApi.clientCredentials().build();

        try{
            final ClientCredentials clientCredentials = request.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return spotifyApi;
    }

    /**
     * Returns a Paging Object containing all of the artists albums
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistID The ID of the artist in Spotify's URI
     * @return A Paging Object containing all of the artists albums
     */

    private static Paging<AlbumSimplified> getAlbums(SpotifyApi spotifyApi, String artistID){
        Paging<AlbumSimplified> albums = new Paging.Builder<AlbumSimplified>().build();

        final GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistID)
                .market(CountryCode.US)
                .album_type("album")
                .build();
        try {
            albums = albumsRequest.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getAlbums, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getAlbums(spotifyApi, artistID);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getAlbums(spotifyApi, artistID);
        } catch (Exception e){
            e.printStackTrace();
        }

        return albums;
    }

    /**
     * Retrieves a List of all of an artist's musical genres
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistName The name of the artist whose genres are being retrieved
     * @return A List containing the artist's genres
     */

    private static List<String> getArtistGenres(SpotifyApi spotifyApi, String artistName){
        List<String> genres = new ArrayList<>();

        final SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .limit(1)
                .build();

        try{
            final Paging<Artist> artistPaging = artReq.execute();
            for( Artist a: artistPaging.getItems()) {
                genres = Arrays.asList(a.getGenres());
            }
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistGenres, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistGenres(spotifyApi, artistName);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getArtistGenres(spotifyApi, artistName);
        } catch (Exception e){
            e.printStackTrace();
        }

        return genres;
    }

    /**
     * Returns the ID for the artist used in Spotify's URI
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistName The name of the artist whose ID is being retrieved
     * @return String representing the artist's ID
     */

    private static String getArtistID(SpotifyApi spotifyApi, String artistName){
        String id = null;

        final SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .build();

        try{
            final Paging<Artist> artistPaging = artReq.execute();
            for( Artist artist: artistPaging.getItems()) {
                if(artistName.toLowerCase().equals(artist.getName().toLowerCase())) {
                    id = artist.getId();
                }
            }
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistID, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistID(spotifyApi, artistName);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getArtistID(spotifyApi, artistName);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }


    /**
     * Gets a list of any other artists featured on a track.
     * @param artistName The name of the artist whose album the track is on
     * @param track The track
     * @return A List containing any featured artists
     */

    private static List<String> getFeatured(String artistName, TrackSimplified track){
        List<String> featured = new ArrayList<>();
        for(ArtistSimplified a: track.getArtists()){
            if ( !a.getName().equals(artistName) )
                featured.add(a.getName());
        }
        return featured;
    }

    /**
     * Gets a Paging of all the tracks on a given album
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param albumID The ID of the album in Spotify's URI
     * @return A Paging containing all tracks on the album
     */

    private static Paging<TrackSimplified> getTracks(SpotifyApi spotifyApi, String albumID){
        Paging<TrackSimplified> tracks = new Paging.Builder<TrackSimplified>().build();

        final GetAlbumsTracksRequest txRequest = spotifyApi.getAlbumsTracks(albumID).build();

        try{
            tracks = txRequest.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getTracks, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getTracks(spotifyApi, albumID);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getTracks(spotifyApi, albumID);
        }  catch (Exception e){
            e.printStackTrace();
        }

        return tracks;
    }

}
