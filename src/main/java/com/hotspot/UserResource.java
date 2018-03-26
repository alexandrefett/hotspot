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
            switch(userService.login(req)){
                case 200:{
                    res.redirect("http://www.everest.com.br");
                    break;
                }
                case 500:{
                    res.redirect("error.html", 500);
                    break;
                }
            }
            return null;
        }, new JsonTransformer());

        get(API_CONTEXT + "/users/id/:id", "application/json", (request, response)
                -> userService.find(request.params(":id")), new JsonTransformer());

        get(API_CONTEXT + "/users/:email", "application/json", (request, response)
                -> userService.findAll(request.params(":email")), new JsonTransformer());

        get(API_CONTEXT + "/users", "application/json", (request, response)
                -> userService.findAll(), new JsonTransformer());

        put(API_CONTEXT + "/users/:id", "application/json", (request, response)
                -> userService.update(request.params(":id"), request.body()), new JsonTransformer());

        get(API_CONTEXT + "/reset", "application/json", (request, response)
                -> userService.reset(), new JsonTransformer());

    }

}
