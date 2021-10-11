package io.github.ashr123.weather;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

@ExtendWith(VertxExtension.class)
public class TestSampleVerticle
{
	@BeforeEach
	void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws IOException
	{
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
	}

	@Test
	void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable
	{
		testContext.completeNow();
	}
}
