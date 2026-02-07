package com.mock.service.service;

import com.mock.service.dto.MockConfigRequest;
import com.mock.service.entity.MockConfig;
import com.mock.service.repository.MockConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mock配置服务类
 * <p>
 * 提供Mock配置的CRUD操作、分页查询、批量导入、启用/禁用切换等功能。
 * 负责Mock配置的业务逻辑处理，包括路径冲突检查、显示序号管理等。
 * </p>
 *
 * @author Mock Service Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockConfigService {

    /**
     * Mock配置数据访问层
     */
    private final MockConfigRepository mockConfigRepository;

    /**
     * 创建Mock配置
     * <p>
     * 创建新的Mock配置，会自动分配显示序号（当前最大序号+1），
     * 并检查路径和方法的唯一性，避免冲突。
     * </p>
     *
     * @param request Mock配置请求对象，包含所有必要的配置信息
     * @return 保存后的Mock配置实体（包含自动生成的ID和时间戳）
     * @throws RuntimeException 如果路径和方法组合已存在
     */
    @Transactional
    public MockConfig createMock(MockConfigRequest request) {
        // 检查路径和方法是否已存在
        if (mockConfigRepository.existsByPathAndMethod(request.getPath(), request.getMethod())) {
            throw new RuntimeException("该路径和方法的 Mock 已存在");
        }

        // 计算新的显示序号（当前最大序号 + 1）
        Integer maxOrder = mockConfigRepository.findAll().stream()
            .map(MockConfig::getDisplayOrder)
            .filter(order -> order != null)
            .max(Integer::compareTo)
            .orElse(0);

        MockConfig config = new MockConfig();
        config.setDisplayOrder(maxOrder + 1);
        config.setName(request.getName());
        config.setPath(request.getPath());
        config.setMethod(request.getMethod().toUpperCase());
        config.setStatusCode(request.getStatusCode());
        config.setContentType(request.getContentType());
        config.setRequestBody(request.getRequestBody());
        config.setResponseBody(request.getResponseBody());
        config.setDelay(request.getDelay());
        config.setEnabled(request.getEnabled());
        config.setDescription(request.getDescription());
        return mockConfigRepository.save(config);
    }

    /**
     * 更新Mock配置
     * <p>
     * 更新现有的Mock配置。会检查路径和方法的唯一性（排除当前记录），
     * 防止与其他配置冲突。更新后会自动刷新数据库确保数据一致性。
     * </p>
     *
     * @param id Mock配置ID
     * @param request 更新的Mock配置数据
     * @return 更新后的Mock配置实体
     * @throws RuntimeException 如果Mock配置不存在或路径方法组合与其他配置冲突
     */
    @Transactional
    public MockConfig updateMock(Long id, MockConfigRequest request) {
        MockConfig config = mockConfigRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mock 配置不存在"));

        log.debug("更新前的 Mock 配置: id={}, name={}, path={}, method={}",
            config.getId(), config.getName(), config.getPath(), config.getMethod());

        // 检查路径和方法是否与其他记录冲突（排除当前记录）
        boolean exists = mockConfigRepository.existsByPathAndMethodAndIdNot(
            request.getPath(),
            request.getMethod().toUpperCase(),
            id
        );

        if (exists) {
            throw new RuntimeException("该路径和方法的 Mock 已被其他配置使用");
        }

        // 更新字段
        config.setName(request.getName());
        config.setPath(request.getPath());
        config.setMethod(request.getMethod().toUpperCase());
        config.setStatusCode(request.getStatusCode());
        config.setContentType(request.getContentType());
        config.setRequestBody(request.getRequestBody());
        config.setResponseBody(request.getResponseBody());
        config.setDelay(request.getDelay());
        config.setEnabled(request.getEnabled());
        config.setDescription(request.getDescription());

        log.info("准备保存更新: name={}, path={}, method={}, statusCode={}, responseBody={}",
            config.getName(), config.getPath(), config.getMethod(), config.getStatusCode(),
            config.getResponseBody() != null ? config.getResponseBody().substring(0, Math.min(50, config.getResponseBody().length())) : "null");

        // 保存并刷新
        MockConfig savedConfig = mockConfigRepository.save(config);
        mockConfigRepository.flush();

        log.debug("更新后的 Mock 配置: id={}, name={}, path={}, method={}",
            savedConfig.getId(), savedConfig.getName(), savedConfig.getPath(), savedConfig.getMethod());

        return savedConfig;
    }

    /**
     * 删除Mock配置
     * <p>
     * 根据ID删除Mock配置，删除后会自动重新排序所有剩余配置的显示序号，
     * 确保序号连续且从1开始。
     * </p>
     *
     * @param id 要删除的Mock配置ID
     */
    @Transactional
    public void deleteMock(Long id) {
        mockConfigRepository.deleteById(id);
        // 删除后重新排序所有 displayOrder
        reorderDisplayOrder();
    }

    /**
     * 重新排序所有Mock的显示序号
     * <p>
     * 私有方法，用于在删除Mock后重新分配连续的显示序号。
     * 按ID升序排序后，依次分配序号1, 2, 3...
     * </p>
     */
    private void reorderDisplayOrder() {
        List<MockConfig> allMocks = mockConfigRepository.findAll();
        // 按 ID 排序
        allMocks.sort((a, b) -> Long.compare(a.getId(), b.getId()));

        // 重新分配连续的序号
        for (int i = 0; i < allMocks.size(); i++) {
            allMocks.get(i).setDisplayOrder(i + 1);
        }

        mockConfigRepository.saveAll(allMocks);
    }

    /**
     * 获取Mock配置详情
     *
     * @param id Mock配置ID
     * @return Mock配置实体
     * @throws RuntimeException 如果Mock配置不存在
     */
    public MockConfig getMock(Long id) {
        return mockConfigRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mock 配置不存在"));
    }

    /**
     * 获取所有Mock配置
     * <p>
     * 按ID升序返回所有Mock配置。
     * </p>
     *
     * @return Mock配置列表
     */
    public List<MockConfig> getAllMocks() {
        return mockConfigRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * 分页获取Mock配置
     * <p>
     * 支持分页查询，按ID升序排序。
     * </p>
     *
     * @param page 页码（从0开始）
     * @param size 每页条数
     * @return 分页结果对象，包含当前页数据和总数等信息
     */
    public Page<MockConfig> getMocksByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return mockConfigRepository.findAll(pageable);
    }

    /**
     * 获取所有启用的Mock配置
     *
     * @return 启用状态的Mock配置列表
     */
    public List<MockConfig> getEnabledMocks() {
        return mockConfigRepository.findByEnabledTrue();
    }

    /**
     * 根据路径和方法查找Mock配置
     * <p>
     * 只返回启用状态的Mock配置。用于动态Mock匹配。
     * </p>
     *
     * @param path 请求路径
     * @param method HTTP方法（会自动转换为大写）
     * @return 匹配的Mock配置，如果未找到返回null
     */
    public MockConfig findMockByPathAndMethod(String path, String method) {
        return mockConfigRepository.findByPathAndMethodAndEnabledTrue(path, method.toUpperCase())
            .orElse(null);
    }

    /**
     * 切换Mock启用状态
     * <p>
     * 在启用和禁用之间切换。
     * </p>
     *
     * @param id Mock配置ID
     * @return 更新后的Mock配置实体
     * @throws RuntimeException 如果Mock配置不存在
     */
    @Transactional
    public MockConfig toggleEnabled(Long id) {
        MockConfig config = getMock(id);
        config.setEnabled(!config.getEnabled());
        return mockConfigRepository.save(config);
    }

    /**
     * 批量导入Mock配置
     * <p>
     * 批量创建Mock配置，自动跳过路径和方法组合已存在的配置。
     * 导入过程中的错误不会影响其他配置的导入。
     * </p>
     *
     * @param requests Mock配置请求列表
     * @return 成功导入的Mock配置列表
     */
    @Transactional
    public List<MockConfig> importMocks(List<MockConfigRequest> requests) {
        List<MockConfig> configs = new java.util.ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        for (MockConfigRequest request : requests) {
            try {
                // 检查路径和方法是否已存在
                if (mockConfigRepository.existsByPathAndMethod(request.getPath(), request.getMethod())) {
                    log.warn("跳过已存在的 Mock: {} {}", request.getMethod(), request.getPath());
                    skipCount++;
                    continue;
                }

                MockConfig config = new MockConfig();
                config.setName(request.getName());
                config.setPath(request.getPath());
                config.setMethod(request.getMethod().toUpperCase());
                config.setStatusCode(request.getStatusCode() != null ? request.getStatusCode() : 200);
                config.setContentType(request.getContentType() != null ? request.getContentType() : "application/json");
                config.setRequestBody(request.getRequestBody());
                config.setResponseBody(request.getResponseBody());
                config.setDelay(request.getDelay() != null ? request.getDelay() : 0);
                config.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
                config.setDescription(request.getDescription());

                MockConfig saved = mockConfigRepository.save(config);
                configs.add(saved);
                successCount++;
            } catch (Exception e) {
                log.error("导入 Mock 失败: {} {}, 错误: {}", request.getMethod(), request.getPath(), e.getMessage());
            }
        }

        log.info("批量导入完成: 成功 {}, 跳过 {}, 总计 {}", successCount, skipCount, requests.size());
        return configs;
    }
}
