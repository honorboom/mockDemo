package com.mock.service.repository;

import com.mock.service.entity.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 环境变量数据访问层
 */
@Repository
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, Long> {

    /**
     * 根据环境ID查找所有变量
     */
    List<EnvironmentVariable> findByEnvironmentId(Long environmentId);

    /**
     * 根据环境ID和变量名查找
     */
    Optional<EnvironmentVariable> findByEnvironmentIdAndKey(Long environmentId, String key);

    /**
     * 检查环境变量是否已存在
     */
    boolean existsByEnvironmentIdAndKey(Long environmentId, String key);

    /**
     * 检查环境变量是否已存在（排除指定 ID）
     */
    boolean existsByEnvironmentIdAndKeyAndIdNot(Long environmentId, String key, Long id);

    /**
     * 删除指定环境的所有变量
     */
    void deleteByEnvironmentId(Long environmentId);
}
