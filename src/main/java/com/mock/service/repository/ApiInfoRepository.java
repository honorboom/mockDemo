package com.mock.service.repository;

import com.mock.service.entity.ApiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 接口信息 Repository
 */
@Repository
public interface ApiInfoRepository extends JpaRepository<ApiInfo, Long> {

    /**
     * 按分类查询接口
     */
    List<ApiInfo> findByCategory(String category);

    /**
     * 按名称模糊查询接口
     */
    List<ApiInfo> findByNameContaining(String name);

    /**
     * 按父级ID查询子节点，按显示顺序排序
     */
    List<ApiInfo> findByParentIdOrderByDisplayOrder(Long parentId);

    /**
     * 检查同级是否存在相同的path和method
     */
    boolean existsByParentIdAndUrlAndMethod(Long parentId, String url, String method);

    /**
     * 获取所有根级接口和文件夹，按显示顺序排序
     */
    List<ApiInfo> findByParentIdIsNullOrderByDisplayOrder();

    /**
     * 按parentId和type查询
     */
    List<ApiInfo> findByParentIdAndType(Long parentId, String type);
}

