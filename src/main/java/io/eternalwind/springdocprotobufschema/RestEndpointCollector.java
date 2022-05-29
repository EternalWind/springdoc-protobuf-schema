package io.eternalwind.springdocprotobufschema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestEndpointCollector {
    private final RequestMappingHandlerMapping handlerMapping;
    private final Set<RequestMethod> requestMethodsToConsider;

    public Collection<Method> collect() {
        return handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(this::isCandidateEndpoint)
                .map(t -> t.getValue().getMethod())
                .collect(Collectors.toSet());
    }

    private boolean isCandidateEndpoint(final Entry<RequestMappingInfo, HandlerMethod> endpoint) {
        return !Sets.intersection(requestMethodsToConsider, endpoint.getKey().getMethodsCondition().getMethods())
                .isEmpty();
    }
}
