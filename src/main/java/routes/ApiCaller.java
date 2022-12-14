package routes;

import model.ApiResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiCaller extends RouteBuilder {

    @Value("${API_KEY}")
    private String apiKey;

    private final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=London,uk&appid=";

    @Override
    public void configure() throws Exception {

        from("timer:my-restapi-consumer?period=15s")
            .toD(API_URL + apiKey)

            .wireTap("log:api-response")

            .unmarshal()
            .json(JsonLibrary.Jackson, ApiResponse.class)
            .process("apiResponseProcessor")

            .wireTap("log:weather-data")

            .multicast().parallelProcessing()
                .to("kafka://weather-topic")
                .process("weatherSaveProcessor")
            .end();
    }
}
