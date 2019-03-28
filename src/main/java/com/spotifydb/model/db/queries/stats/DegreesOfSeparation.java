package com.spotifydb.model.db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.spotifydb.model.db.queries.DatabaseQueries;
import org.bson.Document;

import java.util.*;

public class DegreesOfSeparation {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");
        String initialArtist = "6aaMZ3fcfLv4tEbmY7bjRM";
        String targetArtist = "2h93pZq0e7k5yf4dywlkpM";
        System.out.println( showPath(artistCollection, initialArtist, targetArtist));
    }

    private static void addToPathString(StringBuilder stringBuilder, String artist, String initialArtist){
        stringBuilder.insert(0, artist);
        if( !artist.equals(initialArtist))
            stringBuilder.insert(0," -> ");
    }

    private static String showPath(MongoCollection<Document> collabCollection, String initialArtist, String targetArtist){
        List<String> names = DatabaseQueries.getArtistCollabNames(collabCollection, initialArtist);
        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> backpointers = new HashMap<>();

        // The value of backpointers is how you got to the key
        for(String name: names)
            backpointers.put(name, initialArtist);

        if(backpointers.containsKey(targetArtist)){
            addToPathString(stringBuilder, initialArtist, initialArtist);
            addToPathString(stringBuilder, targetArtist, initialArtist);
        }

        else{
            Queue<String> nameQueue = new LinkedList<>(names);
            while(!nameQueue.isEmpty()){
                String referencingArtist = nameQueue.remove();
                List<String> moreNames = DatabaseQueries.getArtistCollabNames(collabCollection, referencingArtist );

                for(String name: moreNames){
                    if(!backpointers.containsKey(name)){
                        backpointers.put(name, referencingArtist);
                    }
                }
                if(backpointers.containsKey(targetArtist)){
                    String cursor = targetArtist;
                    while( !cursor.equals(initialArtist) ) {
                        addToPathString(stringBuilder, cursor, initialArtist);
                        cursor = backpointers.get(cursor);
                    }
                    addToPathString(stringBuilder, initialArtist, initialArtist);
                    return stringBuilder.toString();
                }
                else{
                    for(String name : moreNames){
                        if(!names.contains(name)){
                            nameQueue.add(name);
                            names.add(name);
                        }
                    }
                }
            }
        }

        return stringBuilder.toString();
    }
}
