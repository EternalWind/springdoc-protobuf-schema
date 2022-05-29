package io.eternalwind.springdocprotobufschema;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;

public class ProtobufMessageDescriptorCollector {
    private static final String GET_DESCRIPTOR_METHOD = "getDescriptor";

    public Collection<Descriptor> collect(final Class<?> type) {
        final Descriptor topLevelDescriptor = getDescriptor(type);

        return addNested(topLevelDescriptor);
    }

    private Collection<Descriptor> addNested(final Descriptor descriptor) {
        final Stream<Descriptor> nested = descriptor.getFields().stream()
                .filter(this::isMessageField)
                .map(FieldDescriptor::getMessageType)
                .flatMap(field -> addNested(field).stream());

        return Streams.concat(nested, Stream.of(descriptor)).collect(Collectors.toSet());
    }

    private Descriptor getDescriptor(final Class<?> type) {
        if (!Message.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not a protobuf Message type");
        }

        try {
            return (Descriptor) type.getMethod(GET_DESCRIPTOR_METHOD).invoke(null);
        } catch (final Exception e) {
            throw new CannotFindDescriptorException(e);
        }
    }

    private boolean isMessageField(final FieldDescriptor field) {
        return field.getType().equals(Type.MESSAGE);
    }
}
