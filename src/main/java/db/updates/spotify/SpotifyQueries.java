package db.updates.spotify;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.ForbiddenException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import db.queries.DatabaseQueries;
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
     * Helper function that calls addArtist and addSongs sequentially. Ensuring they are called in this order makes it
     * so that in addSongs a internal database query is made that may spare an external query being sent to Spotify.
     * @param songsCollection MongoCollection containing song Documents
     * @param artistsCollection MongoCollection containing artist Documents
     * @param artistName The name of the artist to attempt to add
     */
    public static void addArtistAndSongs(MongoCollection<Document> songsCollection, MongoCollection<Document> artistsCollection, String artistName){
        addArtist(artistsCollection, artistName);
        addSongs(songsCollection, artistsCollection, artistName);
    }

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
        String spotifyId = getArtistID(spotifyApi, artistName);

        if(spotifyId == null)
            return;

        List<String> genres = getArtistGenres(spotifyApi, spotifyId);

        Document doc = new Document("_id", artistName)
                .append("genres", genres)
                .append("spotify_id", spotifyId);

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

    static void addSongs(MongoCollection<Document> songsCollection, MongoCollection<Document> artistsCollection, String artistName) {

        SpotifyApi spotifyApi = createSpotifyAPI();

        Document artistDoc = DatabaseQueries.getArtistDoc(artistsCollection, artistName);
        String artistID = (artistDoc != null) ? artistDoc.getString("spotify_id")
                : getArtistID(spotifyApi, artistName);
        if(artistID == null)
            return;

        Paging<AlbumSimplified> albums = getAlbums(spotifyApi, artistID);
        if(albums != null ) {
            String id;
            int duration;
            String title;
            Document doc;
            List<String> featured;

            for (AlbumSimplified album : albums.getItems()) {

                for (TrackSimplified track : getTracks(spotifyApi, album.getId()).getItems()) {
                    title = track.getName();
                    id = track.getUri();
                    duration = track.getDurationMs() / 1000;
                    featured = getFeatured(artistName, track);

                    Document songDoc = songsCollection.find(eq("_id", id)).first();

                    //This check should prevent any potential MongoWriteExceptions
                    if (songDoc == null) {
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

        ClientCredentialsRequest request = spotifyApi.clientCredentials().build();

        try{
            ClientCredentials clientCredentials = request.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return spotifyApi;
    }

    /**
     * Attempts to create an empty Spotify playlist, and returns the spotify id of the playlist so that tracks may be
     * added to it.
     * @param usersCollection The MongoCollection containing Documents with user access/refresh tokens
     * @param username The username of the person to create a playlist for
     * @param playlistName The name of the playlist to create
     * @return a String containing the Spotify Id of the playlist
     */

    public static String createPlaylist(MongoCollection<Document> usersCollection, String username, String playlistName){
        SpotifyApi spotifyApi = createSpotifyAPI();

        Document userDoc = usersCollection.find( eq("_id", username) ).first();
        if(userDoc == null) {
            return null;
        }

        spotifyApi.setAccessToken( userDoc.getString("access_token") );
        spotifyApi.setRefreshToken( userDoc.getString("refresh_token") );

        CreatePlaylistRequest request = spotifyApi.createPlaylist(username, playlistName)
                .collaborative(false)
                .public_(true)
                .name(playlistName)
                .build();

        try {
            final Playlist playlist = request.execute();
            return playlist.getId();

        } catch (ForbiddenException fe) {
            //Stuff to refresh access/refresh tokens
            refreshTokens(usersCollection, username);
            return createPlaylist(usersCollection, username, playlistName);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addTracksToPlaylist(MongoCollection<Document> usersCollection, String username, String playlistId, String[] uris){
        if(playlistId == null) {
            return false;
        }

        SpotifyApi spotifyApi = createSpotifyAPI();
        Document userDoc = usersCollection.find( eq("_id", username) ).first();
        if(userDoc == null) {
            return false;
        }

        spotifyApi.setAccessToken( userDoc.getString("access_token") );
        spotifyApi.setRefreshToken( userDoc.getString("refresh_token") );

        AddTracksToPlaylistRequest addTracksToPlaylistRequest = spotifyApi
                .addTracksToPlaylist(username, playlistId, uris)
                .build();

        try {
            addTracksToPlaylistRequest.execute();
            return true;
        } catch (ForbiddenException fe) {
            //Stuff to refresh access/refresh tokens
            refreshTokens(usersCollection, username);
            return addTracksToPlaylist(usersCollection, username, playlistId, uris);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void refreshTokens(MongoCollection<Document> usersCollection, String username){
        Document userDoc = usersCollection.find( eq("_id", username) ).first();
        if(userDoc == null)
            return;

        String refreshToken = userDoc.getString("refresh_token");
        SpotifyApi spotifyApi = createSpotifyAPI();
        spotifyApi.setRefreshToken(refreshToken);
        AuthorizationCodeRefreshRequest refreshRequest  = spotifyApi.authorizationCodeRefresh().build();
        try {
            AuthorizationCodeCredentials credentials = refreshRequest.execute();
            refreshToken = credentials.getRefreshToken();
            String accessToken = credentials.getAccessToken();
            spotifyApi.setRefreshToken(refreshToken);
            spotifyApi.setAccessToken(accessToken);
            userDoc.put("refresh_token", refreshToken);
            userDoc.put("access_token", accessToken);
            usersCollection.replaceOne( eq("_id", username), userDoc);
        } catch(TooManyRequestsException tmre){
            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in refreshTokens, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            refreshTokens(usersCollection, username);
        } catch (Exception e){
            e.printStackTrace();
        }
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
     * @param spotifyId The Id of the artist whose genres are being retrieved
     * @return A List containing the artist's genres
     */

    private static List<String> getArtistGenres(SpotifyApi spotifyApi, String spotifyId){
        List<String> genres = new ArrayList<>();

        GetArtistRequest artReq = spotifyApi.getArtist(spotifyId)
                .build();

        try{
            Artist artist = artReq.execute();
            genres = Arrays.asList(artist.getGenres());
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistGenres, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistGenres(spotifyApi, spotifyId);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getArtistGenres(spotifyApi, spotifyId);
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

        SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .build();

        try{
            Paging<Artist> artistPaging = artReq.execute();
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

        GetAlbumsTracksRequest txRequest = spotifyApi.getAlbumsTracks(albumID).build();

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
