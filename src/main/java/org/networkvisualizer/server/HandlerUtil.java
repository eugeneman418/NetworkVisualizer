package org.networkvisualizer.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerUtil {

    // List<String> because query parameters need not be unique ?x=1&x=2 is valid and will be mapped to x:[1,2]
    static Map<String, List<String>> parseQueryParams(URI uri) {
        Map<String, List<String>> queryParams = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return queryParams;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyVal = pair.split("=", 2);
            String key = URLDecoder.decode(keyVal[0], StandardCharsets.UTF_8);
            String value = keyVal.length > 1
                    ? URLDecoder.decode(keyVal[1], StandardCharsets.UTF_8)
                    : "";
            queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return queryParams;
    }

    static void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.sendResponseHeaders(statusCode, body.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body.getBytes());
        }
    }

}
