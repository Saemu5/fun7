package com.samod.fun7demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@RestController
public class EndpointController {
    static final String ext_api_uri = "https://us-central1-o7tools.cloudfunctions.net/fun7-ad-partner";

    @GetMapping("/")
    public HashMap<String, String> getParams(@RequestParam(value="timezone") String timezone,
                             @RequestParam(value="userID") String userID,
                             @RequestParam(value="cc") String cc) {


        var reply = new HashMap<String, String>();

        try {
            var db = Fun7demoApplication.db;

            int id = Integer.parseInt(userID);
            int loginCount = db.getLogins(id);
            System.out.println("input: " + userID + " " + cc + " " + loginCount);

            if (loginCount == -1){
                db.addNewUser(id);
            }
            db.incrementLoginCount(id);

            reply.put("multiplayer", (loginCount >= 5) && cc.equals("840") ? "enabled" : "disabled");

            ZonedDateTime ljTime = ZonedDateTime.now(ZoneId.of("Europe/Ljubljana"));
            reply.put("user-support", ljTime.getHour() >= 9 && ljTime.getHour() < 15 ? "enabled" : "disabled");

            var client = HttpClient.newHttpClient();
            String login = Base64.getEncoder().encodeToString(("fun7user:fun7pass").getBytes());
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(ext_api_uri + "?countryCode=" + "840"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Basic " + login)
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                while (response.body().endsWith("seppuku!")) {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                }
                var map = new ObjectMapper().readValue(response.body(),
                        new TypeReference<Map<String, String>>() {});
                reply.put("ads", map.get("ads").startsWith("sure") ? "enabled" : "disabled");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e){
            DbController.printSQLException(e);
        }
        return reply;
    }
}

