package web;

public class HttpException extends RuntimeException {
    public HttpException(String msg, Throwable th) {
        super(msg, th);
    }
}