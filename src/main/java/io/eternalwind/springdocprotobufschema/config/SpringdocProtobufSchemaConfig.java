package io.eternalwind.springdocprotobufschema.config;

import java.util.Set;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.eternalwind.springdocprotobufschema.ProtobufMessageDescriptorCollector;
import io.eternalwind.springdocprotobufschema.ProtobufMessageSchemaCustomizer;
import io.eternalwind.springdocprotobufschema.ProtobufMessageTypeCollector;
import io.eternalwind.springdocprotobufschema.RestEndpointCollector;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@ConditionalOnClass({ RequestMappingHandlerMapping.class, OpenAPI.class })
public class SpringdocProtobufSchemaConfig {
    @Bean
    @ConditionalOnMissingBean
    public RestEndpointCollector restEndpointCollector(final RequestMappingHandlerMapping handlerMapping) {
        return new RestEndpointCollector(handlerMapping, Set.of(
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH));
    }
    
    @Bean
    public ProtobufMessageTypeCollector protobufMessageTypeCollector() {
        return new ProtobufMessageTypeCollector();
    }

    @Bean
    public ProtobufMessageDescriptorCollector protobufMessageDescriptorCollector() {
        return new ProtobufMessageDescriptorCollector();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalOpenApiCustomizer protobufMessageScheCustomizer(final RestEndpointCollector restEndpointCollector,
                                                                 final ProtobufMessageTypeCollector protobufMessageTypeCollector,
                                                                 final ProtobufMessageDescriptorCollector protobufMessageDescriptorCollector) {
        return new ProtobufMessageSchemaCustomizer(restEndpointCollector, protobufMessageTypeCollector, protobufMessageDescriptorCollector);
    }
}
