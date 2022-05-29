package io.eternalwind.springdocprotobufschema;

import java.util.Set;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(RestEndpointCollectorTest.TestController.class)
public class RestEndpointCollectorTest implements WithAssertions {

    @SpringBootApplication
    @NoArgsConstructor
    private static class TestApplication {
    }

    @RestController
    public static class TestController {
        @PostMapping
        public void testEndpoint() {}
    }
    

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    @SneakyThrows
    void testCanCollectMatchingEndpoint() {
        final RestEndpointCollector collector = new RestEndpointCollector(handlerMapping, Set.of(RequestMethod.POST));

        assertThat(collector.collect()).containsExactly(TestController.class.getMethod("testEndpoint"));
    }

    @Test
    void testCollectNothingWhenNoMatchingEndpoint() {
        final RestEndpointCollector collector = new RestEndpointCollector(handlerMapping, Set.of(RequestMethod.PUT));

        assertThat(collector.collect()).isEmpty();
    }
}
