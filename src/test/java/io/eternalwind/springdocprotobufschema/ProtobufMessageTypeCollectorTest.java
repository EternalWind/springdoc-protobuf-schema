package io.eternalwind.springdocprotobufschema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.Message;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ProtobufMessageTypeCollectorTest implements WithAssertions {
    private static interface TestMessage extends Message {
    }

    private ProtobufMessageTypeCollector collector;

    @BeforeEach
    void setup() {
        collector = new ProtobufMessageTypeCollector();
    }

    @Test
    @SneakyThrows
    void testCanCollectProtobufMessageTypeFromParameter() {
        final Object testController = new Object() {
            public void testEndpoint(final TestMessage arg) {}
        };

        final Method testEndpoint = testController.getClass().getMethod("testEndpoint", TestMessage.class);
        final Collection<Class<?>> actual = collector.collect(List.of(testEndpoint));
        
        assertThat(actual).containsExactly(TestMessage.class);
    }

    @Test
    @SneakyThrows
    void testCanCollectProtobufMessageTypeFromReturnValue() {
        final Object testController = new Object() {
            public TestMessage testEndpoint() {
                return null;
            }
        };

        final Method testEndpoint = testController.getClass().getMethod("testEndpoint");
        final Collection<Class<?>> actual = collector.collect(List.of(testEndpoint));

        assertThat(actual).containsExactly(TestMessage.class);
    }

    @Test
    @SneakyThrows
    void testWillNotCollectDuplicatedMessageTypes() {
        final Object testController = new Object() {
            public TestMessage testEndpoint1(final TestMessage arg1, final TestMessage arg2) {
                return null;
            }

            public TestMessage testEndpoint2(final TestMessage arg1, final TestMessage arg2) {
                return null;
            }
        };

        final Method testEndpoint1 = testController.getClass().getMethod("testEndpoint1", TestMessage.class,
                TestMessage.class);
        final Method testEndpoint2 = testController.getClass().getMethod("testEndpoint2", TestMessage.class,
                TestMessage.class);
        final Collection<Class<?>> actual = collector.collect(List.of(testEndpoint1, testEndpoint2));

        assertThat(actual).containsExactly(TestMessage.class);
    }

    @Test
    @SneakyThrows
    void testWillNotCollectNonMessageTypes() {
        final Object testController = new Object() {
            public int testEndpoint(final String arg1, final Object arg2) {
                return 0;
            }
        };

        final Method testEndpoint = testController.getClass().getMethod("testEndpoint", String.class,
                Object.class);
        final Collection<Class<?>> actual = collector.collect(List.of(testEndpoint));

        assertThat(actual).isEmpty();
    }
}
