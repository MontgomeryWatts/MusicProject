package db.updates.old;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ChangeFieldType {
    public static void main(String[] args) {
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> songsCol = db.getCollection("songs");

        for(Document doc: songsCol.find()){
            if (doc.get("duration") instanceof Integer){
                Document replacement = new Document("$set", new Document("duration", new Double(doc.getInteger("duration"))));
                songsCol.updateOne(doc, replacement);
            }
        }
    }
}
