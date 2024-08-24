package web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

public class HttpClient {
    public static final String HTTP_1_1 = "HTTP/1.1";

    public static HttpClient newHttpClient() {
        return new HttpClient();
    }

    private HttpClient() {
        String version = System.getProperty("java.version");
        System.out.println(version.substring(0, version.indexOf('.')));
        int major = Integer.parseInt(version.substring(0, version.indexOf('.')));
        if (major < 12) {
            allowMethods("PATCH");
        }        
    }

    public HttpResponse execute(HttpRequest request) {
        // Create Connection
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) request.uri().toURL().openConnection();
        } catch (IOException e) {
            throw new HttpException("Exception connecting to uri: " + request.uri(), e);
        }

        try {
            connection.setRequestMethod(request.method());
        } catch (ProtocolException e) {
            throw new HttpException("Invalid Protocol: " + request.uri(), e);
        }
        for (Entry<String, String> entry : request.headers().entrySet()) {
            // System.out.println("Setting Header: " + entry.getKey() + " / " +entry.getValue());
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setRequestProperty("User-Agent", "ASTRA");

        // Send Message Body (if existing)
        if (request.body().length > 0) {
            connection.setDoOutput(true);
            try {
                connection.getOutputStream().write(request.body(), 0, request.body().length);
            } catch (IOException e) {
                throw new HttpException("Exception sending request body ro uri: " + request.uri(), e);
            }
        }

        HttpResponse response = HttpResponse.newBuilder(connection).build();
        connection.disconnect();
        return response;
    }

    private static void allowMethods(String... methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }    
}
