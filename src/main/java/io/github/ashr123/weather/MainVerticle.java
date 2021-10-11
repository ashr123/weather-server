package io.github.ashr123.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ashr123.weather.utils.City;
import io.github.ashr123.weather.utils.Coordinates;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.OptionalInt;

public class MainVerticle extends AbstractVerticle
{
	private static final SimpleDateFormat DATE_FORMATTER_FOR_PRINTING = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static String API_KEY;
	private final Collection<City> citiesList;

	public MainVerticle() throws IOException
	{
		citiesList = JSON_MAPPER.readValue(getClass().getClassLoader().getResourceAsStream("city.list.json"), new TypeReference<>()
		{
		});
	}

	/**
	 * @param args {@code args[0]} must be the API key for the OpenWeather account
	 * @throws IOException if there is a problem with reading city.list.json file
	 * @see <a href="https://home.openweathermap.org/api_keys">https://home.openweathermap.org/api_keys</a>
	 */
	public static void main(String... args) throws IOException
	{
		MainVerticle.API_KEY = args[0];
		System.out.println("Generating dates strings for zone: " + DATE_FORMATTER_FOR_PRINTING.getTimeZone().getID());
//		DATE_FORMATTER_FOR_PRINTING.setTimeZone(TimeZone.getTimeZone("UTC"));
		Vertx.vertx().deployVerticle(new MainVerticle());
	}

	@Override
	public void start()
	{
		// Create a Router
		final Router router = Router.router(vertx);

		// Mount the handler for all incoming requests at every path and HTTP method
		router.get("/healthcheck")
			.handler(context -> context.json("I'm alive!!!"));
		router.get("/hello")
			.handler(context -> context.json("Hello " + context.queryParams().get("name") + "!"));
		router.get("/currentforcasts")
			.handler(context ->
			{
				final OptionalInt cityId = citiesList.parallelStream()
					.filter(city -> city.name().equalsIgnoreCase(context.queryParams().get("city")) &&
						city.country().equalsIgnoreCase(context.queryParams().get("country")))
					.mapToInt(City::id)
					.findAny();
				if (cityId.isPresent())
					WebClient.create(vertx)
						.get("api.openweathermap.org", "/data/2.5/weather?id=" + cityId.getAsInt() + "&appid=" + API_KEY)
						.putHeader("Accept", "application/json")
						.as(BodyCodec.jsonObject())
						.expect(ResponsePredicate.SC_OK)
						.send(handler -> context.json(handler.succeeded() ?
							new JsonObject()
								.put(
									"city",
									handler.result()
										.body()
										.getString("name")
								)
								.put(
									"country",
									handler.result()
										.body()
										.getJsonObject("sys")
										.getString("country")
								)
								.put(
									"temp",
									handler.result()
										.body()
										.getJsonObject("main")
										.getDouble("temp")
								)
								.put(
									"humidity",
									handler.result()
										.body()
										.getJsonObject("main")
										.getDouble("humidity")
								)
								.put(
									"date",
									DATE_FORMATTER_FOR_PRINTING.format(new Date(handler.result()
										.body()
										.getLong("dt") * 1000))
								) :
							"Error: " + handler.cause().toString()));
				else
					context.json("City not found!");
			});
		router.get("/forecasts")
			.handler(context ->
			{
				final Optional<Coordinates> coordinates = citiesList.parallelStream()
					.filter(city -> city.name().equalsIgnoreCase(context.queryParams().get("city")) &&
						city.country().equalsIgnoreCase(context.queryParams().get("country")))
					.map(City::coord)
					.findAny();
				if (coordinates.isPresent())
					WebClient.create(vertx)
						.get("api.openweathermap.org", "/data/2.5/onecall?lat=" + coordinates.get().lat() + "&lon=" + coordinates.get().lon() + "&exclude=current,minutely,hourly,alerts&appid=" + API_KEY)
						.putHeader("Accept", "application/json")
						.as(BodyCodec.jsonObject())
						.expect(ResponsePredicate.SC_OK)
						.send(handler ->
						{
							if (handler.succeeded())
							{
								final long days = Long.parseLong(context.queryParams().get("days"));
								context.json(days < 6 ?
									new JsonObject()
										.put(
											"forecasts",
											new JsonArray(handler.result()
												.body()
												.getJsonArray("daily").stream()
												.limit(days)
												.map(JsonObject.class::cast)
												.map(jsonObject -> new JsonObject()
													.put(
														"date",
														DATE_FORMATTER_FOR_PRINTING.format(new Date(jsonObject.getLong("dt") * 1000))
													)
													.put(
														"dayTemp",
														jsonObject.getJsonObject("temp")
															.getDouble("day")
													)
													.put(
														"minTemp",
														jsonObject.getJsonObject("temp")
															.getDouble("min")
													)
													.put(
														"maxTemp",
														jsonObject.getJsonObject("temp")
															.getDouble("max")
													))
												.toList())
										) :
									"Error: max days allowed are 5, got " + days);
							} else
								context.json("Error: " + handler.cause().toString());
						});
				else
					context.json("City not found!");
			});

		// Create the HTTP server
		vertx.createHttpServer()
			// Handle every request using the router
			.requestHandler(router)
			// Start listening
			.listen(8080)
			// Print the port
			.onSuccess(server -> System.out.println("HTTP server started on port " + server.actualPort()));
	}
}
