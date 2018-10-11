package spotify;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import db.updates.spotify.SpotifyQueries;
import org.bson.Document;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

public class CreatePlaylist {
    public static void main(String[] args) {

        String userID = "loco__motives";
        String playlistName = "Playlist For Lee";

        final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost");
        String code = "AQDSr-aybtUROQVCpzbFsVQ2l7I37xyNT6ZmWVcLUXjrtFn-VEjow3dSfBC5SHCecPq0cNo5dGglDEHJC2sOXNCPC9p2oJ-epxJDEusW7TUeSZuKsWM3sA1opMuFNRkMlQHU-QtGx8glB6f8GB1lhKwX4sqtswwtzxzx_Qjjk-O5g-phPXz-cZy6KKdNEB5fPBkjJnKmXs9Iy4D8mLFENg";
        SpotifyApi spotifyApi = SpotifyQueries.createSpotifyAPI(redirectUri);


        final AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                .build();

        //Get auth codes
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
        }

        //Make playlist
        CreatePlaylistRequest request = spotifyApi.createPlaylist(userID, playlistName)
                .collaborative(false)
                .public_(true)
                .name(playlistName)
                .build();

        String playlistID = null;
        try {
            final Playlist playlist = request.execute();
            playlistID = playlist.getId();

            System.out.println("Name: " + playlist.getName());
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
        }



        //Add tracks to playlist
        Set<String> trackIDs = db.queries.CreatePlaylist.getTrackUris("Isaiah Rashad", 7200, null);

        final String[] uris = trackIDs.toArray(new String[0]);

        final AddTracksToPlaylistRequest addTracksToPlaylistRequest = spotifyApi
                .addTracksToPlaylist(userID, playlistID, uris)
                .build();

        try {
            final SnapshotResult snapshotResult = addTracksToPlaylistRequest.execute();

            System.out.println("Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
