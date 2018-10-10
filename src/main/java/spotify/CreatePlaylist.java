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
        String code = "AQDknLbQeyEIQawtAw4QxKhaBa0TNtmRA8CTbCe0S5zFa1EHOnm7ROxf7ZDL11dnJRbFCDzF5f7BZNQnVtIinFXlt4OT746S5PSG2xmw5TA9qAQ-0uh8wxdhYDpSoml7gnZdSkZApAj4nT2HmqFAl8Jj5_4fyFY7jCDO4o7yrlCyg50SVcg6PAcyh668zBiQAwa9jnvVIIBmK0VTUA6nrQ";
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
            System.out.println("Error: " + e.getMessage());
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
            System.out.println("Error: " + e.getMessage());
        }



        //Add tracks to playlist
        Set<String> trackIDs = db.queries.CreatePlaylist.getTrackUris("Lil Wayne", 7200, null);

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
