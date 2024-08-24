package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtils {
	public static WebResponse sendRequest(RequestObject requestObject) {
		try {
			// Create Connection
			HttpURLConnection connection = (HttpURLConnection) new URL(requestObject.url).openConnection();
			connection.setRequestMethod(requestObject.method);
			connection.setRequestProperty("Content-Type", requestObject.type);
			connection.setRequestProperty("Accept", requestObject.type);
			connection.setRequestProperty("User-Agent", "ASTRA/1.0");

			// Send Message Body (if existing)
			if (requestObject.content != null) {
				connection.setDoOutput(true);
				byte[] input = requestObject.content.getBytes("utf-8");
				connection.getOutputStream().write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();

			// get response content
			// note getResponseMessage dose not retrieve the content
			String response = "";
			if (200 <= responseCode && responseCode <= 299) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					response += line;
			} else {
				System.err.println("[WebUtils] error code: "+responseCode+" returned on message: " + requestObject.method + ":"
							+ requestObject.content + " to " + requestObject.url + " with media type " + requestObject.type);
				if (connection.getErrorStream() != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
					String line;
					while ((line = reader.readLine()) != null)
						System.err.println("[WebUtils] " + line);
				}
			}
			connection.disconnect();
			return new WebResponse(responseCode, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
