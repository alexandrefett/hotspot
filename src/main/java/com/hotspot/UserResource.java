package com.hotspot;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

;import static spark.Spark.*;

public class UserResource {
    private static final String API_CONTEXT = "/api/v1";

    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        get("/hello", (req, res) -> "Hello World");

        get(API_CONTEXT + "/login", (req, res) -> {
            System.out.println("-------------------------");
            if(userService.login(req)){
                res.redirect("http://wwww.everest.com.br");
            } else {
                res.redirect("error.html", 500);
            }
            return null;
        }, new JsonTransformer());

        post(API_CONTEXT + "/newuser", (req, res) -> {
            userService.createUser(req.body());
                    res.status(201);
                    return res;
        }, new JsonTransformer());

        get(API_CONTEXT + "/users/id/:id", "application/json", (request, response)
                -> userService.find(request.params(":id")), new JsonTransformer());

        get(API_CONTEXT + "/users/:email", "application/json", (request, response)
                -> userService.findAll(request.params(":email")), new JsonTransformer());

        get(API_CONTEXT + "/users", "application/json", (request, response)
                -> userService.findAll(), new JsonTransformer());

        put(API_CONTEXT + "/users/:id", "application/json", (request, response)
                -> userService.update(request.params(":id"), request.body()), new JsonTransformer());

        get(API_CONTEXT + "/init", "application/json", (request, response)
                -> userService.init2(), new JsonTransformer());

        get(API_CONTEXT + "/reset", "application/json", (request, response)
                -> userService.reset(), new JsonTransformer());
    }

}
