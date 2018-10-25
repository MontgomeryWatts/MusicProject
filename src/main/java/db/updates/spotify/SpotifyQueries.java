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
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
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
        Artist artist = getArtistById(spotifyApi, artistId);

        if(artist != null)
            insertArtist(spotifyApi, artistCollection, artist);
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
        Paging<Artist> artists = getArtistsByName(spotifyApi, artistName);

        for( Artist artist: artists.getItems()){
            insertArtist(spotifyApi, artistCollection, artist);
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

    private static Album[] getAlbums(SpotifyApi spotifyApi, Set<String> ids){
        String[] idArray = ids.toArray(new String[0]);
        if (idArray.length == 0)
            return new Album[0];

        GetSeveralAlbumsRequest request = spotifyApi.getSeveralAlbums(idArray)
                .market(CountryCode.US)
                .build();
        try {
            return request.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getAlbums, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getAlbums(spotifyApi, ids);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getAlbums(spotifyApi, ids);
        } catch (Exception e){
            e.printStackTrace();
        }
        return new Album[0];
    }

    /**
     * Returns a Set containing all of the Spotify ids of the given artist's albums
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistID The ID of the artist in Spotify's URI
     * @return a Set containing all of the Spotify ids of the given artist's albums
     */

    private static Set<String> getAlbumIds(SpotifyApi spotifyApi, String artistID){
        Set<String> albumIds = new HashSet<>();

        final GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistID)
                .market(CountryCode.US)
                .album_type("album,single")
                .build();
        try {
            Paging<AlbumSimplified> albums = albumsRequest.execute();
            for(AlbumSimplified album : albums.getItems()){
                albumIds.add(album.getId());
            }
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getAlbums, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getAlbumIds(spotifyApi, artistID);

        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
            return getAlbumIds(spotifyApi, artistID);
        } catch (Exception e){
            e.printStackTrace();
        }

        return albumIds;
    }

    /**
     * Returns a Paging<Artist> for the artist name provided. Since artist names are not necessarily unique, this returns
     * any artists that may potentially be the one of interest.
     * @param spotifyApi A SpotifyAPI Object to generate requests
     * @param artistName The name of an artist
     * @return Collection of all Artists that have the provided name
     */

    private static Paging<Artist> getArtistsByName(SpotifyApi spotifyApi, String artistName){

        SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .build();

        try{
            return artReq.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistIds, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistsByName(spotifyApi, artistName);
        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return getArtistsByName(spotifyApi, artistName);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new Paging.Builder<Artist>().build();
    }

    private static Artist getArtistById(SpotifyApi spotifyApi, String artistID){
        GetArtistRequest request = spotifyApi.getArtist(artistID).build();

        try{
            return request.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistName, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return  getArtistById(spotifyApi, artistID);
        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return  getArtistById(spotifyApi, artistID);
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

    private static boolean insertArtist(SpotifyApi spotifyApi, MongoCollection<Document> artistCollection, Artist artist){
        String artistName = artist.getName();
        String spotifyId = artist.getId();
        Document idDoc = new Document("name", artistName)
                .append("uri", spotifyId);

        Image[] artistImages = artist.getImages();
        if(artistImages.length != 0)
            idDoc.append("image", artistImages[0].getUrl());

        List<String> genres = Arrays.asList(artist.getGenres());
        Set<String> albumIds = getAlbumIds(spotifyApi, spotifyId);
        Album[] albums = getAlbums(spotifyApi, albumIds);
        Set<Document> albumDocuments = new HashSet<>();
        for(Album album: albums){
            int year = Integer.parseInt(album.getReleaseDate().substring(0, 4));
            Document albumDoc = new Document("title", album.getName())
                    .append("uri", album.getUri())
                    .append("year", year);

            Image[] albumImages = album.getImages();
            if(albumImages.length != 0)
                albumDoc.append("image", albumImages[0].getUrl());

            Set<Document> songDocuments = new HashSet<>();
            boolean explicit = false;
            for(TrackSimplified track: album.getTracks().getItems() ){

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
