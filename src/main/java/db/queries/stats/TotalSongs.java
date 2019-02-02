package db.queries.stats;

import db.implementations.DatabaseConnection;
import db.implementations.MongoConnection;


public class TotalSongs {
    public static void main(String[] args) {

        DatabaseConnection db = new MongoConnection();

        System.out.println(db.getNumberOfSongs());
    }
}
