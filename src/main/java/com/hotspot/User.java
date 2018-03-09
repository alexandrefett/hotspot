package com.hotspot;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    private String id;
    private String mac;
    private String email;
    private String name;
    private Date createdOn = new Date();

    public User(Document doc) {
        this.id = ((ObjectId) doc.get("_id")).toString();
        this.mac = doc.getString("mac");
        this.name = doc.getString("name");
        this.email = doc.getString("email");
        this.createdOn = doc.getDate("createdOn");
    }

    public User(String name, String mac, String email) {
        this.mac = mac;
        this.email = email;
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedOn() {
        return createdOn;
    }
}
