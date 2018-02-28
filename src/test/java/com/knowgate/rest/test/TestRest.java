package com.knowgate.rest.test;

import java.io.IOException;

import java.net.URISyntaxException;
	
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.net.httpserver.HttpServer;

import com.knowgate.rest.Application;

public class TestRest {

	private static HttpServer server;

	@BeforeClass
	public static void init() throws IllegalArgumentException, IOException {
		Application.start(Application.DEFAULT_BASE_URI);
	}

	@AfterClass
	public static void clean() {
		Application.stop();
	}

	@Test
	public void testHello() throws IOException, InterruptedException, URISyntaxException {
		HttpGetRequest hello = new HttpGetRequest(Application.DEFAULT_BASE_URI + "hello/");
		assertEquals("Hello World!", hello.src());
	}
}