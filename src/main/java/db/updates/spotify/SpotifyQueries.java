package db.updates.spotify;

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
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import org.bson.Document;

import java.io.File;
import java.net.URI;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class SpotifyQueries {
    private static final int MAX_ATTEMPTS = 10;
    private static final int WAIT_TIME = 2000;

    public static Document getArtistDocById(String artistId) {
        SpotifyApi spotifyApi = createSpotifyAPI();
        Artist artist = getArtistById(spotifyApi, artistId);
        return (artist == null) ? null : retrieveArtistInfo(spotifyApi, artist);
    }

    /**
     * Attempts to create and add an artist Document to a given collection.
     * @param artistName The name of the artist to attempt to add
     */

    static List<Document> getArtistDocsByName(String artistName) {
        List<Document> artistDocs = new ArrayList<>();

        SpotifyApi spotifyApi = createSpotifyAPI();
        Paging<Artist> artists = getArtistsByName(spotifyApi, artistName);

        Document artistDoc;
        for( Artist artist: artists.getItems()){
            artistDoc = retrieveArtistInfo(spotifyApi, artist);
            if(artistDoc != null)
                artistDocs.add(artistDoc);
        }
        return artistDocs;
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
     * @param username The username of the person to create a playlist for
     * @param playlistName The name of the playlist to create
     * @return a String containing the Spotify Id of the playlist
     */

    public static String exportSpotifyPlaylist(String accessToken, String refreshToken, String username, String playlistName){
        SpotifyApi spotifyApi = createSpotifyAPI();

        spotifyApi.setAccessToken( accessToken );
        spotifyApi.setRefreshToken( refreshToken );

        CreatePlaylistRequest request = spotifyApi.createPlaylist(username, playlistName)
                .collaborative(false)
                .public_(true)
                .name(playlistName)
                .build();

        try {
            final Playlist playlist = request.execute();
            return playlist.getId();

        } catch (UnauthorizedException | ForbiddenException fe) {
            //TODO Pass a DatabaseConnection as a parameter when refreshing tokens
            //refreshTokens(usersCollection, username); //Stuff to refresh access/refresh tokens
            return exportSpotifyPlaylist(accessToken, refreshToken, username, playlistName);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static Album[] getAlbums(SpotifyApi spotifyApi, List<String> ids){
        int albumsToGet = ids.size();
        if(albumsToGet == 0)
            return new Album[0];
        int failedAttempts = 0;
        int startIndex = 0;
        int endIndex = (albumsToGet - startIndex > 20) ? startIndex + 20 : startIndex + (albumsToGet - startIndex);
        List<Album> albums = new ArrayList<>();

        while( (albumsToGet != 0) && (failedAttempts != MAX_ATTEMPTS) ){
            try{
                String[] idArray = ids.subList(startIndex, endIndex).toArray(new String[0]); //make sure inbounds
                GetSeveralAlbumsRequest request = spotifyApi.getSeveralAlbums(idArray)
                        .market(CountryCode.US)
                        .build();
                Album[] albumArray = request.execute();
                if(albumArray != null){
                    startIndex += 20;
                    endIndex = (ids.size() - startIndex > 20) ? startIndex + 20 : startIndex + (ids.size() - startIndex);
                    albums.addAll( Arrays.asList(albumArray) );
                    albumsToGet -= albumArray.length;
                }

            } catch(Exception e) {
                failedAttempts++;
                try{
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie){
                    System.err.println("please don't happen..");
                }
            }
        }
        return albums.toArray(new Album[0]);
    }

    private static List<String> getAlbumIds(SpotifyApi spotifyApi, String artistId){

        ArrayList<String> albumIds = new ArrayList<>();
        int offset = 0;
        int itemsAdded = 0;
        int idsToAdd = Integer.MAX_VALUE;
        int failedAttempts = 0;
        Paging<AlbumSimplified> albumsPaging;

        //While all album Ids have not been added and there have not been 10 failed attempts
        while( (idsToAdd - itemsAdded != 0) && (failedAttempts != MAX_ATTEMPTS) ){
            final GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistId)
                    .market(CountryCode.US)
                    .album_type("album,single")
                    .limit(50) //50 is the max that may be returned
                    .offset(offset)
                    .build();
            try{
                albumsPaging = albumsRequest.execute();


                if(albumsPaging != null){
                    offset += 50;
                    idsToAdd = albumsPaging.getTotal();
                    for(AlbumSimplified a: albumsPaging.getItems())
                        albumIds.add(a.getId());
                    itemsAdded = albumIds.size();
                }
            } catch (Exception e){
                failedAttempts++;
                try{
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie){
                    System.err.println("please don't happen..");
                }
            }
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
            System.out.println("TooManyRequestsException in getArtistsByName, waiting for " + wait + " milliseconds");
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
            System.out.println("TooManyRequestsException in getArtistById, waiting for " + wait + " milliseconds");
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

    private static AudioFeatures[] getAudioFeaturesForTracks(SpotifyApi spotifyApi, String[] trackIds){
        GetAudioFeaturesForSeveralTracksRequest request = spotifyApi.getAudioFeaturesForSeveralTracks(trackIds)
                .build();

        AudioFeatures[] features = new AudioFeatures[0];
        int failedAttempts = 0;
        while ((features.length != trackIds.length) && (failedAttempts != MAX_ATTEMPTS)){
            try{
                features = request.execute();
            } catch (Exception e){
                failedAttempts++;
            }
        }
        return features;
    }

    private static Document retrieveArtistInfo(SpotifyApi spotifyApi, Artist artist){
        String artistName = artist.getName();
        System.out.println("Attempting to retrieve information for " + artistName);

        String uri = artist.getId();

        Image[] artistImages = artist.getImages();
        if(artistImages.length == 0) //Only add artists with profile pictures
            return null;

        List<String> imageUrls = new ArrayList<>();
        for(Image image: artistImages)
            imageUrls.add(image.getUrl());

        List<String> genres = Arrays.asList(artist.getGenres());
        List<String> albumIds = getAlbumIds(spotifyApi, uri);
        Album[] albums = getAlbums(spotifyApi, albumIds);

        if(albums.length == 0) //Only add artists with music
            return null;

        List<Document> albumDocuments = new ArrayList<>();
        for(Album album: albums){
            int year = Integer.parseInt(album.getReleaseDate().substring(0, 4)); //Dates are in YYYY-MM-DD format
            Document albumDoc = new Document("title", album.getName())
                    .append("uri", album.getUri())
                    .append("year", year);

            Image[] albumImages = album.getImages();
            if(albumImages.length != 0) {
                albumDoc.append("image", albumImages[0].getUrl()); //The first image in the images array is the largest
            }

            List<Document> songDocuments = new ArrayList<>();
            boolean albumIsExplicit = false;

            TrackSimplified[] tracks = album.getTracks().getItems();
            String[] trackIds = new String[tracks.length];

            for(int i = 0; i < trackIds.length; i++){
                trackIds[i] = tracks[i].getId();
            }

            AudioFeatures[] audioFeatures = getAudioFeaturesForTracks(spotifyApi, trackIds);

            for(int i = 0; i < tracks.length; i++){
                TrackSimplified track = tracks[i]; //For simplicity's sake

                if (track.getIsExplicit()){
                    albumIsExplicit = true;
                }

                Document songDoc = new Document("title", track.getName())
                        .append("duration", track.getDurationMs() / 1000)
                        .append("uri", track.getUri());

                Set<String> featured= getFeatured(uri, track);
                if(!featured.isEmpty()) {
                    songDoc.append("featured", featured);
                }

                if(audioFeatures.length == tracks.length){ //If audio info has been retrieved for all songs in the album
                    AudioFeatures trackFeatures = audioFeatures[i];
                    int bpm = Math.round(trackFeatures.getTempo());
                    songDoc.append("bpm", bpm);
                }

                songDocuments.add(songDoc);
            }

            albumDoc.append("is_explicit", albumIsExplicit)
                    .append("songs", songDocuments);
            albumDocuments.add(albumDoc);
        }

        return new Document("_id", uri)
                .append("name", artistName)
                .append("images", imageUrls)
                .append("genres", genres)
                .append("albums", albumDocuments);
    }

    //TODO change this so it doesn't search DB itself, pass off job to a DatabaseConnection

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
            refreshTokens(usersCollection, username);
        }
    }
}
