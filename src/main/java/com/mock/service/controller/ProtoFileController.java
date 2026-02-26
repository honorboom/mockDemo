package com.mock.service.controller;

import com.mock.service.dto.ProtoFileRequest;
import com.mock.service.dto.Result;
import com.mock.service.entity.ProtoFile;
import com.mock.service.service.ProtoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Proto 文件管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/proto-file")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProtoFileController {

    private final ProtoService protoService;

    /**
     * 创建 Proto 定义（编译 .proto 并存储）
     */
    @PostMapping
    public Result<ProtoFile> createProtoFile(@Valid @RequestBody ProtoFileRequest request) {
        try {
            ProtoFile protoFile = protoService.createProtoFile(request);
            return Result.success(protoFile);
        } catch (Exception e) {
            log.error("创建 Proto 文件失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有 Proto 定义列表
     */
    @GetMapping
    public Result<List<ProtoFile>> getAllProtoFiles() {
        try {
            List<ProtoFile> list = protoService.getAllProtoFiles();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取 Proto 文件列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取 Proto 定义详情
     */
    @GetMapping("/{id}")
    public Result<ProtoFile> getProtoFile(@PathVariable Long id) {
        try {
            ProtoFile protoFile = protoService.getProtoFile(id);
            return Result.success(protoFile);
        } catch (Exception e) {
            log.error("获取 Proto 文件失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新 Proto 定义（重新编译）
     */
    @PutMapping("/{id}")
    public Result<ProtoFile> updateProtoFile(@PathVariable Long id, @Valid @RequestBody ProtoFileRequest request) {
        try {
            ProtoFile protoFile = protoService.updateProtoFile(id, request);
            return Result.success(protoFile);
        } catch (Exception e) {
            log.error("更新 Proto 文件失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除 Proto 定义
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProtoFile(@PathVariable Long id) {
        try {
            protoService.deleteProtoFile(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除 Proto 文件失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取可用 message 类型列表
     */
    @GetMapping("/{id}/messages")
    public Result<List<String>> getMessageTypes(@PathVariable Long id) {
        try {
            List<String> types = protoService.getMessageTypes(id);
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取 message 类型列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
