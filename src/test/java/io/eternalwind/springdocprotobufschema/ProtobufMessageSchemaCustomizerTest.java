package io.eternalwind.springdocprotobufschema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@ExtendWith(MockitoExtension.class)
public class ProtobufMessageSchemaCustomizerTest implements WithAssertions {
    @Mock
    private RestEndpointCollector restEndpointCollector;

    @Mock
    private ProtobufMessageTypeCollector protobufMessageTypeCollector;

    @Mock
    private ProtobufMessageDescriptorCollector protobufMessageDescriptorCollector;

    @Mock
    private Descriptor protobufMessageDescriptor;

    private ProtobufMessageSchemaCustomizer protobufMessageSchemaCustomizer;

    @BeforeEach
    void setup() {
        @SuppressWarnings("unchecked")
        final Collection<Method> restEndpoints = mock(Collection.class);
        when(restEndpointCollector.collect()).thenReturn(restEndpoints);

        when(protobufMessageTypeCollector.collect(restEndpoints)).thenReturn(List.of(Message.class));
        when(protobufMessageDescriptorCollector.collect(Message.class)).thenReturn(List.of(protobufMessageDescriptor));
        when(protobufMessageDescriptor.getName()).thenReturn("PROTOBUF_MESSAGE");

        protobufMessageSchemaCustomizer = new ProtobufMessageSchemaCustomizer(restEndpointCollector,
                protobufMessageTypeCollector, protobufMessageDescriptorCollector);
    }

    @Test
    void testCanCustomiseEnumListFieldSchema() {
        final FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getType()).thenReturn(Type.ENUM);
        when(field.isRepeated()).thenReturn(true);
        when(field.getName()).thenReturn("ENUM_LIST");

        final Object defaultValue = mock(Object.class);
        when(field.getDefaultValue()).thenReturn(defaultValue);

        final EnumDescriptor enumDescriptor = mock(EnumDescriptor.class);
        when(field.getEnumType()).thenReturn(enumDescriptor);

        when(protobufMessageDescriptor.getFields()).thenReturn(List.of(field));

        final EnumValueDescriptor enumValueDescriptor = mock(EnumValueDescriptor.class);
        when(enumValueDescriptor.getName()).thenReturn("ENUM_VALUE");
        when(enumDescriptor.getValues()).thenReturn(List.of(enumValueDescriptor));

        final ArraySchema enumListSchema = new ArraySchema();

        final Schema messageSchema = mock(Schema.class);
        when(messageSchema.getName()).thenReturn("PROTOBUF_MESSAGE");
        when(messageSchema.getProperties()).thenReturn(Map.of("ENUM_LIST", enumListSchema));

        final OpenAPI openApi = mock(OpenAPI.class);
        final Components components = mock(Components.class);
        when(openApi.getComponents()).thenReturn(components);
        when(components.getSchemas()).thenReturn(Map.of("PROTOBUF_MESSAGE", messageSchema));

        protobufMessageSchemaCustomizer.customise(openApi);

        assertThat(enumListSchema.getDefault()).isEqualTo(defaultValue);

        final Schema enumSchema = enumListSchema.getItems();
        assertThat(enumSchema.getEnum()).containsExactlyInAnyOrder(enumValueDescriptor.getName());
    }
    
    @Test
    void testCanCustomiseEnumFieldSchema() {
        final FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getType()).thenReturn(Type.ENUM);
        when(field.isRepeated()).thenReturn(false);
        when(field.getName()).thenReturn("ENUM");

        final String defaultValue = "DEFAULT";
        when(field.getDefaultValue()).thenReturn(defaultValue);

        final EnumDescriptor enumDescriptor = mock(EnumDescriptor.class);
        when(field.getEnumType()).thenReturn(enumDescriptor);

        when(protobufMessageDescriptor.getFields()).thenReturn(List.of(field));

        final EnumValueDescriptor enumValueDescriptor = mock(EnumValueDescriptor.class);
        when(enumValueDescriptor.getName()).thenReturn("ENUM_VALUE");
        when(enumDescriptor.getValues()).thenReturn(List.of(enumValueDescriptor));

        final StringSchema enumSchema = new StringSchema();

        final Schema messageSchema = mock(Schema.class);
        when(messageSchema.getName()).thenReturn("PROTOBUF_MESSAGE");
        when(messageSchema.getProperties()).thenReturn(Map.of("ENUM", enumSchema));

        final OpenAPI openApi = mock(OpenAPI.class);
        final Components components = mock(Components.class);
        when(openApi.getComponents()).thenReturn(components);
        when(components.getSchemas()).thenReturn(Map.of("PROTOBUF_MESSAGE", messageSchema));

        protobufMessageSchemaCustomizer.customise(openApi);

        assertThat(enumSchema.getDefault()).isEqualTo(defaultValue);
        assertThat(enumSchema.getEnum()).containsExactlyInAnyOrder(enumValueDescriptor.getName());
    }

    @Test
    void testCanCustomiseStringListFieldSchema() {
        final FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getType()).thenReturn(Type.STRING);
        when(field.isRepeated()).thenReturn(true);
        when(field.getName()).thenReturn("STRING_LIST");

        final Object defaultValue = mock(Object.class);
        when(field.getDefaultValue()).thenReturn(defaultValue);

        when(protobufMessageDescriptor.getFields()).thenReturn(List.of(field));

        final ArraySchema stringListSchema = new ArraySchema();

        final Schema messageSchema = mock(Schema.class);
        when(messageSchema.getName()).thenReturn("PROTOBUF_MESSAGE");
        when(messageSchema.getProperties()).thenReturn(Map.of("STRING_LIST", stringListSchema));

        final OpenAPI openApi = mock(OpenAPI.class);
        final Components components = mock(Components.class);
        when(openApi.getComponents()).thenReturn(components);
        when(components.getSchemas()).thenReturn(Map.of("PROTOBUF_MESSAGE", messageSchema));

        protobufMessageSchemaCustomizer.customise(openApi);

        assertThat(stringListSchema.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    void testCanCustomiseStringFieldSchema() {
        final FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.getType()).thenReturn(Type.STRING);
        when(field.isRepeated()).thenReturn(false);
        when(field.getName()).thenReturn("STRING");

        final String defaultValue = "DEFAULT";
        when(field.getDefaultValue()).thenReturn(defaultValue);

        when(protobufMessageDescriptor.getFields()).thenReturn(List.of(field));

        final StringSchema stringSchema = new StringSchema();

        final Schema messageSchema = mock(Schema.class);
        when(messageSchema.getName()).thenReturn("PROTOBUF_MESSAGE");
        when(messageSchema.getProperties()).thenReturn(Map.of("STRING", stringSchema));

        final OpenAPI openApi = mock(OpenAPI.class);
        final Components components = mock(Components.class);
        when(openApi.getComponents()).thenReturn(components);
        when(components.getSchemas()).thenReturn(Map.of("PROTOBUF_MESSAGE", messageSchema));

        protobufMessageSchemaCustomizer.customise(openApi);

        assertThat(stringSchema.getDefault()).isEqualTo(defaultValue);
    }
}
