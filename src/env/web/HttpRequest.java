package web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";
    public static final String HEAD = "HEAD";
    public static final String OPTIONS = "OPTIONS";

    private static final byte[] NO_BODY = new byte[0];
    private static final BodyPublishers NO_BODY_PUBLISHERS = new BodyPublishers() {
        @Override
        public byte[] toByteArray() {
            return NO_BODY;
        }
    };

    public static interface BodyPublishers {
        public static BodyPublishers noBody() {
            return NO_BODY_PUBLISHERS;
        }

        public static BodyPublishers ofString(String body) {
            return new BodyPublishers() {
                @Override
                public byte[] toByteArray() {
                    try {
                        return body.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new HttpException("Exception adding String to HttpRequest body", e);
                    }
                }
            };
        }

        public static BodyPublishers ofByteArray(byte[] body) {
            return new BodyPublishers() {
                @Override
                public byte[] toByteArray() {
                    return body;
                }
            };
        }

        public byte[] toByteArray();
    }

    public static class HttpRequestBuilder {
        private String method;
        private String version = HttpClient.HTTP_1_1;
        private URI uri;
        private Map<String,String> headers = new HashMap<>();
        private BodyPublishers publishers = BodyPublishers.noBody();

        public HttpRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public HttpRequestBuilder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public HttpRequestBuilder version(String version) {
            this.version = version;
            return this;
        }

        public HttpRequestBuilder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public HttpRequestBuilder GET() {
            this.method = HttpRequest.GET;
            return this;
        }

        public HttpRequestBuilder POST() {
            this.method = HttpRequest.POST;
            return this;
        }

        public HttpRequestBuilder POST(BodyPublishers publishers) {
            this.method = HttpRequest.POST;
            this.publishers = publishers;
            return this;
        }

        public HttpRequestBuilder PUT() {
            this.method = HttpRequest.PUT;
            return this;
        }

        public HttpRequestBuilder PATCH() {
            this.method = HttpRequest.PATCH;
            return this;
        }

        public HttpRequestBuilder HEAD() {
            this.method = HttpRequest.HEAD;
            return this;
        }

        public HttpRequestBuilder body(BodyPublishers publishers) {
            this.publishers = publishers;
            return this;
        }

        public HttpRequest build() {
            HttpRequest request = new HttpRequest();
            request.method = method;
            request.uri = uri;
            request.version = version;
            request.headers = headers;
            request.body = publishers.toByteArray();
            return request;
        }
    }

    public static HttpRequestBuilder newBuilder() {
        return new HttpRequestBuilder();
    }

    private HttpRequest() {
    }

    private String method;
    private URI uri;
    private String version;
    private Map<String,String> headers;
    private byte[] body;

    public String method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public String version() {
        return version;
    }

    public Map<String,String> headers() {
        return headers;
    }

    public byte[] body() {
        return body;
    }
}
