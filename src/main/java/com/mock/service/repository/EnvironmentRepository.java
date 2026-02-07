package com.mock.service.repository;

import com.mock.service.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 环境配置数据访问层
 */
@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    /**
     * 查找默认环境
     */
    Optional<Environment> findByIsDefaultTrue();

    /**
     * 按显示顺序排序查询所有环境
     */
    List<Environment> findAllByOrderByDisplayOrderAsc();
}
