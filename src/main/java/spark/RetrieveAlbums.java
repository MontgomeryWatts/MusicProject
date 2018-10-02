package spark;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bson.Document;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static db.queries.DatabaseQueries.*;
import static spark.Spark.get;
import static spark.Spark.post;

@SuppressWarnings("unchecked")
public class RetrieveAlbums {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(RetrieveAlbums.class, "/");

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> artists = db.getCollection("artists");

        Spark.setPort(80);

        post(new Route("/search"){
            @Override
            public Object handle(Request request, Response response) {
                StringWriter writer = new StringWriter();

                String artistName = request.queryParams("input");
                Document artistDoc  = getArtistDoc(artists, artistName);

                //Prevents a 500 error/NPE from being thrown if the artist is not in the database
                if(artistDoc == null)
                    response.redirect("/search");

                else {
                    List<Document> albums = (List<Document>) artistDoc.get("albums");
                    String artist = artistDoc.getString("_id");

                    try {
                        Template t = config.getTemplate("retrieveAlbums.ftl");
                        Map<String, Object> map = new HashMap<>();
                        map.put("artist", artist);
                        map.put("albums", albums);
                        t.process(map, writer);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }

                return writer;
            }
        });

        get(new Route("/search"){
            @Override
            public Object handle(Request request, Response response) {
                StringWriter writer = new StringWriter();
                try{
                    Template t = config.getTemplate("retrieveAlbums.ftl");
                    Map<String, Object> map = new HashMap<>();
                    t.process(map, writer);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }

                return writer;
            }
        });

    }
}
