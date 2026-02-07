package com.mock.service.controller;

import com.mock.service.dto.ApiInfoRequest;
import com.mock.service.dto.ApiInfoTreeDTO;
import com.mock.service.dto.Result;
import com.mock.service.entity.ApiInfo;
import com.mock.service.service.ApiInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 接口信息管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api-info")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiInfoController {

    private final ApiInfoService apiInfoService;

    /**
     * 获取接口树形结构
     */
    @GetMapping("/tree")
    public Result<List<ApiInfoTreeDTO>> getApiInfoTree() {
        try {
            List<ApiInfoTreeDTO> tree = apiInfoService.getApiInfoTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取接口树形结构失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        try {
            List<String> categories = apiInfoService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有接口
     */
    @GetMapping
    public Result<List<ApiInfo>> getAllApiInfos() {
        try {
            List<ApiInfo> apiInfos = apiInfoService.getAllApiInfos();
            return Result.success(apiInfos);
        } catch (Exception e) {
            log.error("获取接口列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取接口详情
     */
    @GetMapping("/{id}")
    public Result<ApiInfo> getApiInfo(@PathVariable Long id) {
        try {
            log.info("【API 请求】获取接口详情，ID: {}", id);

            if (id == null || id <= 0) {
                log.warn("【API 警告】无效的ID: {}", id);
                return Result.error("无效的接口ID");
            }

            ApiInfo apiInfo = apiInfoService.getApiInfo(id);

            if (apiInfo == null) {
                log.warn("【API 警告】接口不存在，ID: {}", id);
                return Result.error("接口不存在，ID: " + id);
            }

            log.info("【API 成功】获取接口详情，名称: {}, ID: {}", apiInfo.getName(), apiInfo.getId());
            return Result.success(apiInfo);

        } catch (Exception e) {
            log.error("【API 异常】获取接口详情失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("获取接口详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建接口
     */
    @PostMapping
    public Result<ApiInfo> createApiInfo(@RequestBody ApiInfoRequest request) {
        try {
            ApiInfo apiInfo = apiInfoService.createApiInfo(request);
            return Result.success(apiInfo);
        } catch (Exception e) {
            log.error("创建接口失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folder")
    public Result<ApiInfo> createFolder(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Long parentId = request.get("parentId") != null
                ? Long.parseLong(request.get("parentId").toString())
                : null;

            if (name == null || name.isEmpty()) {
                return Result.error("文件夹名称不能为空");
            }

            ApiInfo folder = apiInfoService.createFolder(name, parentId);
            return Result.success(folder);
        } catch (Exception e) {
            log.error("创建文件夹失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导入接口信息（支持Postman Collection和简单数组格式）
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importApiInfos(@RequestBody Object jsonData) {
        try {
            Map<String, Object> result = apiInfoService.importApiInfos(jsonData);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入接口失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新接口
     */
    @PutMapping("/{id}")
    public Result<ApiInfo> updateApiInfo(
            @PathVariable Long id,
            @RequestBody ApiInfoRequest request
    ) {
        try {
            ApiInfo apiInfo = apiInfoService.updateApiInfo(id, request);
            return Result.success(apiInfo);
        } catch (Exception e) {
            log.error("更新接口失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移动接口或文件夹到其他位置
     */
    @PutMapping("/{id}/move")
    public Result<ApiInfo> moveApiInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        try {
            Long newParentId = request.get("parentId") != null
                ? Long.parseLong(request.get("parentId").toString())
                : null;

            ApiInfo apiInfo = apiInfoService.moveApiInfo(id, newParentId);
            return Result.success(apiInfo);
        } catch (Exception e) {
            log.error("移动接口失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除接口
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiInfo(@PathVariable Long id) {
        try {
            apiInfoService.deleteApiInfo(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除接口失败", e);
            return Result.error(e.getMessage());
        }
    }
}
