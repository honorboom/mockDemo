package com.mock.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.mock.service.dto.ProtoFileRequest;
import com.mock.service.entity.ProtoFile;
import com.mock.service.repository.ProtoFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proto 核心服务
 * <p>
 * 负责 .proto 文件的编译、FileDescriptor 的解析、JSON 到 Protobuf 二进制的转换。
 * 使用内存缓存已解析的 FileDescriptor 以提升性能。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtoService {

    private final ProtoFileRepository protoFileRepository;
    private final ObjectMapper objectMapper;

    /**
     * 缓存已解析的 FileDescriptor，key = protoFileId
     */
    private final Map<Long, Descriptors.FileDescriptor> descriptorCache = new ConcurrentHashMap<>();

    /**
     * 创建并编译 Proto 文件
     */
    @Transactional
    public ProtoFile createProtoFile(ProtoFileRequest request) {
        if (protoFileRepository.existsByName(request.getName())) {
            throw new RuntimeException("Proto 名称已存在: " + request.getName());
        }

        ProtoFile protoFile = new ProtoFile();
        protoFile.setName(request.getName());
        protoFile.setFileName(request.getFileName());
        protoFile.setProtoContent(request.getProtoContent());
        protoFile.setDescription(request.getDescription());

        // 编译 .proto 文件
        compileAndFill(protoFile);

        return protoFileRepository.save(protoFile);
    }

    /**
     * 更新 Proto 文件（重新编译）
     */
    @Transactional
    public ProtoFile updateProtoFile(Long id, ProtoFileRequest request) {
        ProtoFile protoFile = protoFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proto 文件不存在"));

        if (protoFileRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Proto 名称已被其他记录使用");
        }

        protoFile.setName(request.getName());
        protoFile.setFileName(request.getFileName());
        protoFile.setProtoContent(request.getProtoContent());
        protoFile.setDescription(request.getDescription());

        // 重新编译
        compileAndFill(protoFile);

        // 清除缓存
        evictCache(id);

        return protoFileRepository.save(protoFile);
    }

    /**
     * 删除 Proto 文件
     */
    @Transactional
    public void deleteProtoFile(Long id) {
        evictCache(id);
        protoFileRepository.deleteById(id);
    }

    /**
     * 获取 Proto 文件详情
     */
    public ProtoFile getProtoFile(Long id) {
        return protoFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proto 文件不存在"));
    }

    /**
     * 获取所有 Proto 文件
     */
    public List<ProtoFile> getAllProtoFiles() {
        return protoFileRepository.findAll();
    }

    /**
     * 获取 Proto 文件的所有 message 类型
     */
    public List<String> getMessageTypes(Long id) {
        ProtoFile protoFile = getProtoFile(id);
        try {
            if (protoFile.getMessageTypes() != null) {
                return objectMapper.readValue(protoFile.getMessageTypes(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.error("解析 message 类型列表失败", e);
        }
        return List.of();
    }

    /**
     * 将 JSON 转换为 Protobuf 二进制数据
     *
     * @param protoFileId   Proto 文件 ID
     * @param messageType   Message 类型名称
     * @param jsonData      JSON 格式的响应数据
     * @return protobuf 二进制字节数组
     */
    public byte[] jsonToProtobuf(Long protoFileId, String messageType, String jsonData) {
        try {
            Descriptors.FileDescriptor fileDescriptor = getOrLoadDescriptor(protoFileId);

            // 查找 message 类型（支持包名前缀和不带包名的简短名称）
            Descriptors.Descriptor descriptor = findMessageDescriptor(fileDescriptor, messageType);
            if (descriptor == null) {
                throw new RuntimeException("未找到 message 类型: " + messageType);
            }

            log.debug("Proto message 字段定义: {}", descriptor.getFields().stream()
                    .map(f -> f.getName() + "(" + f.getJsonName() + ")")
                    .toList());
            log.debug("输入 JSON 数据: {}", jsonData);

            // 使用 JsonFormat 将 JSON 解析为 DynamicMessage
            DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
            JsonFormat.parser().ignoringUnknownFields().merge(jsonData, builder);
            DynamicMessage message = builder.build();

            byte[] result = message.toByteArray();
            if (result.length == 0) {
                log.warn("Protobuf 序列化结果为 0 bytes，可能原因：JSON 字段名与 Proto 定义不匹配。" +
                        " Proto 期望字段: {}, 输入 JSON: {}",
                        descriptor.getFields().stream().map(Descriptors.FieldDescriptor::getJsonName).toList(),
                        jsonData);
            }

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("JSON 转 Protobuf 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清除指定 Proto 文件的缓存
     */
    public void evictCache(Long protoFileId) {
        descriptorCache.remove(protoFileId);
        log.debug("已清除 Proto 缓存: id={}", protoFileId);
    }

    // ==================== 私有方法 ====================

    /**
     * 编译 .proto 文件并填充实体
     */
    private void compileAndFill(ProtoFile protoFile) {
        try {
            byte[] descriptorBytes = compileProto(protoFile.getProtoContent(),
                    protoFile.getFileName() != null ? protoFile.getFileName() : "input.proto");
            protoFile.setDescriptorBytes(descriptorBytes);

            // 提取 message 类型列表
            List<String> types = extractMessageTypes(descriptorBytes);
            protoFile.setMessageTypes(objectMapper.writeValueAsString(types));

            log.info("Proto 编译成功: name={}, messageTypes={}", protoFile.getName(), types);
        } catch (Exception e) {
            throw new RuntimeException("编译 .proto 文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 protoc-jar 编译 .proto 文件内容为 FileDescriptorSet
     */
    private byte[] compileProto(String protoContent, String fileName) throws Exception {
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("proto-compile-");
        try {
            // 写入 .proto 文件
            Path protoPath = tempDir.resolve(fileName);
            Files.writeString(protoPath, protoContent);

            // 输出的 descriptor set 文件
            Path descPath = tempDir.resolve("output.desc");

            // 调用 protoc 编译
            String[] args = {
                    "--include_std_types",
                    "--include_imports",
                    "--descriptor_set_out=" + descPath.toAbsolutePath(),
                    "--proto_path=" + tempDir.toAbsolutePath(),
                    protoPath.toAbsolutePath().toString()
            };

            int exitCode = Protoc.runProtoc(args);
            if (exitCode != 0) {
                throw new RuntimeException("protoc 编译失败，退出码: " + exitCode);
            }

            // 读取编译结果
            File descFile = descPath.toFile();
            if (!descFile.exists()) {
                throw new RuntimeException("protoc 编译未生成描述文件");
            }

            try (FileInputStream fis = new FileInputStream(descFile)) {
                return fis.readAllBytes();
            }
        } finally {
            // 清理临时文件
            deleteDirectory(tempDir.toFile());
        }
    }

    /**
     * 从 FileDescriptorSet 字节中提取所有 message 类型名称
     */
    private List<String> extractMessageTypes(byte[] descriptorBytes) throws Exception {
        DescriptorProtos.FileDescriptorSet descriptorSet =
                DescriptorProtos.FileDescriptorSet.parseFrom(descriptorBytes);

        List<String> types = new ArrayList<>();
        for (DescriptorProtos.FileDescriptorProto fileProto : descriptorSet.getFileList()) {
            // 跳过 Google well-known types
            if (fileProto.getName().startsWith("google/")) {
                continue;
            }
            for (DescriptorProtos.DescriptorProto msgProto : fileProto.getMessageTypeList()) {
                collectMessageTypes(msgProto, "", types);
            }
        }
        return types;
    }

    /**
     * 递归收集 message 类型名称（包括嵌套类型）
     */
    private void collectMessageTypes(DescriptorProtos.DescriptorProto msgProto, String prefix, List<String> types) {
        String fullName = prefix.isEmpty() ? msgProto.getName() : prefix + "." + msgProto.getName();
        types.add(fullName);

        // 递归处理嵌套 message
        for (DescriptorProtos.DescriptorProto nestedMsg : msgProto.getNestedTypeList()) {
            collectMessageTypes(nestedMsg, fullName, types);
        }
    }

    /**
     * 从缓存或数据库加载并解析 FileDescriptor
     */
    private Descriptors.FileDescriptor getOrLoadDescriptor(Long protoFileId) {
        return descriptorCache.computeIfAbsent(protoFileId, id -> {
            try {
                ProtoFile protoFile = protoFileRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Proto 文件不存在: id=" + id));

                if (protoFile.getDescriptorBytes() == null) {
                    throw new RuntimeException("Proto 文件未编译: id=" + id);
                }

                return parseDescriptor(protoFile.getDescriptorBytes());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("加载 Proto 描述符失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 从 FileDescriptorSet 字节解析为 FileDescriptor
     */
    private Descriptors.FileDescriptor parseDescriptor(byte[] descriptorBytes) throws Exception {
        DescriptorProtos.FileDescriptorSet descriptorSet =
                DescriptorProtos.FileDescriptorSet.parseFrom(descriptorBytes);

        // 构建 FileDescriptor 链（处理 import 依赖）
        List<Descriptors.FileDescriptor> resolvedDescriptors = new ArrayList<>();
        for (DescriptorProtos.FileDescriptorProto fileProto : descriptorSet.getFileList()) {
            Descriptors.FileDescriptor[] dependencies = resolveDependencies(
                    fileProto, descriptorSet, resolvedDescriptors);
            Descriptors.FileDescriptor fd = Descriptors.FileDescriptor.buildFrom(fileProto, dependencies);
            resolvedDescriptors.add(fd);
        }

        // 返回最后一个（即用户的 .proto 文件描述符，import 的在前面）
        if (resolvedDescriptors.isEmpty()) {
            throw new RuntimeException("FileDescriptorSet 为空");
        }
        return resolvedDescriptors.get(resolvedDescriptors.size() - 1);
    }

    /**
     * 解析 import 依赖
     */
    private Descriptors.FileDescriptor[] resolveDependencies(
            DescriptorProtos.FileDescriptorProto fileProto,
            DescriptorProtos.FileDescriptorSet descriptorSet,
            List<Descriptors.FileDescriptor> resolved) {

        List<Descriptors.FileDescriptor> deps = new ArrayList<>();
        for (String depName : fileProto.getDependencyList()) {
            // 从已解析的描述符中查找
            for (Descriptors.FileDescriptor fd : resolved) {
                if (fd.getName().equals(depName)) {
                    deps.add(fd);
                    break;
                }
            }
        }
        return deps.toArray(new Descriptors.FileDescriptor[0]);
    }

    /**
     * 在 FileDescriptor 中查找 message 类型
     */
    private Descriptors.Descriptor findMessageDescriptor(Descriptors.FileDescriptor fileDescriptor, String messageType) {
        // 先尝试直接按名称查找
        for (Descriptors.Descriptor desc : fileDescriptor.getMessageTypes()) {
            Descriptors.Descriptor found = findInMessage(desc, messageType);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 递归查找 message 类型（支持嵌套）
     */
    private Descriptors.Descriptor findInMessage(Descriptors.Descriptor descriptor, String messageType) {
        if (descriptor.getName().equals(messageType) || descriptor.getFullName().equals(messageType)) {
            return descriptor;
        }
        for (Descriptors.Descriptor nested : descriptor.getNestedTypes()) {
            Descriptors.Descriptor found = findInMessage(nested, messageType);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}
