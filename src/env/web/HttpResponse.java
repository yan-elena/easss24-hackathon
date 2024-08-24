package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponse {
    public static class HttpResponseBuilder {
        private HttpURLConnection connection;

        public HttpResponseBuilder(HttpURLConnection connection) {
            this.connection = connection;
        }
        
        public HttpResponse build() {
            HttpResponse response = new HttpResponse();
            try {
                response.code = connection.getResponseCode();
            } catch (IOException e) {
                throw new HttpException("Failure to read response code", e);
            }

            for (Entry<String, List<String>> entry: connection.getHeaderFields().entrySet()) {
                response.headers.put(entry.getKey(), entry.getValue().get(0));
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4];

            InputStream is;
            if (200 <= response.code && response.code <= 299) {
                try {
                    is = connection.getInputStream();
                } catch (IOException e) {
                    throw new HttpException("Failure to get inputstream in response", e);
                }
                try {
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                } catch (IOException e) {
                    throw new HttpException("Error reading response", e);
                }
            
                try {
                    buffer.flush();
                } catch (IOException e) {
                    throw new HttpException("Error flushing response buffer", e);
                }
                response.body = buffer.toByteArray();
            // } else {
            //     is = connection.getErrorStream();
            }


            return response;
        }
    }

    public static HttpResponseBuilder newBuilder(HttpURLConnection connection) {
        return new HttpResponseBuilder(connection);
    }

    private int code;
    private Map<String,String> headers = new HashMap<>();
    private byte[] body;

    private HttpResponse() {
    }

    public int code() {
        return code;
    }

    public byte[] body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }
}
