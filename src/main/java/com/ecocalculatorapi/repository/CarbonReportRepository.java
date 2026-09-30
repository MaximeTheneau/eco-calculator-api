package com.ecocalculatorapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecocalculatorapi.entity.CarbonReport;

public interface CarbonReportRepository extends JpaRepository<CarbonReport, Long> {
}
