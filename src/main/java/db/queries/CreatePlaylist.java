package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import static db.queries.DatabaseQueries.*;

@SuppressWarnings("unchecked")
public class CreatePlaylist {

    private static final int DEFAULT_DURATION = 3600;

    public static void main(String[] args) {
        getTrackUris(null, DEFAULT_DURATION, null);
    }


    public static Set<String> getTrackUris(String artist, int duration, Set<String> genres){
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        MongoCollection<Document> collabCollection = db.getCollection("collabs");

        Set<Document> songs  = new HashSet<>();

        if(artist != null){

            songs.addAll(getSongsByArtist(songsCollection, artist));

            for(String name: getArtistCollabNames(collabCollection, artist)){
                songs.addAll( getSongsByArtist(songsCollection, name) );
            }

            Set<String> artistGenres = getArtistGenres(artistCollection, artist);

            songs.addAll(getSongsByGenre(artistCollection, songsCollection, artistGenres));
        }

        if(genres != null){
            songs.addAll( getSongsByGenre( artistCollection, songsCollection, genres));
        }

        //Should no criteria be entered at all
        if( artist == null && genres == null){
            songs.addAll( getSongsByRandom(songsCollection));
        }


        List<Document> songsList = new ArrayList<>(songs);
        Set<String> playlistURIs = new HashSet<>();
        while ( duration > 180 && !songsList.isEmpty()){
            Document song = songsList.remove(Math.abs(new Random().nextInt()) % songsList.size());
            int songDuration = song.getInteger("duration");
            if (songDuration <= duration) {
                playlistURIs.add(song.getString("_id"));
                System.out.println( song.getString("title") + "  -  " + song.getString("artist"));
            }
            duration -= songDuration;
        }

        return playlistURIs;
    }
}
