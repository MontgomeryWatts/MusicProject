package db.queries.stats;

import db.implementations.DatabaseConnection;
import db.implementations.MongoConnection;

import java.text.DecimalFormat;

/**
 * Prints how long it would take to listen to every song currently in the database
 */
public class TotalDuration {
    public static void main(String[] args) {
        DatabaseConnection db = new MongoConnection();

        printFormattedTime(db.getTotalDuration());
    }

    private static void printFormattedTime(int seconds){
        DecimalFormat df = new DecimalFormat("00");
        System.out.println( seconds/3600 / 24 + ":" +
                df.format(seconds/3600 % 24) + ":" +
                df.format(seconds/60 % 60) + ":" +
                df.format(seconds%60) );
    }
}
