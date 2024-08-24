package web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
/**
 * The WebServer class is a convenience wrapper around the Netty HTTP server.
 */
public class WebServer {
	public static final String TYPE_PLAIN = "text/plain; charset=UTF-8";
	public static final String TYPE_HTML = "text/html; charset=UTF-8";
	public static final String TYPE_JSON = "application/json; charset=UTF-8";
	public static final String SERVER_NAME = "Netty";

	// Routing Table
	private final Map<String, Handler> routes;
	private final int port;
	private final EventLoopGroup masterGroup;
	private final EventLoopGroup slaveGroup;

	private static WebServer instance;
	
	public static WebServer getInstance() {
		return (instance != null) ? instance:getInstance(9000);
	}
			
	public static WebServer getInstance(int port) {
		if (instance == null) {
			instance = new WebServer(port);
			new Thread(new Runnable() {
				public void run() {
					try {
						instance.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		return instance;
	}
			
	private WebServer(int port) {
		this.routes = new HashMap<String, Handler>();
		this.port = port;
		masterGroup = new NioEventLoopGroup();
		slaveGroup = new NioEventLoopGroup();
	}

	public WebServer createContext(final String path, final Handler handler) {
		this.routes.put(path, handler);
		return this;
	}

	public Handler getHandler(String prefix) {
		for (Entry<String, Handler> entry : routes.entrySet()) {
			if (prefix.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		System.out.println("No handler found.");
		return null;
	}

	/**
	 * Starts the web server.
	 *
	 * @throws Exception
	 */
	public void start() throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});

		final ServerBootstrap b = new ServerBootstrap();
		b.group(masterGroup, slaveGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new WebServerInitializer())
				.option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
				.childOption(ChannelOption.SO_REUSEADDR, true);

		b.bind(new InetSocketAddress(port)).sync().channel().closeFuture().sync();
	}

	public void shutdown() {
		slaveGroup.shutdownGracefully();
		masterGroup.shutdownGracefully();
	}

	/**
	 * The Initializer class initializes the HTTP channel.
	 */
	private class WebServerInitializer extends ChannelInitializer<SocketChannel> {

		/**
		 * Initializes the channel pipeline with the HTTP response handlers.
		 *
		 * @param ch
		 *            The Channel which was registered.
		 */
        @Override
		@SuppressWarnings("deprecation")
        public void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline p = ch.pipeline();
			CorsConfig.Builder builder = new CorsConfig.Builder();
            CorsConfig corsConfig = builder.allowedRequestMethods(HttpMethod.GET, HttpMethod.POST,HttpMethod.OPTIONS, HttpMethod.PUT).allowedRequestHeaders("content-type").build();
            p.addLast("codec", new HttpServerCodec());
            p.addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
            p.addLast(new CorsHandler(corsConfig));
            p.addLast("handler", new WebServerHandler());
        }
	}

	private class WebServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
			if (HttpUtil.is100ContinueExpected(request)) send100Continue(ctx);

			System.out.println("[Web Server] Method: " + request.method() + " / Action: " + request.uri());
			
            final Path path = new Path(request.uri().substring(1));
            String prefix = "";
            try {
                prefix = "/"+path.prefix();
            } catch (Throwable th) {
                th.printStackTrace();
			}
			
			final Handler handler = WebServer.this.getHandler(prefix);
			if (handler == null) {
				writeNotFound(ctx, request);
			} else {
				try {
					// System.out.println("Handling: " + prefix);
					handler.handle(ctx, request, path);
				} catch (final Throwable ex) {
					ex.printStackTrace();
					writeInternalServerError(ctx, request);
				}
			}
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
			ctx.close();
		}

		@Override
		public void channelReadComplete(final ChannelHandlerContext ctx) {
			ctx.flush();
		}
	}

	private static void writeNotFound(final ChannelHandlerContext ctx, final FullHttpRequest request) {
		writeErrorResponse(ctx, request, HttpResponseStatus.NOT_FOUND);
	}

	private static void writeInternalServerError(final ChannelHandlerContext ctx, final FullHttpRequest request) {
		writeErrorResponse(ctx, request, HttpResponseStatus.INTERNAL_SERVER_ERROR);
	}

	public static void writeErrorResponse(final ChannelHandlerContext ctx, final FullHttpRequest request,
			final HttpResponseStatus status) {
		writeResponse(ctx, request, new ResponseEntity(TYPE_PLAIN).status(status).body(status.reasonPhrase().toString()));
	}


	/**
	 * Custom method for handling responses constructed using the active record model based on a
	 * ResponseObject.
	 * 
	 * @param ctx
	 * @param request
	 * @param responseObject
	 */
	public static void writeResponse(final ChannelHandlerContext ctx, final FullHttpRequest request,
			final ResponseEntity responseObject) {

		// Decide whether to close the connection or not.
		final boolean keepAlive = HttpUtil.isKeepAlive(request);

		// Build the response object.
		final FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, 
				responseObject.status, 
				responseObject.content, 
				false
		);

		final ZonedDateTime dateTime = ZonedDateTime.now();
		final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

		final DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
		headers.set(HttpHeaderNames.SERVER, SERVER_NAME);
		headers.set(HttpHeaderNames.DATE, dateTime.format(formatter));
		if (responseObject.location != null) headers.set(HttpHeaderNames.LOCATION, responseObject.location);
		headers.set(HttpHeaderNames.CONTENT_TYPE, responseObject.type);
		headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(responseObject.length));
		for (Entry<String, String> entry : responseObject.headers.entrySet()) {
			headers.set(entry.getKey(), entry.getValue());
		}
		// Close the non-keep-alive connection after the write operation is
		// done.
		if (!keepAlive) {
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.writeAndFlush(response, ctx.voidPromise());
		}
	}


	private static void send100Continue(final ChannelHandlerContext ctx) {
		ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
	}

	public String getBaseUrl() {
		return "http://"+getHostPort();
	}
	
	public String getHostPort() {
		try {
			return InetAddress.getLocalHost().getHostAddress()+":"+port;
		} catch (UnknownHostException e) {
			return "unknown:"+port;
		}
	}
	
	public String getHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "unknown";
		}
	}

	public int getPort() {
		return port;
	}
}