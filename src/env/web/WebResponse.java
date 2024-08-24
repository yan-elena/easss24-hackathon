package web;

public class WebResponse {
	private int code;
	private String content;
	
	public WebResponse(int code, String content) {
		this.code = code;
		this.content = content;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getContent() {
		return content;
	}
}
