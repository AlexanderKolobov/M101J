/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.week_2.spark;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bson.Document;

import java.io.StringWriter;

import static spark.Spark.get;
import static spark.Spark.halt;

public class HelloWorldMongoDBSparkFreemarkerStyle {
    public static void main(String[] args)  {
        final Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading
                      (HelloWorldMongoDBSparkFreemarkerStyle.class, "/freemarker");

        MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));

        MongoDatabase database = client.getDatabase("m101");
        final MongoCollection<Document> collection = database.getCollection("hello");
        collection.drop();

        collection.insertOne(new Document("name", "MongoDB"));

        get("/",(request, response) ->  {
                StringWriter writer = new StringWriter();
                try {
                    Template helloTemplate = configuration.getTemplate("hello.ftl");

                    Document document = collection.find().first();

                    helloTemplate.process(document, writer);
                } catch (Exception e) {
                    halt(500);
                    e.printStackTrace();
                }
                return writer;
        });
    }
}
