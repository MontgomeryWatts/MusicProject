package db.updates.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * The purpose of this program is to create a collection that indicates which artists have collaborated on
 * songs with each other without having to do multiple queries at runtime to gather the same information.
 * This program only writes to the collabs collection. If you wish to create the song and artist Documents
 * that correspond to these newly create documents, run {@link db.updates.spotify.AddReferencedArtists#main(String[])}}
 */

@SuppressWarnings("unchecked")
public class AddCollabs {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songs = db.getCollection("songs");
        MongoCollection<Document> artists = db.getCollection("artists");
        MongoCollection<Document> collabs = db.getCollection("collabs");


        for(Document artistDoc: artists.find()){
            try {
                String artistName = artistDoc.getString("_id");
                HashSet<String> featuredArtists = new HashSet<>();

                //For every song Document that is by the artist and has other artists featured on it
                for (Document songDoc : songs.find(and(eq("artist", artistName), exists("featured")))) {
                    //For every artist in the featured list
                    for (String featured : (List<String>) songDoc.get("featured")) {
                        featuredArtists.add(featured);


                        //If an artist has worked with other this should be shown in both of their docs
                        Document featuredDoc = collabs.find(eq("_id", featured)).first();

                        //If the other artist already has a document
                        if (featuredDoc != null) {
                            HashSet<String> otherFeaturedArtists = new HashSet<>((ArrayList<String>) featuredDoc.get("collabs"));
                            otherFeaturedArtists.add(artistName);
                            featuredDoc.put("collabs", otherFeaturedArtists);
                            collabs.replaceOne(eq("_id", featured), featuredDoc);
                        }
                        else{
                            HashSet<String> singleton = new HashSet<>();
                            singleton.add(artistName);
                            featuredDoc = new Document("_id", featured)
                                .append("collabs", singleton);
                            collabs.insertOne(featuredDoc);
                        }


                    }
                }

                Document collabDoc = collabs.find( eq("_id", artistName)).first();

                //If the current artist's document has already been created by the insertion of another artist's doc
                if(collabDoc != null){
                    HashSet<String> existingFeatures = new HashSet<>((ArrayList<String>) collabDoc.get("collabs"));
                    featuredArtists.addAll( existingFeatures );
                    collabDoc.put("collabs", featuredArtists);
                    collabs.replaceOne( eq("_id", artistName), collabDoc);
                }
                else{
                    collabDoc = new Document("_id", artistName)
                            .append("collabs", featuredArtists);
                    collabs.insertOne(collabDoc);
                }

            } catch (MongoWriteException mwe){
                mwe.printStackTrace();
            }
        }
    }
}
