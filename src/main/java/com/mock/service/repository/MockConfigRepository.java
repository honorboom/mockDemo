package com.mock.service.repository;

import com.mock.service.entity.MockConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Mock 配置数据访问层
 */
@Repository
public interface MockConfigRepository extends JpaRepository<MockConfig, Long> {

    /**
     * 根据路径和方法查找启用的 Mock 配置
     */
    Optional<MockConfig> findByPathAndMethodAndEnabledTrue(String path, String method);

    /**
     * 根据路径和方法查找 Mock 配置（包括禁用的）
     */
    Optional<MockConfig> findByPathAndMethod(String path, String method);

    /**
     * 查找所有启用的 Mock 配置
     */
    List<MockConfig> findByEnabledTrue();

    /**
     * 检查路径和方法是否已存在
     */
    boolean existsByPathAndMethod(String path, String method);

    /**
     * 检查路径和方法是否已存在（排除指定 ID）
     */
    boolean existsByPathAndMethodAndIdNot(String path, String method, Long id);
}
