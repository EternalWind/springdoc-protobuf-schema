package io.eternalwind.springdocprotobufschema;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProtobufMessageSchemaCustomizer implements GlobalOpenApiCustomizer {
    private final RestEndpointCollector restEndpointCollector;
    private final ProtobufMessageTypeCollector protobufMessageTypeCollector;
    private final ProtobufMessageDescriptorCollector protobufMessageDescriptorCollector;

    private Map<String, Descriptor> protobufMessageDescriptorMapping = null;

    @Override
    public void customise(final OpenAPI openApi) {
        if (protobufMessageDescriptorMapping == null) {
            protobufMessageDescriptorMapping = createProtobufMessageDescriptorMapping();
        }

        openApi.getComponents().getSchemas().values().stream()
                .filter(this::isProtobufMessageSchema)
                .forEach(this::customise);
    }
    
    private void customise(final Schema schema) {
        final Descriptor descriptorForSchema = protobufMessageDescriptorMapping.get(schema.getName());
        final Collection<FieldDescriptor> fields = descriptorForSchema.getFields();

        final Map<String, Schema> schemasForFields = schema.getProperties();

        fields.stream()
                .filter(this::isEnumListField)
                .forEach(field -> customiseEnumListFieldSchema(schemasForFields.get(field.getName()), field));

        fields.stream()
                .filter(this::isEnumField)
                .forEach(field -> customiseEnumFieldSchema(schemasForFields.get(field.getName()), field));

        fields.stream()
                .filter(this::isStringListField)
                .forEach(field -> customiseStringListFieldSchema(schemasForFields.get(field.getName()), field));

        fields.stream()
                .filter(this::isStringField)
                .forEach(field -> customiseStringFieldSchema(schemasForFields.get(field.getName()), field));
    }

    private Map<String, Descriptor> createProtobufMessageDescriptorMapping() {
        final Collection<Method> restEndpoints = restEndpointCollector.collect();

        return protobufMessageTypeCollector.collect(restEndpoints).stream()
                .flatMap(type -> protobufMessageDescriptorCollector.collect(type).stream())
                .collect(Collectors.toMap(Descriptor::getName, Function.identity()));
    }

    private boolean isProtobufMessageSchema(final Schema schema) {
        return protobufMessageDescriptorMapping.containsKey(schema.getName());
    }

    private boolean isEnumListField(final FieldDescriptor field) {
        return field.getType().equals(Type.ENUM) && field.isRepeated();
    }

    private void customiseEnumListFieldSchema(final Schema schema, final FieldDescriptor field) {
        final List<String> enumValues = field.getEnumType().getValues().stream()
                .map(EnumValueDescriptor::getName)
                .toList();

        final StringSchema enumSchema = new StringSchema();
        enumSchema.setEnum(enumValues);

        schema.setItems(enumSchema);
        schema.setDefault(field.getDefaultValue());
    }

    private boolean isEnumField(final FieldDescriptor field) {
        return field.getType().equals(Type.ENUM) && !field.isRepeated();
    }

    private void customiseEnumFieldSchema(final Schema schema, final FieldDescriptor field) {
        final List<String> enumValues = field.getEnumType().getValues().stream()
                .map(EnumValueDescriptor::getName)
                .toList();

        schema.setEnum(enumValues);
        schema.setDefault(field.getDefaultValue());
    }

    private boolean isStringListField(final FieldDescriptor field) {
        return field.getType().equals(Type.STRING) && field.isRepeated();
    }

    private void customiseStringListFieldSchema(final Schema schema, final FieldDescriptor field) {
        schema.setDefault(field.getDefaultValue());
    }

    private boolean isStringField(final FieldDescriptor field) {
        return field.getType().equals(Type.STRING) && !field.isRepeated();
    }

    private void customiseStringFieldSchema(final Schema schema, final FieldDescriptor field) {
        schema.setDefault(field.getDefaultValue());
    }
}
