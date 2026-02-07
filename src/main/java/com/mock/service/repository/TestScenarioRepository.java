package com.mock.service.repository;

import com.mock.service.entity.TestScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestScenarioRepository extends JpaRepository<TestScenario, Long> {
    List<TestScenario> findByEnabledTrue();
}
