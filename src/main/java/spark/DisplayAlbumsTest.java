package spark;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import db.queries.FindArtistCaseInsensitive;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bson.Document;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

public class DisplayAlbumsTest {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(SparkTests.class, "/");

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artists = db.getCollection("artists");

        String artistName = "xxxtentacion";
        Document artistDoc  = FindArtistCaseInsensitive.findArtist(artists, artistName);
        List<Document> albums = (List<Document>)artistDoc.get("albums");

        //If the artist name was not formatted correctly in the initial query,
        //this will ensure it is displayed correctly
        String artist = artistDoc.getString("_id");

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
