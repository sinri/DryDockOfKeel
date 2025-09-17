package io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

/**
 * Aliyun SLS Protobuf Definitions
 *
 * @since 2.1.0
 */
public final class LogEntityDescriptors {
    private static final LogEntityDescriptors instance = new LogEntityDescriptors();
    private final Descriptors.Descriptor logDescriptor;
    private final Descriptors.Descriptor contentDescriptor;
    private final Descriptors.Descriptor logTagDescriptor;
    private final Descriptors.Descriptor logGroupDescriptor;
    @Deprecated
    private final Descriptors.Descriptor logGroupListDescriptor;

    private LogEntityDescriptors() {
        try {
            DescriptorProtos.DescriptorProto contentDescriptorProto = createContentDescriptorProto();
            DescriptorProtos.DescriptorProto logDescriptorProto = createLogDescriptorProto(contentDescriptorProto);
            DescriptorProtos.DescriptorProto logTagDescriptorProto = createLogTagDescriptorProto();
            DescriptorProtos.DescriptorProto logGroupDescriptorProto = createLogGroupDescriptorProto();
            DescriptorProtos.DescriptorProto logGroupListDescriptorProto = createLogGroupListDescriptorProto();
            // 创建文件描述符
            var fileDescriptorProto = DescriptorProtos.FileDescriptorProto
                    .newBuilder()
                    .setName("log.proto")
                    .setSyntax("proto2")
                    .addMessageType(logDescriptorProto)
                    .addMessageType(logTagDescriptorProto)
                    .addMessageType(logGroupDescriptorProto)
                    .addMessageType(logGroupListDescriptorProto)
                    .build();

            var fileDescriptor = Descriptors.FileDescriptor
                    .buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[0]);
            // 获取各个消息类型的描述符
            logDescriptor = fileDescriptor.findMessageTypeByName("Log");
            contentDescriptor = logDescriptor.findNestedTypeByName("Content");
            logTagDescriptor = fileDescriptor.findMessageTypeByName("LogTag");
            logGroupDescriptor = fileDescriptor.findMessageTypeByName("LogGroup");
            logGroupListDescriptor = fileDescriptor.findMessageTypeByName("LogGroupList");
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public static LogEntityDescriptors getInstance() {
        return instance;
    }

    /**
     * 创建 Content 嵌套消息描述符
     */
    private DescriptorProtos.DescriptorProto createContentDescriptorProto() {
        return DescriptorProtos.DescriptorProto
                .newBuilder()
                .setName("Content")
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Key")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED)
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Value")
                        .setNumber(2)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED)
                        .build())
                .build();
    }

    /**
     * 创建 Log 消息描述符
     */
    private DescriptorProtos.DescriptorProto createLogDescriptorProto(DescriptorProtos.DescriptorProto contentDescriptor) {
        return DescriptorProtos.DescriptorProto
                .newBuilder()
                .setName("Log")
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Time")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED)
                        .setJsonName("Time")
                        .setDefaultValue("0")
                        .build())
                .addNestedType(contentDescriptor)
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Contents")
                        .setNumber(2)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED)
                        .setTypeName(".Log.Content")
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Time_ns")
                        .setNumber(4)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                        .setJsonName("Time_ns")
                        .build())
                .build();
    }

    /**
     * 创建 LogTag 消息描述符
     */
    private DescriptorProtos.DescriptorProto createLogTagDescriptorProto() {
        return DescriptorProtos.DescriptorProto
                .newBuilder()
                .setName("LogTag")
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Key")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED)
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Value")
                        .setNumber(2)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED)
                        .build())
                .build();
    }

    /**
     * 创建 LogGroup 消息描述符
     */
    private DescriptorProtos.DescriptorProto createLogGroupDescriptorProto() {
        return DescriptorProtos.DescriptorProto
                .newBuilder()
                .setName("LogGroup")
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Logs")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED)
                        .setTypeName(".Log")
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Topic")
                        .setNumber(3)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("Source")
                        .setNumber(4)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                        .build())
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("LogTags")
                        .setNumber(6)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED)
                        .setTypeName(".LogTag")
                        .build())
                .build();
    }

    /**
     * 创建 LogGroupList 消息描述符
     */
    private DescriptorProtos.DescriptorProto createLogGroupListDescriptorProto() {
        return DescriptorProtos.DescriptorProto
                .newBuilder()
                .setName("LogGroupList")
                .addField(DescriptorProtos.FieldDescriptorProto
                        .newBuilder()
                        .setName("logGroupList")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED)
                        .setTypeName(".LogGroup")
                        .build())
                .build();

    }

    public Descriptors.Descriptor getLogTagDescriptor() {
        return logTagDescriptor;
    }

    @Deprecated
    public Descriptors.Descriptor getLogGroupListDescriptor() {
        return logGroupListDescriptor;
    }

    public Descriptors.Descriptor getLogGroupDescriptor() {
        return logGroupDescriptor;
    }

    public Descriptors.Descriptor getContentDescriptor() {
        return contentDescriptor;
    }

    public Descriptors.Descriptor getLogDescriptor() {
        return logDescriptor;
    }
}
