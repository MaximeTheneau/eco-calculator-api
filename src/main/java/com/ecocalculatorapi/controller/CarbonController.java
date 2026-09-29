package com.ecocalculatorapi.controller; 

import org.springframework.web.bind.annotation.*;

import com.ecocalculatorapi.dto.CarbonCalculationResult;
import com.ecocalculatorapi.entity.CarbonReport;
import com.ecocalculatorapi.repository.CarbonReportRepository;
import com.ecocalculatorapi.service.CarbonCalculatorService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarbonController {

    private final CarbonCalculatorService calculatorService;
    private final CarbonReportRepository reportRepository;

    public CarbonController(CarbonCalculatorService calculatorService,
                             CarbonReportRepository reportRepository) {
        this.calculatorService = calculatorService;
        this.reportRepository = reportRepository;
    }

    /**
     * Exemple : GET /api/carbon/calculate?url=https://example.com&greenHosting=false
     */
    @GetMapping("/calculate")
    public CarbonCalculationResult calculate(
            @RequestParam String url,
            @RequestParam(defaultValue = "false") boolean greenHosting
    ) throws IOException, InterruptedException {
        CarbonCalculationResult result = calculatorService.calculate(url, greenHosting);

        CarbonReport report = new CarbonReport(
                result.url(),
                result.pageSizeBytes(),
                result.co2GramsPerVisit(),
                result.greenHosting(),
                result.rating()
        );
        reportRepository.save(report);

        return result;
    }

    @GetMapping("/history")
    public List<CarbonReport> history() {
        return reportRepository.findAll();
    }
}
