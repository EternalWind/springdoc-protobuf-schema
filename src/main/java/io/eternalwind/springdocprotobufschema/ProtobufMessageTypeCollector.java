package io.eternalwind.springdocprotobufschema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.google.protobuf.Message;

public class ProtobufMessageTypeCollector {
    public Collection<Class<?>> collect(final Collection<Method> methods) {
        return methods.stream()
                .flatMap(method -> getDeclaredTypes(method).stream())
                .filter(this::isProtobufMessageType)
                .collect(Collectors.toSet());
    }

    private Collection<Class<?>> getDeclaredTypes(final Method method) {
        return Streams.concat(Stream.of(method.getParameterTypes()), Stream.of(method.getReturnType()))
                .collect(Collectors.toSet());
    }

    private boolean isProtobufMessageType(final Class<?> type) {
        return Message.class.isAssignableFrom(type);
    }
}
