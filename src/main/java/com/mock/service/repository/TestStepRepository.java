package com.mock.service.repository;

import com.mock.service.entity.TestStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestStepRepository extends JpaRepository<TestStep, Long> {
    List<TestStep> findByScenarioIdOrderByStepOrderAsc(Long scenarioId);
    void deleteByScenarioId(Long scenarioId);
}
