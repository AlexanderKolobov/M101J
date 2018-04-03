package com.mongodb.week_2.HW2;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

/**
 * Created by Alexander Kolobov on 4/3/2018.
 */
public class HW_2_3 {
    public static void main(String[] args) {
        MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));

        MongoDatabase database = client.getDatabase("students");
        final MongoCollection<Document> collection = database.getCollection("grades");

        for (int i = 0; i < 200; i++) {
            Bson filter = and(eq("student_id", i), eq("type", "homework"));
            Bson sort = ascending("score");
            if (filter != null) {
                FindIterable<Document> doc = collection.find(filter).sort(sort);
                // make deletion of all documents in one iteration
                collection.deleteOne(doc.first());
            }
        }
    }
}
