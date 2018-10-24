package db.updates.spotify;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.ForbiddenException;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
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
import org.bson.Document;

import java.io.File;
import java.net.URI;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class SpotifyQueries {

    static void addArtistById(MongoCollection<Document> artistCollection, String artistId) {
        //Don't bother doing anything if the document already exists
        Document artistDoc = artistCollection.find( eq("_id.uri", artistId) ).first();
        if(artistDoc != null)
            return;

        SpotifyApi spotifyApi = createSpotifyAPI();
        String artistName = getArtistName(spotifyApi, artistId);

        insertArtist(spotifyApi, artistCollection, artistName, artistId);
    }

    /**
     * Attempts to create and add an artist Document to a given collection.
     * @param artistCollection The MongoCollection to add the Document to
     * @param artistName The name of the artist to attempt to add
     */

    static void addArtistByName(MongoCollection<Document> artistCollection, String artistName) {
        //Don't bother doing anything if the document already exists
        Document artistDoc = artistCollection.find( eq("_id.name", artistName) ).first();
        if(artistDoc != null)
            return;

        SpotifyApi spotifyApi = createSpotifyAPI();
        Set<String> spotifyIds = getArtistIds(spotifyApi, artistName);

        if(spotifyIds.isEmpty())
            return;

        for( String spotifyId: spotifyIds){
            insertArtist(spotifyApi, artistCollection, artistName, spotifyId);
        }

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
        } catch (UnauthorizedException | ForbiddenException fe) {
            //Stuff to refresh access/refresh tokens
            refreshTokens(usersCollection, username);
            return addTracksToPlaylist(usersCollection, username, playlistId, uris);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public static String createSpotifyPlaylist(MongoCollection<Document> usersCollection, String username, String playlistName){
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

        } catch (UnauthorizedException | ForbiddenException fe) {
            //Stuff to refresh access/refresh tokens
            refreshTokens(usersCollection, username);
            return createSpotifyPlaylist(usersCollection, username, playlistName);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
                .album_type("album,single")
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
     * Returns the Spotify IDs for the artist provided. This may return multiple ids in the case of artists with shared
     * names.
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistName The name of the artist whose ID is being retrieved
     * @return String representing the artist's ID
     */

    private static Set<String> getArtistIds(SpotifyApi spotifyApi, String artistName){

        Set<String> ids = new HashSet<>();

        SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .build();

        try{
            Paging<Artist> artistPaging = artReq.execute();
            for( Artist artist: artistPaging.getItems()) {
                if(artistName.toLowerCase().equals(artist.getName().toLowerCase())) {
                    ids.add(artist.getId());
                }
            }
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistIds, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistIds(spotifyApi, artistName);
        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return getArtistIds(spotifyApi, artistName);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ids;
    }

    private static String getArtistName(SpotifyApi spotifyApi, String artistID){
        GetArtistRequest request = spotifyApi.getArtist(artistID).build();

        try{
            Artist artist = request.execute();
            return artist.getName();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistName, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return  getArtistName(spotifyApi, artistID);
        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return  getArtistName(spotifyApi, artistID);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Gets a set of any other artists featured on a track.
     * @param artistId The id of the artist whose album the track is on
     * @param track The track
     * @return A Set containing any featured artists
     */

    private static Set<String> getFeatured(String artistId, TrackSimplified track){
        Set<String> featured = new HashSet<>();
        for(ArtistSimplified a: track.getArtists()){
             featured.add(a.getId());
        }
        featured.remove(artistId);
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

    private static boolean insertArtist(SpotifyApi spotifyApi, MongoCollection<Document> artistCollection,
                                        String artistName, String spotifyId){
        Document idDoc = new Document("name", artistName)
                .append("uri", spotifyId);
        List<String> genres = getArtistGenres(spotifyApi, spotifyId);
        Paging<AlbumSimplified> albums = getAlbums(spotifyApi, spotifyId);
        Set<Document> albumDocuments = new HashSet<>();
        for(AlbumSimplified album: albums.getItems()){
            Document albumDoc = new Document("title", album.getName())
                    .append("uri", album.getUri());
            Set<Document> songDocuments = new HashSet<>();
            boolean explicit = false;
            for(TrackSimplified track: getTracks(spotifyApi,album.getId()).getItems() ){

                if(track.getIsExplicit())
                    explicit = true;
                Set<String> featured= getFeatured(spotifyId, track);

                Document songDoc = new Document("title", track.getName())
                        .append("duration", track.getDurationMs() / 1000)
                        .append("uri", track.getUri());

                if(!featured.isEmpty())
                    songDoc.append("featured", featured);

                songDocuments.add(songDoc);
            }

            albumDoc.append("is_explicit", explicit)
                    .append("songs", songDocuments);
            albumDocuments.add(albumDoc);

        }

        Document mainDoc = new Document("_id", idDoc)
                .append("genres", genres)
                .append("albums", albumDocuments);

        try{
            artistCollection.insertOne(mainDoc);
            return true;
        } catch (MongoWriteException mwe){
            mwe.printStackTrace();
        }
        return false;
    }

    private static void refreshTokens(MongoCollection<Document> usersCollection, String username){
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
}
