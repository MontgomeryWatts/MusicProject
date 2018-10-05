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

import static com.mongodb.client.model.Filters.eq;

public class CreatePlaylist {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");

        String userID = "loco__motives";
        String playlistName = "All Isaiah Rashad Songs";

        final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost");
        String code = "AQCja4ud9NF5RlByOEZnozXuc2NwQ0GWjx_BREBXXPX8zAaxRnzq1BP7isB1bQG812tqVsXmKL_1-eVKYWO81RoQTa2csffPohr1ZrH3fn0fOP2aHg-xO1PgLN4XqtP-oTY0dR80T4gljJF4bnGimLQYg_QD0aO5u9vofByMfifkNvM2qwtKDwxWdvpgN69BtjQAZdH5IgrDqiCU_jM";
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
                .description("That I have")
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
        ArrayList<String> trackIDs = new ArrayList<>();
        for (Document doc: songs.find(eq("artist", "Isaiah Rashad"))){
            trackIDs.add(doc.getString("_id"));
        }

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
