package db.queries;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.text.DecimalFormat;

public class TotalDuration {
    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> col = db.getCollection("songs");


        long seconds = 0L;

        for(Document doc: col.find()){
            seconds += doc.getInteger("duration");
        }

        printFormattedTime(seconds);

    }

    public static void printFormattedTime(long seconds){
        DecimalFormat df = new DecimalFormat("00");
        System.out.println( df.format(seconds/3600 / 24) + ":" +
                df.format(seconds/3600 % 24) + ":" +
                df.format(seconds/60 % 60) + ":" +
                df.format(seconds%60) );
    }
}
