package com.mongodb.week_3.hw3;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.util.Helpers.printJson;

/**
 * Created by Alexander Kolobov on 4/10/2018.
 */
//TODO finish this!!!
public class HW_3_1 {
    public static void main(String[] args) {
        MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));

        MongoDatabase database = client.getDatabase("school");
        final MongoCollection<Document> students = database.getCollection("students");

        ArrayList<Document> homework = students.aggregate(Arrays.asList(match(eq("scores.type", "homework")))).into(new ArrayList<>());
        System.out.println(homework.size());
        for (int i = 0; i < 2; i++) {
            printJson(homework.get(i));
        }
    }
}
