/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
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
 *
 */

package com.mongodb.week_1;

import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.Spark;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

public class SparkFormHandling {
    public static void main(String[] args) {
        // Configure Freemarker
        final Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(SparkFormHandling.class, "/");

        // Configure routes
        Spark.get("/", (request, response) -> {
            try {

                Map<String, Object> fruitsMap = new HashMap<>();
                fruitsMap.put("fruits",
                        Arrays.asList("apple", "orange", "banana", "peach", "kiwi"));

                Template fruitPickerTemplate =
                        configuration.getTemplate("fruitPicker.ftl");
                StringWriter writer = new StringWriter();
                fruitPickerTemplate.process(fruitsMap, writer);
                return writer;

            } catch (Exception e) {
                halt(500);
                return null;
            }
        });

        Spark.post("/favorite_fruit", (request, response) -> {
            final String fruit = request.queryParams("fruit");
            if (fruit == null) {
                return "Why don't you pick one?";
            }
            else {
                return "Your favorite fruit is " + fruit;
            }
        });
    }
}
