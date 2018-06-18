package db.updates.spotify;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import org.bson.Document;


import java.util.List;

import static db.updates.spotify.SpotifyHelpers.*;

public class AddSongs {

    public static void addAllSongs(String artistName, MongoCollection<Document> col) {

        SpotifyApi spotifyApi = createSpotifyAPI();
        String artistID = getArtistID(spotifyApi, artistName);
        Paging<AlbumSimplified> albums = getAlbums(spotifyApi, artistID);

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

                try {

                    doc = new Document("_id", id).append("artist", artistName);

                    if (featured.size() != 0)
                        doc.append("featured", featured);

                    doc.append("album", album.getName())
                            .append("title", title)
                            .append("duration", duration);
                    col.insertOne(doc);

                } catch (MongoWriteException mwe) {

                }

            }
        }
    }
}
