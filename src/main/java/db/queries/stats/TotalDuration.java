package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;

/**
 * Prints how long it would take to listen to every song currently in the database
 */
public class TotalDuration {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");


        Document songsDoc = artistCollection.aggregate(
                Arrays.asList(
                        unwind("$albums"),
                        unwind("$albums.songs"),
                        replaceRoot("$albums.songs"),
                        group(null, Accumulators.sum("duration", "$duration"))
                )
        ).first();

        printFormattedTime( songsDoc.getInteger("duration") );

    }

    private static void printFormattedTime(long seconds){
        DecimalFormat df = new DecimalFormat("00");
        System.out.println( seconds/3600 / 24 + ":" +
                df.format(seconds/3600 % 24) + ":" +
                df.format(seconds/60 % 60) + ":" +
                df.format(seconds%60) );
    }
}
