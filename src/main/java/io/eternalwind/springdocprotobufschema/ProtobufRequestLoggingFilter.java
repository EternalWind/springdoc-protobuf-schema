package io.eternalwind.springdocprotobufschema;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import lombok.SneakyThrows;


public class ProtobufRequestLoggingFilter extends CommonsRequestLoggingFilter {
    private static final String PARSE_FROM = "parseFrom";
    private static final String APPLICATION_PROTOBUF = "application/x-protobuf";

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final JsonFormat.Printer protobufPrinter;

    public ProtobufRequestLoggingFilter(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.protobufPrinter = JsonFormat.printer();
    }

    @Override
    @SneakyThrows
    protected String getMessagePayload(HttpServletRequest request) {
        final String originalMessage = super.getMessagePayload(request);

        if (request.getContentType().contains(APPLICATION_PROTOBUF)) {
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }

            return Optional.ofNullable(originalMessage)
                    .flatMap(msg -> toProtobufMessage(request, msg))
                    .map(this::printProtobufMessage)
                    .orElse(null);
        } else {
            return originalMessage;
        }
    }

    @SneakyThrows
    private Optional<Class<?>> getProtobufMessageType(HttpServletRequest request) {
        return Optional.ofNullable(requestMappingHandlerMapping.getHandler(request))
                .flatMap(handlerExecChain -> getProtobufMessageType((HandlerMethod) handlerExecChain.getHandler()));
    }

    private Optional<Class<?>> getProtobufMessageType(HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        return Stream.of(method.getParameterTypes())
                .filter(type -> Message.class.isAssignableFrom(type))
                .findFirst();
    }

    @SneakyThrows
    private String printProtobufMessage(Message message) {
        return protobufPrinter.print(message);
    }

    @SneakyThrows
    private Optional<Message> toProtobufMessage(HttpServletRequest request, String originalMessage) {
        final byte[] bytes = originalMessage.getBytes();

        return getProtobufMessageType(request)
                .map(type -> toProtobufMessage(type, bytes));
    }

    @SneakyThrows
    private Message toProtobufMessage(Class<?> messageType, byte[] bytes) {
        final Method parseFrom = messageType.getMethod(PARSE_FROM, byte[].class);
        return (Message) parseFrom.invoke(null, bytes);
    }

    @SneakyThrows
    private Message unpack(Any anyMessage, Class<? extends Message> type) {
        return anyMessage.unpack(type);
    }
}
