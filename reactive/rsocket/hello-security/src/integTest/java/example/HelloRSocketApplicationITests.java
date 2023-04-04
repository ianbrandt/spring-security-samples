/*
 * Copyright 2002-2019 the original author or authors.
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
package example;

import io.rsocket.exceptions.RejectedSetupException;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integration tests for the rsocket application.
 *
 * @author Rob Winch
 * @author Eddú Meléndez
 * @since 5.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.rsocket.server.port=0")
class HelloRSocketApplicationITests {

	@Autowired
	RSocketRequester.Builder requester;

	// @LocalRSocketServerPort
	@Value("${local.rsocket.server.port}")
	int port;

	@Test
	void messageWhenAuthenticatedThenSuccess() {
		UsernamePasswordMetadata credentials =
				new UsernamePasswordMetadata("user", "password");

		RSocketRequester requester = this.requester
				.rsocketStrategies(builder ->
						builder.encoder(new SimpleAuthenticationEncoder())
				)
				.setupMetadata(
						credentials,
						MimeTypeUtils.parseMimeType(
								WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()
						)
				)
				.tcp("localhost", this.port);

		String message = requester
				.route("message")
				.data(Mono.empty())
				.retrieveMono(String.class)
				.block();

		assertThat(message).isEqualTo("Hello");
	}

	@Test
	void messageWhenNotAuthenticatedThenError() {
		UsernamePasswordMetadata invalidCredentials =
				new UsernamePasswordMetadata("invalid user", "bad password");

		RSocketRequester requester = this.requester
				.rsocketStrategies(builder ->
						builder.encoder(new SimpleAuthenticationEncoder())
				)
				.setupMetadata(
						invalidCredentials,
						MimeTypeUtils.parseMimeType(
								WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()
						)
				)
				.tcp("localhost", this.port);

		assertThatExceptionOfType(RejectedSetupException.class).isThrownBy(() ->
				requester.route("message")
						.data(Mono.empty())
						.retrieveMono(String.class)
						.block()
		).withMessageContaining("Invalid Credentials");
	}
}
