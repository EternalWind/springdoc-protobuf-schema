package io.eternalwind.springdocprotobufschema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProtobufMessageDescriptorFinderTest implements WithAssertions {
    private static interface MessageWithoutNestedMessage extends Message {
        static Descriptor getDescriptor() {
            return descriptorWithoutNestedMessage;
        }
    }

    private static interface MessageWithNestedMessage extends Message {
        static Descriptor getDescriptor() {
            return descriptorWithNestedMessage;
        }
    }

    private static interface InvalidMessage extends Message {}

    @Mock
    private static Descriptor descriptorWithoutNestedMessage;

    @Mock
    private static Descriptor descriptorWithNestedMessage;

    private ProtobufMessageDescriptorCollector collector;

    @BeforeEach
    void setup() {
        collector = new ProtobufMessageDescriptorCollector();
    }

    @Test
    void testCanCollectNonNestedMessageDescriptors() {
        when(descriptorWithoutNestedMessage.getFields()).thenReturn(List.of());

        final Collection<Descriptor> actual = collector.collect(MessageWithoutNestedMessage.class);

        assertThat(actual).containsExactly(descriptorWithoutNestedMessage);
    }

    @Test
    void testCanCollectNestedMessageDescriptors() {
        when(descriptorWithoutNestedMessage.getFields()).thenReturn(List.of());

        final FieldDescriptor field = mock(FieldDescriptor.class);
        when(descriptorWithNestedMessage.getFields()).thenReturn(List.of(field));
        when(field.getType()).thenReturn(Type.MESSAGE);
        when(field.getMessageType()).thenReturn(descriptorWithoutNestedMessage);
        
        final Collection<Descriptor> actual = collector.collect(MessageWithNestedMessage.class);

        assertThat(actual).containsExactlyInAnyOrder(descriptorWithoutNestedMessage, descriptorWithNestedMessage);
    }

    @Test
    void testThrowExceptionWhenCannotFindDescriptor() {
        assertThatThrownBy(() -> collector.collect(InvalidMessage.class)).isInstanceOf(CannotFindDescriptorException.class);
    }
}
