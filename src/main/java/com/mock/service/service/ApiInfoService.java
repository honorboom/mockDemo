package com.mock.service.service;

import com.mock.service.dto.ApiInfoRequest;
import com.mock.service.dto.ApiInfoTreeDTO;
import com.mock.service.entity.ApiInfo;
import com.mock.service.repository.ApiInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 接口信息服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiInfoService {

    private final ApiInfoRepository apiInfoRepository;

    /**
     * 创建接口
     */
    @Transactional
    public ApiInfo createApiInfo(ApiInfoRequest request) {
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setName(request.getName());
        apiInfo.setDescription(request.getDescription());
        apiInfo.setUrl(request.getUrl());
        apiInfo.setMethod(request.getMethod());
        apiInfo.setHeaders(request.getHeaders());
        apiInfo.setParams(request.getParams());
        apiInfo.setBody(request.getBody());
        apiInfo.setExpectedStatus(request.getExpectedStatus() != null ? request.getExpectedStatus() : 200);
        apiInfo.setCategory(request.getCategory());
        apiInfo.setTags(request.getTags());
        apiInfo.setParentId(request.getParentId());
        apiInfo.setType("API");
        apiInfo.setDisplayOrder(0);

        return apiInfoRepository.save(apiInfo);
    }

    /**
     * 更新接口
     */
    @Transactional
    public ApiInfo updateApiInfo(Long id, ApiInfoRequest request) {
        ApiInfo apiInfo = apiInfoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("接口不存在"));

        apiInfo.setName(request.getName());
        apiInfo.setDescription(request.getDescription());
        apiInfo.setUrl(request.getUrl());
        apiInfo.setMethod(request.getMethod());
        apiInfo.setHeaders(request.getHeaders());
        apiInfo.setParams(request.getParams());
        apiInfo.setBody(request.getBody());
        apiInfo.setExpectedStatus(request.getExpectedStatus());
        apiInfo.setCategory(request.getCategory());
        apiInfo.setTags(request.getTags());
        apiInfo.setParentId(request.getParentId());

        return apiInfoRepository.save(apiInfo);
    }

    /**
     * 删除接口
     */
    @Transactional
    public void deleteApiInfo(Long id) {
        ApiInfo apiInfo = apiInfoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("接口不存在"));

        // 如果是文件夹，删除其所有子元素
        if ("FOLDER".equals(apiInfo.getType())) {
            List<ApiInfo> children = apiInfoRepository.findByParentIdOrderByDisplayOrder(id);
            for (ApiInfo child : children) {
                deleteApiInfo(child.getId());
            }
        }

        apiInfoRepository.deleteById(id);
    }

    /**
     * 获取接口详情
     */
    public ApiInfo getApiInfo(Long id) {
        return apiInfoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("接口不存在"));
    }

    /**
     * 获取所有接口
     */
    public List<ApiInfo> getAllApiInfos() {
        return apiInfoRepository.findAll();
    }

    /**
     * 按分类查询接口
     */
    public List<ApiInfo> getApiInfosByCategory(String category) {
        return apiInfoRepository.findByCategory(category);
    }

    /**
     * 按名称模糊查询接口
     */
    public List<ApiInfo> searchApiInfosByName(String name) {
        return apiInfoRepository.findByNameContaining(name);
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return apiInfoRepository.findAll().stream()
            .map(ApiInfo::getCategory)
            .filter(category -> category != null && !category.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * 获取接口树形结构
     */
    public List<ApiInfoTreeDTO> getApiInfoTree() {
        List<ApiInfo> roots = apiInfoRepository.findByParentIdIsNullOrderByDisplayOrder();
        // 排序：API 类型在前，FOLDER 类型在后，同类型按 displayOrder 排序
        roots.sort((a, b) -> {
            boolean aIsApi = "API".equals(a.getType());
            boolean bIsApi = "API".equals(b.getType());
            if (aIsApi && !bIsApi) return -1;
            if (!aIsApi && bIsApi) return 1;
            return Integer.compare(a.getDisplayOrder(), b.getDisplayOrder());
        });
        return roots.stream()
            .map(this::buildTreeNode)
            .collect(Collectors.toList());
    }

    /**
     * 构建树形节点
     */
    private ApiInfoTreeDTO buildTreeNode(ApiInfo apiInfo) {
        ApiInfoTreeDTO dto = ApiInfoTreeDTO.builder()
            .id(apiInfo.getId())
            .name(apiInfo.getName())
            .type(apiInfo.getType())
            .url(apiInfo.getUrl())
            .method(apiInfo.getMethod())
            .description(apiInfo.getDescription())
            .category(apiInfo.getCategory())
            .createdAt(apiInfo.getCreatedAt())
            .updatedAt(apiInfo.getUpdatedAt())
            .build();

        // 如果是文件夹，递归加载子节点
        if ("FOLDER".equals(apiInfo.getType())) {
            List<ApiInfo> children = apiInfoRepository.findByParentIdOrderByDisplayOrder(apiInfo.getId());
            // 子节点也按照同样规则排序：API 在前，FOLDER 在后
            children.sort((a, b) -> {
                boolean aIsApi = "API".equals(a.getType());
                boolean bIsApi = "API".equals(b.getType());
                if (aIsApi && !bIsApi) return -1;
                if (!aIsApi && bIsApi) return 1;
                return Integer.compare(a.getDisplayOrder(), b.getDisplayOrder());
            });
            dto.setChildren(children.stream()
                .map(this::buildTreeNode)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * 创建文件夹
     */
    @Transactional
    public ApiInfo createFolder(String name, Long parentId) {
        // 检查文件夹名称是否重复
        List<ApiInfo> siblings = parentId == null
            ? apiInfoRepository.findByParentIdIsNullOrderByDisplayOrder()
            : apiInfoRepository.findByParentIdOrderByDisplayOrder(parentId);

        boolean exists = siblings.stream()
            .anyMatch(item -> item.getName().equals(name));

        if (exists) {
            throw new RuntimeException("同级目录中已存在同名文件夹：" + name);
        }

        ApiInfo folder = new ApiInfo();
        folder.setName(name);
        folder.setParentId(parentId);
        folder.setType("FOLDER");
        folder.setDisplayOrder(siblings.size());
        folder.setUrl(null);
        folder.setMethod(null);

        return apiInfoRepository.save(folder);
    }

    /**
     * 创建文件夹（内部方法，用于导入时避免嵌套事务）
     */
    private ApiInfo createFolderInternal(String name, Long parentId) {
        // 检查文件夹名称是否重复
        List<ApiInfo> siblings = parentId == null
            ? apiInfoRepository.findByParentIdIsNullOrderByDisplayOrder()
            : apiInfoRepository.findByParentIdOrderByDisplayOrder(parentId);

        boolean exists = siblings.stream()
            .anyMatch(item -> item.getName().equals(name));

        if (exists) {
            // 如果已存在，直接返回（用于导入时处理）
            return siblings.stream()
                .filter(f -> "FOLDER".equals(f.getType()) && f.getName().equals(name))
                .findFirst()
                .orElse(null);
        }

        ApiInfo folder = new ApiInfo();
        folder.setName(name);
        folder.setParentId(parentId);
        folder.setType("FOLDER");
        folder.setDisplayOrder(siblings.size());
        folder.setUrl(null);
        folder.setMethod(null);

        return apiInfoRepository.save(folder);
    }

    /**
     * 移动接口或文件夹到其他位置
     */
    @Transactional
    public ApiInfo moveApiInfo(Long id, Long newParentId) {
        ApiInfo apiInfo = apiInfoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("接口不存在"));

        // 检查是否尝试移动到自己或其子目录
        if (id.equals(newParentId)) {
            throw new RuntimeException("不能移动到自己");
        }

        // 检查是否尝试移动到自己的子目录
        if (newParentId != null && isDescendant(id, newParentId)) {
            throw new RuntimeException("不能移动到自己的子目录");
        }

        apiInfo.setParentId(newParentId);
        return apiInfoRepository.save(apiInfo);
    }

    /**
     * 检查targetId是否是sourceId的后代
     */
    private boolean isDescendant(Long sourceId, Long targetId) {
        ApiInfo target = apiInfoRepository.findById(targetId).orElse(null);
        if (target == null) {
            return false;
        }

        if (target.getParentId() == null) {
            return false;
        }

        if (target.getParentId().equals(sourceId)) {
            return true;
        }

        return isDescendant(sourceId, target.getParentId());
    }

    /**
     * 导入接口信息
     */
    @Transactional
    public Map<String, Object> importApiInfos(Object jsonData) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<Map<String, Object>> apiList = parseImportData(jsonData);

            for (Map<String, Object> item : apiList) {
                try {
                    importApiItem(item, null);
                    successCount++;
                } catch (Exception e) {
                    log.warn("导入接口失败: {}, 原因: {}", item.get("name"), e.getMessage());
                    skipCount++;
                    errors.add("导入失败: " + item.get("name") + " - " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("successCount", successCount);
            result.put("skipCount", skipCount);
            result.put("totalCount", apiList.size());
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            log.info("批量导入接口完成: 成功 {}, 跳过 {}, 总计 {}",
                     successCount, skipCount, apiList.size());

        } catch (Exception e) {
            log.error("导入接口失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 解析导入数据（支持Postman Collection和简单数组格式）
     */
    private List<Map<String, Object>> parseImportData(Object jsonData) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (jsonData instanceof List) {
            // 简单数组格式
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) jsonData;
            result.addAll(list);
        } else if (jsonData instanceof Map) {
            // Postman Collection 格式或单个对象
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) jsonData;

            if (map.containsKey("info") && map.containsKey("item")) {
                // Postman Collection 格式
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("item");
                parsePostmanItems(items, result, null);
            } else {
                // 单个对象
                result.add(map);
            }
        }

        return result;
    }

    /**
     * 递归解析Postman Collection格式
     */
    @SuppressWarnings("unchecked")
    private void parsePostmanItems(List<Map<String, Object>> items, List<Map<String, Object>> result, String folderPath) {
        for (Map<String, Object> item : items) {
            String itemName = (String) item.get("name");
            String currentPath = folderPath == null ? itemName : folderPath + "/" + itemName;

            // 检查是否是文件夹（包含item数组）
            if (item.containsKey("item") && item.get("item") instanceof List) {
                List<Map<String, Object>> subitems = (List<Map<String, Object>>) item.get("item");
                parsePostmanItems(subitems, result, currentPath);
            } else if (item.containsKey("request")) {
                // 这是一个API请求
                Map<String, Object> apiItem = extractApiFromPostmanItem(item, currentPath);
                result.add(apiItem);
            }
        }
    }

    /**
     * 从Postman项提取API信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractApiFromPostmanItem(Map<String, Object> item, String name) {
        Map<String, Object> api = new HashMap<>();

        api.put("name", name);

        Map<String, Object> request = (Map<String, Object>) item.get("request");
        String method = (String) request.getOrDefault("method", "GET");
        api.put("method", method);

        // 解析URL
        String url = extractUrlFromPostmanRequest(request);
        api.put("url", url);

        // 获取description
        Object description = request.get("description");
        if (description != null) {
            api.put("description", description.toString());
        }

        // 获取响应示例
        if (item.containsKey("response") && item.get("response") instanceof List) {
            List<Map<String, Object>> responses = (List<Map<String, Object>>) item.get("response");
            if (!responses.isEmpty()) {
                Map<String, Object> firstResponse = responses.get(0);
                Object body = firstResponse.get("body");
                if (body != null) {
                    api.put("responseBody", body.toString());
                }
            }
        }

        return api;
    }

    /**
     * 从Postman请求提取URL
     */
    @SuppressWarnings("unchecked")
    private String extractUrlFromPostmanRequest(Map<String, Object> request) {
        Object urlObj = request.get("url");

        if (urlObj instanceof String) {
            return (String) urlObj;
        } else if (urlObj instanceof Map) {
            Map<String, Object> urlMap = (Map<String, Object>) urlObj;
            String url = (String) urlMap.get("raw");

            if (url == null) {
                url = (String) urlMap.get("path");
            }

            if (url != null) {
                // 移除变量占位符
                url = url.replaceAll("\\{\\{[^}]+\\}\\}", "");
                // 移除查询参数
                url = url.split("\\?")[0];

                // 确保以/开头
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
            }

            return url;
        }

        return "/unknown";
    }

    /**
     * 导入单个API项
     */
    private void importApiItem(Map<String, Object> item, Long parentId) {
        String name = (String) item.get("name");
        String url = (String) item.get("url");
        String method = (String) item.get("method");

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("缺少name字段");
        }

        // 处理嵌套的folder/name格式
        String[] parts = name.split("/");
        Long currentParentId = parentId;

        // 创建所有必要的文件夹
        for (int i = 0; i < parts.length - 1; i++) {
            String folderName = parts[i];
            List<ApiInfo> folders = currentParentId == null
                ? apiInfoRepository.findByParentIdIsNullOrderByDisplayOrder()
                : apiInfoRepository.findByParentIdOrderByDisplayOrder(currentParentId);

            Optional<ApiInfo> existingFolder = folders.stream()
                .filter(f -> "FOLDER".equals(f.getType()) && f.getName().equals(folderName))
                .findFirst();

            if (existingFolder.isPresent()) {
                currentParentId = existingFolder.get().getId();
            } else {
                ApiInfo newFolder = createFolderInternal(folderName, currentParentId);
                currentParentId = newFolder.getId();
            }
        }

        // 获取最终的API名称
        String apiName = parts[parts.length - 1];

        // 检查同级是否已存在相同的API
        if (url != null && method != null) {
            boolean exists = apiInfoRepository.existsByParentIdAndUrlAndMethod(currentParentId, url, method);
            if (exists) {
                throw new RuntimeException("同级已存在相同的API: " + method + " " + url);
            }
        }

        // 创建API
        ApiInfo api = new ApiInfo();
        api.setName(apiName);
        api.setUrl(url);
        api.setMethod(method);
        api.setParentId(currentParentId);
        api.setType("API");
        api.setDisplayOrder(0);

        // 设置可选字段
        if (item.containsKey("description")) {
            api.setDescription((String) item.get("description"));
        }
        if (item.containsKey("responseBody")) {
            api.setBody((String) item.get("responseBody"));
        }
        if (item.containsKey("category")) {
            api.setCategory((String) item.get("category"));
        }
        if (item.containsKey("headers")) {
            api.setHeaders((String) item.get("headers"));
        }
        if (item.containsKey("params")) {
            api.setParams((String) item.get("params"));
        }

        apiInfoRepository.save(api);
    }
}
