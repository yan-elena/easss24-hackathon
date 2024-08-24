package web;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import utils.Utils;

public class ResponseEntity {
	private static ObjectMapper mapper = new ObjectMapper();

	public static ResponseEntity type(String type) {
		return new ResponseEntity(type);
	}

	protected ResponseEntity(String type) {
		this.type = type;
	}

	public ResponseEntity(HttpResponseStatus status) {
		this.status = status;
	}

	public ResponseEntity() {}
	
	public String type="text/html";
	public String location=null;
	public HttpResponseStatus status=HttpResponseStatus.OK;
	public ByteBuf content=Unpooled.wrappedBuffer(new byte[0]);
	public int length=0;
	public Map<String, String> headers = new HashMap<>();

	public ResponseEntity status(HttpResponseStatus status) {
		this.status = status;
		return this;
	}

	public ResponseEntity body(ByteBuf content) {
		this.content = content;
		return this;
	}

	public ResponseEntity body(Object object) throws JsonProcessingException {
		return body(mapper.writeValueAsString(object));
	}

	public ResponseEntity body(String content) {
		if(content != null) {
			this.content = Utils.toByteBuf(content);
			this.length = content.length();
		} else {
			this.content = Utils.toByteBuf("");
			this.length = 0;
		}
		return this;
	}

	public ResponseEntity header(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public ResponseEntity location(String location) {
		this.location = location;
		return this;
	}

}
