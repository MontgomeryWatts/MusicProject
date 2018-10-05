package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import static db.queries.DatabaseQueries.*;

@SuppressWarnings("unchecked")
public class CreatePlaylist {
    public static void main(String[] args) {
        String artist = "madlib";
        int duration = 3600;
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        MongoCollection<Document> collabCollection = db.getCollection("collabs");

        Document artistDoc = getArtistDoc(artistCollection, artist);
        String artistName = artistDoc.getString("_id");
        Set<Document> songs = getArtistSongs(songsCollection, artistName);
        List<String> artistGenres = (ArrayList<String>) artistDoc.get("genres");

        //Get all artists with related genres
        HashSet<String> artistNames = new HashSet<>();
        for (String genre: artistGenres) {
            for (Document artistDocs : artistCollection.find(new Document("genres", genre))){
                String name = artistDocs.getString("_id");
                artistNames.add(name);
            }
        }

        //Add all artists the given artist has worked with
        artistNames.addAll( getArtistCollabs(collabCollection, artistName) );

        for (String name: artistNames){
            System.out.println(name);
            songs.addAll( getArtistSongs(songsCollection, name) );
        }

        List<Document> possibleSongs = new ArrayList<>(songs);
        List<Document> playlist = new ArrayList<>();
        while (duration > 180){
            Document d = possibleSongs.remove(Math.abs(new Random().nextInt()) % possibleSongs.size());
            int songDuration = d.getInteger("duration");
            if (songDuration <= duration)
                playlist.add(d);
            duration -= songDuration;
        }
        for(Document songDoc: playlist){
            System.out.println(songDoc.getString("title") + " - " + songDoc.getString("artist"));
        }
    }

    public static List<String> getTrackUris(String artist){
        int duration = 3600;
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");
        MongoCollection<Document> songsCollection = db.getCollection("songs");
        MongoCollection<Document> collabCollection = db.getCollection("collabs");

        Document artistDoc = getArtistDoc(artistCollection, artist);
        String artistName = artistDoc.getString("_id");
        Set<Document> songs = getArtistSongs(songsCollection, artistName);
        List<String> artistGenres = (ArrayList<String>) artistDoc.get("genres");

        //Get all artists with related genres
        HashSet<String> artistNames = new HashSet<>();
        for (String genre: artistGenres) {
            for (Document artistDocs : artistCollection.find(new Document("genres", genre))){
                String name = artistDocs.getString("_id");
                artistNames.add(name);
            }
        }

        //Add all artists the given artist has worked with
        artistNames.addAll( getArtistCollabs(collabCollection, artistName) );

        for (String name: artistNames){
            songs.addAll( getArtistSongs(songsCollection, name) );
        }

        List<Document> possibleSongs = new ArrayList<>(songs);
        List<String> playlistURIs = new ArrayList<>();
        while (duration > 180){
            Document d = possibleSongs.remove(Math.abs(new Random().nextInt()) % possibleSongs.size());
            int songDuration = d.getInteger("duration");
            if (songDuration <= duration)
                playlistURIs.add( d.getString("_id") );
            duration -= songDuration;
        }

        return playlistURIs;
    }
}
