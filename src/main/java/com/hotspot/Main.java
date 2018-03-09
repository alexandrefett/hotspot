package com.hotspot;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.BasicConfigurator;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

public class Main {

    public static void main(String[] args) {
        BasicConfigurator.configure();

        try {
            new UserResource(new UserService(mongo()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MongoDatabase mongo() throws Exception {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        //MongoClientURI uri = new MongoClientURI("mongodb+srv://alexandrefett:fvrAqxTY4IIiPtSO@everest-2ne3f.mongodb.net/test");
        MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("test");
            return database;
    }


}

