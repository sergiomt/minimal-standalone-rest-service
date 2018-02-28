package com.knowgate.rest;

import java.io.IOException;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.net.httpserver.HttpServer;
 
public class Application {

	public static final String DEFAULT_BASE_URI = "http://localhost:9999/";

	private static HttpServer server;

	public static void start(final String baseUri) throws IOException {
			final PackagesResourceConfig rc = new PackagesResourceConfig("com.knowgate.rest");
			server = HttpServerFactory.create(baseUri, rc);
			server.start();
	}

	public static void stop() {
			if (null!=server)
				server.stop(0);
	}

	public static void main(String[] args) {
		try {
			final String baseUri = args!=null && args.length>0 ? args[0] : DEFAULT_BASE_URI;
			start(baseUri);
			System.out.println("Press Enter to stop the server. ");
			System.in.read();
			stop();
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
	}

}