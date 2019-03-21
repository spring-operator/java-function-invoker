/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.projectriff.invoker;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class FatJarNextTests {

	private TestRestTemplate rest;
	private int port = SocketUtils.findAvailableTcpPort();

	private File sampleJar;

	private JavaFunctionInvokerApplication runner;
	private File sampleDir;

	@Before
	public void init() {
		runner = new JavaFunctionInvokerApplication();
		rest = new TestRestTemplate();
	}

	@After
	public void close() throws Exception {
		if (runner != null) {
			runner.close();
		}
	}

	@Before
	public void check() {
		sampleDir = new File("target/it/pojo/target/classes");
		sampleJar = new File(
				"target/it/next/target/function-sample-next-1.0.0.BUILD-SNAPSHOT.jar");
		Assume.assumeTrue("Sample jar does not exist: " + sampleJar, sampleJar.exists());
	}

	@Test
	public void fatJar() throws Exception {
		runner.run("--server.port=" + port, 
				"--function.uri=" + sampleJar.toURI()
						+ "?handler=uppercase&main=com.example.SampleApplication");
		ResponseEntity<String> result = rest.exchange(RequestEntity
				.post(new URI("http://localhost:" + port + "/"))
				.contentType(MediaType.APPLICATION_JSON).body("{\"value\":\"foo\"}"),
				String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo("[{\"value\":\"FOO\"}]");
	}

	@Test
	public void fatJarAndDirectory() throws Exception {
		runner.run("--server.port=" + port, 
				"--function.uri=" + sampleDir.toURI() + "," + sampleJar.toURI()
						+ "?handler=uppercase&main=com.example.SampleApplication");
		ResponseEntity<String> result = rest.exchange(RequestEntity
				.post(new URI("http://localhost:" + port + "/"))
				.contentType(MediaType.APPLICATION_JSON).body("{\"value\":\"foo\"}"),
				String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo("[{\"value\":\"FOO\"}]");
	}
}
