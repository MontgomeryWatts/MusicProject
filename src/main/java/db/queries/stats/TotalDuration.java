package db.queries.stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.DatabaseQueries;
import org.bson.Document;

import java.text.DecimalFormat;

/**
 * Prints how long it would take to listen to every song currently in the database
 */
public class TotalDuration {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artistCollection = db.getCollection("artists");

        printFormattedTime(DatabaseQueries.getTotalDuration(artistCollection));
    }

    private static void printFormattedTime(int seconds){
        DecimalFormat df = new DecimalFormat("00");
        System.out.println( seconds/3600 / 24 + ":" +
                df.format(seconds/3600 % 24) + ":" +
                df.format(seconds/60 % 60) + ":" +
                df.format(seconds%60) );
    }
}
