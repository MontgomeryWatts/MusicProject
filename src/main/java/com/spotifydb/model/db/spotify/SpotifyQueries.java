package com.spotifydb.model.db.spotify;

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

import java.net.URI;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class SpotifyQueries {

    /*


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
     * Attempts to create an empty Spotify playlist, and returns the spotify id of the playlist so that tracks may be
     * added to it.
     * @param username The username of the person to create a playlist for
     * @param playlistName The name of the playlist to create
     * @return a String containing the Spotify Id of the playlist


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

    /**
     * Gets a set of any other artists featured on a track.
     * @param artistId The id of the artist whose album the track is on
     * @param track The track
     * @return A Set containing any featured artists


    private static Set<String> getFeatured(String artistId, TrackSimplified track){
        Set<String> featured = new HashSet<>();
        for(ArtistSimplified a: track.getArtists()){
             featured.add(a.getId());
        }
        featured.remove(artistId);
        return featured;
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
        Album[] albums = getAlbumsById(spotifyApi, albumIds);

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
                    if(trackFeatures != null){ //Some tracks don't have audio analysis
                        int bpm = Math.round(trackFeatures.getTempo());
                        songDoc.append("bpm", bpm);
                    }
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
    */
}
