package db.implementations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresConnection {
    public static void main(String[] args) {

        String url = System.getenv("POSTGRES_URL");
        if (url == null)
            return;
        Connection connection;
        try {
            connection = DriverManager.getConnection(url);
            if( createArtistGenreTable(connection))
                System.out.println("Successfully created artist_genre table");
            else
                System.out.println("Unable to create artist_genre table");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean makeUpdateStatement(Connection connection, String sql){
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e){
            return false;
        }
        return true;
    }

    private static boolean createArtistTable(Connection connection){
        String sql = "CREATE TABLE ARTIST " +
                "(ARTIST_ID INT PRIMARY KEY NOT NULL," +
                " URI TEXT NOT NULL, " +
                " NAME TEXT NOT NULL)";
        return makeUpdateStatement(connection, sql);
    }

    private static boolean createGenreTable(Connection connection){
        String sql = "CREATE TABLE GENRE " +
                "(GENRE_ID INT PRIMARY KEY NOT NULL," +
                " NAME TEXT NOT NULL)";
        return makeUpdateStatement(connection, sql);
    }

    private static boolean createArtistGenreTable(Connection connection){
        String sql = "CREATE TABLE ARTIST_GENRE " +
                "(ARTIST_ID INT NOT NULL REFERENCES ARTIST(ARTIST_ID)," +
                " GENRE_ID INT NOT NULL REFERENCES GENRE(GENRE_ID)," +
                " PRIMARY KEY (ARTIST_ID, GENRE_ID))";
        return makeUpdateStatement(connection, sql);
    }
}
