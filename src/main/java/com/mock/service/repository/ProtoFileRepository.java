package com.mock.service.repository;

import com.mock.service.entity.ProtoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Proto 文件数据访问层
 */
@Repository
public interface ProtoFileRepository extends JpaRepository<ProtoFile, Long> {

    /**
     * 检查名称是否已存在
     */
    boolean existsByName(String name);

    /**
     * 检查名称是否已存在（排除指定 ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);
}
