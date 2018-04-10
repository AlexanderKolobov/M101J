package com.mongodb.week_3.hw3;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by Alexander Kolobov on 4/10/2018.
 */
public class HW_3_1 {
    public static void main(String[] args) {
        MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));

        MongoDatabase database = client.getDatabase("students");
        final MongoCollection<Document> collection = database.getCollection("grades");


    }
}
