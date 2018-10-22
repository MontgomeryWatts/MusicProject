package spark;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bson.Document;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static db.queries.DatabaseQueries.*;
import static spark.Spark.get;

@SuppressWarnings("unchecked")
public class DisplayAlbums {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(DisplayAlbums.class, "/");

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artists = db.getCollection("artists");

        String artistName = "Jaden Smith";
        Document artistDoc  = getArtistDoc(artists, artistName);
        List<Document> albums = (ArrayList<Document>)artistDoc.get("albums");

        //If the artist name was not formatted correctly in the initial query, this will ensure it is displayed correctly
        Document idDoc = (Document) artistDoc.get("_id");
        String artist = idDoc.getString("name");

        Spark.setPort(80);

        get(new Route("/"){
            @Override
            public Object handle(Request request, Response response) {
                StringWriter writer = new StringWriter();

                try{
                    Template t = config.getTemplate("displayAlbums.ftl");
                    Map<String, Object> map = new HashMap<>();
                    map.put("artist", artist);
                    map.put("albums", albums);
                    t.process(map, writer);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }

                return writer;
            }
        });
    }
}
