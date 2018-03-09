package com.hotspot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;
import me.legrange.mikrotik.ResultListener;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import sun.security.provider.MD5;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class UserService {
    private final MongoDatabase db;
    private final MongoCollection<Document> collection;

    public UserService(MongoDatabase db) {
        this.db = db;
        this.collection = db.getCollection("users");
    }

    public List<User> findAll() {
        MongoCursor cursor = collection.find().iterator();
        List<User> users = new ArrayList<User>();
        System.out.println("-------------------------");
        System.out.println("findall:");

        try {
            while (cursor.hasNext()) {
                System.out.println("-------------------------");
                System.out.println("cursor");
//                System.out.println(cursor.next().toString());
                users.add(new User((Document) cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return users;
    }

    public List<User> findAll(String email) {
        MongoCursor cursor = collection.find(new Document("email", email)).iterator();
        List<User> users = new ArrayList<User>();
        System.out.println("-------------------------");
        System.out.println("findall:");

        try {
            while (cursor.hasNext()) {
                System.out.println("-------------------------");
                System.out.println("cursor");
//                System.out.println(cursor.next().toString());
                users.add(new User((Document) cursor.next()));
            }
        } finally {
            cursor.close();
        }

        return users;
    }

    public List<User> init2() {
        Document d = new Document("name", "Alexandre")
                .append("email", "alexandrefett@gmail.com")
                .append("mac", "74:D4:35:B7:C5:4B");
        createUser(d.toJson());
        return findAll();
    }

    public List<User> init() {
        List<Document> data = new ArrayList<Document>();


        data.add(new Document("name", "Alexandre")
                .append("email", "alexandrefett@gmail.com")
                .append("mac", "74:D4:35:B7:C5:4B"));

        collection.insertMany(data);
        return findAll();
    }

    public void createNewUser(String body) {
        User user = new Gson().fromJson(body, User.class);
        //collection.insert(new BasicDBObject("email", user.getEmail()).append("mac", user.getMac()).append("createdOn", new Date()));
    }

    public String reset() {
        DeleteResult r = collection.deleteMany(new Document());
        return r.getDeletedCount() + "OK";
    }

    public User find(String id) {
        return new User((Document) collection.find(new Document("_id", id)));
    }

    public User update(String userId, String body) {
        User user = new Gson().fromJson(body, User.class);
        //collection.update(new BasicDBObject("_id", new ObjectId(userId)), new BasicDBObject("$set", new BasicDBObject("mac", user.getMac())));
        return this.find(userId);
    }

    public String createUser(String body) {
        //String name, String email, String mac){
        Document u = Document.parse(body);
        String name = u.getString("name");
        String email = u.getString("email");
        String mac = u.getString("mac");
        try {
            collection.insertOne(u);
            ApiConnection con = connect();

            // connection.execute(String.format("/ip/hotspot/user/profile/add name=%s","userprofile"));

            String command = "/ip/hotspot/user/add name=" + mac +
                    " email=" + email +
                    " mac-address=" + mac +
                    " profile=guest comment=" + name + "|" + email + "|" + mac;
            System.out.println("-------------------------");
            System.out.println("command: " + command);

            con.execute(command, new ResultListener() {
                @Override
                public void receive(Map<String, String> map) {
                    collection.insertOne(u);
                }

                @Override
                public void error(MikrotikApiException e) {
                    System.out.println("-------------------------");
                    System.out.println("error:" + e.getMessage());
                }

                @Override
                public void completed() {
                    System.out.println("-------------------------");
                    System.out.println("completed");
                    try {
                        con.close();
                    } catch (ApiConnectionException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (MikrotikApiException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
        return "{\"status\":\"OK\"}";
    }

    private boolean addUserMikrotik(ApiConnection con, String name, String email, String mac){
        try {
            String add = "/ip/hotspot/user/add name=" + mac + " email=" + email +
                    " mac-address=" + mac + " profile=guest comment=" + name.trim().replace(' ', '.') +
                    "|" + email.trim().replace(' ', '.') + "|" + mac;
            System.out.println("addUserMikrotik: " + add);
            con.execute(add);
        } catch (MikrotikApiException e){
            System.out.println("MikrotikApiException:"+e.getMessage());
            if (e.getMessage().equals("failure: already have user with this name for this server")) {
                return false;
            }
        }
        return true;
    }

    private boolean loginUserMikrotik(ApiConnection con, String ip, String mac){
        try {
            String login = "/ip/hotspot/active/login user=" + mac + " ip=" + ip;
            System.out.println("loginUserMikrotik: " + login);
            con.execute(login);
        } catch (MikrotikApiException e){
            System.out.println("MikrotikApiException:"+e.getMessage());
            return false;
        }
        return true;
    }

    private boolean removeUserMikrotik(ApiConnection con, String mac){
        try {
            String remove = "/ip/hotspot/user/remove name=" + mac;
            System.out.println("removeUserMikrotik: " + remove);
            con.execute(remove);
        } catch (MikrotikApiException e){
            System.out.println("MikrotikApiException:"+e.getMessage());
            return false;
        }
        return true;
    }

    private boolean addUserMongoDB(String name, String email, String mac){
        try {
            Document u = new Document("name", name).append("email", email).append("mac", mac);
            System.out.println("addUserMongoDB:" + u.toJson());
            collection.insertOne(u);
        } catch (MongoException e) {
            System.out.println("MongoException:" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean login(Request body) {

        try {
            System.out.println("login: " + body.body());

            String name = URLDecoder.decode(body.queryParams("name"),"UTF-8");
            String email = URLDecoder.decode(body.queryParams("email"),"UTF-8");
            String mac = URLDecoder.decode(body.queryParams("username"),"UTF-8");
            String ip = URLDecoder.decode(body.queryParams("ip"),"UTF-8");

            ApiConnection com = connect();
            if(addUserMikrotik(com, name, email, mac)){
                loginUserMikrotik(com, ip, mac);
                addUserMongoDB(name, email, mac);
            } else {
                if(removeUserMikrotik(com, mac)){
                    if(addUserMikrotik(com, name, email, mac)){
                        loginUserMikrotik(com, ip, mac);
                        addUserMongoDB(name, email, mac);
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private ApiConnection connect() {
        try {
            ApiConnection con = ApiConnection.connect("192.168.1.1"); // connect to router
            con.login("alexandrefett", "ax34y0"); // log in to router
            System.out.println("Mikrotik connected");
            return con;
        } catch (MikrotikApiException e) {
            System.out.println("Mikrotik not connected");
            e.printStackTrace();
            return null;
        }
    }

    private void sendPost(String name, String email, String mac, String pass) throws Exception {

        String url = "http://192.168.1.1/login";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "popup=true&username="+ mac + "&password="+pass;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }
}

// "/ip/hotspot/active/add name=50:92:B9:3C:C6:6A
