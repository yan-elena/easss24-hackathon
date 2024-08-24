package web;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface Handler {
    void handle(ChannelHandlerContext ctx, FullHttpRequest request, Path path) throws Exception;
}