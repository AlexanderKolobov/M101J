package com.mongodb.week_2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import static com.mongodb.util.Helpers.printJson;
import static java.util.Arrays.asList;



public class App {
    public static void main(String[] args) {
        // Connection to the DB
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(500).build();
        MongoClient client = new MongoClient(new ServerAddress(), options);

        MongoDatabase db = client.getDatabase("m101");

        MongoCollection<Document> coll = db.getCollection("test");
        coll.drop();

        // Documents
        BsonDocument bsonDocument = new BsonDocument("str", new BsonString("MongoDB, Hello!"));

        Document me = new Document()
                .append("name", "Kolobov")
                .append("age", 34)
                .append("position", "developer");

        Document nikolay = new Document()
                .append("name", "Tsyb")
                .append("age", 29)
                .append("position", "developer");

        printJson(me);

//        coll.insertOne(me);
        coll.insertMany(asList(me, nikolay));
        me.remove("_id");
        coll.insertOne(me);

        printJson(me);
        printJson(nikolay);

    }
}
