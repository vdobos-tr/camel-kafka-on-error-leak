package cz.trask.bi.demo.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.kafka;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.controlbus;

import org.apache.camel.LoggingLevel;

@Component
public class KafkaEndlessErrorRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from(timer("once").delay(0).repeatCount(1))
			.routeId("test-kafka-send")
			.setHeader(KafkaConstants.KEY, constant("SOME_KEY"))
			.setBody(constant("SOME_BODY"))
			.to(kafka("test-leak"))
			.to(controlbus("route").routeId("test-kafka-receive").action("start"));
		
		from(kafka("test-leak"))
			.noAutoStartup()
			.routeId("test-kafka-receive")
			.log(LoggingLevel.INFO, "MESSAGE: ${body}")
			.delay(1000).syncDelayed()
			.throwException(new Exception("Throw me up !"))
			.end();
	}

}
