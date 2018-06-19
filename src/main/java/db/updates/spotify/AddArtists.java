package db.updates.spotify;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static db.updates.spotify.SpotifyHelpers.*;

public class AddArtists {
    public static void addAllArtists(String artistName, MongoCollection<Document> col) {

        SpotifyApi spotifyApi = createSpotifyAPI();
        String id = getArtistID(spotifyApi, artistName);
        List<String> genres = getArtistGenres(spotifyApi, artistName);
        List<Document> albums = new ArrayList<>();

        Document doc = new Document("_id", artistName)
                .append("genres", genres);

        for(AlbumSimplified album: getAlbums(spotifyApi, id).getItems()){
            albums.add(new Document("name", album.getName())
                    .append("spotify", album.getUri())
            );
        }

        doc.append("albums", albums)
            .append("spotify", getArtistURI(spotifyApi, artistName));

        try{
            col.insertOne(doc);
        } catch (MongoWriteException mwe){

        }
    }
}
