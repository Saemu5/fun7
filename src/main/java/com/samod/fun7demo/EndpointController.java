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
import java.util.*;


@RestController
public class EndpointController {
    DbController db = Fun7demoApplication.db; //for easier referencing

    //modifiable parameters
    final String allowedCountry = "US";
    final int allowedLoginCount = 5;
    final int [] supportHours = new int[]{9,15};
    final String supportCity = "Europe/Ljubljana";
    final String ext_api_uri = "https://us-central1-o7tools.cloudfunctions.net/fun7-ad-partner";
    final String ext_api_login = Base64.getEncoder().encodeToString(("fun7user:fun7pass").getBytes());

    @GetMapping("/") //handles request input and output, returned map is pushed straight to output in json form
    public HashMap<String, String> processRequest(@RequestParam(value="timezone") String timezone,
                             @RequestParam(value="userID") String userID,
                             @RequestParam(value="cc") String countryCode) {


        var output = new HashMap<String, String>(); //output map to be populated

        //check for proper input, return error otherwise
        if (userID.length() > 40 || userID.length() < 1 || !Arrays.asList(Locale.getISOCountries()).contains(countryCode)){
            return makeErrorMap("400", "Bad Request");
        }

        try {
            //check for user's login count in db
            int loginCount = db.getLogins(userID);

            //if not existent, add new user to db
            if (loginCount == -1){ db.addNewUser(userID); }

            //add current session to login count
            db.incrementLoginCount(userID);

            System.out.println("input: user: " + userID + " cc: " + countryCode + " login count: " + loginCount); //verbose print

            //set multiplayer access based on country code and login count
            output.put("multiplayer",(loginCount >= allowedLoginCount) && countryCode.equals(allowedCountry)
                    ? "enabled" : "disabled");

            //set user-support availability according to support's local time
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of(supportCity));
            output.put("user-support",
                    localTime.getHour() >= supportHours[0] && localTime.getHour() < supportHours[1]
                            ? "enabled" : "disabled");

            //connect to external api to check for ads availability
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(ext_api_uri + "?countryCode=" + countryCode))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Basic " + ext_api_login)
                    .GET()
                    .build();

            //try sending request to external API and reading response
            //TODO: async?
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                //request handler likes to crash so try resending request until properly processed (usually once)
                while (response.body().endsWith("seppuku!")) {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                }
                //read response and set ad availability
                var map = new ObjectMapper().readValue(response.body(),
                        new TypeReference<Map<String, String>>() {});
                output.put("ads", map.get("ads").startsWith("sure")
                        ? "enabled" : "disabled");

            } catch (Exception e) { //handle request/response errors
                e.printStackTrace();
                return makeErrorMap("500", "Internal Server Error");
            }

        } catch (SQLException e){ //handle db errors
            DbController.printSQLException(e);
            return makeErrorMap("500", "Internal Server Error");
        }

        System.out.println(String.format("output: ads: %s, multiplayer: %s, user-support: %s",
                output.get("ads"), output.get("multiplayer"), output.get("user-support"))); //verbose print

        return output;
    }

    HashMap<String, String> makeErrorMap(String errCode, String errMsg){
        var map = new HashMap<String, String>();
        map.put("error", errCode);
        map.put("message", errMsg);
        map.put("code", errCode);
        return map;
    }
}

