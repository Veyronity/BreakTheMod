/* This file is part of BreakTheMod.
*
* BreakTheMod is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or (at your
* option) any later version.
*
* BreakTheMod is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with BreakTheMod.  If not, see <https://www.gnu.org/licenses/>.
*
* For more information, please visit <https://discord.gg/kwvrgt6jH5>.
*/
package com.utils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class fetch {

    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");
    
    /**
     * Sends an HTTP POST request with the provided URL and JSON payload and returns the response as a [String].
     * @param url the URL to send the request to
     * @param jsonPayload the JSON payload to include in the request body
     * @return the response body as a JsonObject
     * @throws Exception It covers all possible exceptions
     */
    public String Fetch(String url, String payload) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        if (payload != null) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
        } else {
            requestBuilder.GET();
        }


        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        try (JsonReader reader = new JsonReader(new StringReader(responseBody))) {
            reader.setLenient(true);
            return responseBody;
        } catch (JsonSyntaxException e) {
            LOGGER.error("Failed to parse JSON. Response body: " + responseBody, e);
            throw new Exception("Failed to parse JSON. Response body: " + responseBody, e);
        }
    }
}