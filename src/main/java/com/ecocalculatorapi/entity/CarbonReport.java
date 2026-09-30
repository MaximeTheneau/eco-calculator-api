package com.ecocalculatorapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class CarbonReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private long pageSizeBytes;
    private double co2GramsPerVisit;
    private boolean greenHosting;
    private String rating;
    private Instant createdAt = Instant.now();

    public CarbonReport() {
    }

    public CarbonReport(String url, long pageSizeBytes, double co2GramsPerVisit,
                         boolean greenHosting, String rating) {
        this.url = url;
        this.pageSizeBytes = pageSizeBytes;
        this.co2GramsPerVisit = co2GramsPerVisit;
        this.greenHosting = greenHosting;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getPageSizeBytes() {
        return pageSizeBytes;
    }

    public void setPageSizeBytes(long pageSizeBytes) {
        this.pageSizeBytes = pageSizeBytes;
    }

    public double getCo2GramsPerVisit() {
        return co2GramsPerVisit;
    }

    public void setCo2GramsPerVisit(double co2GramsPerVisit) {
        this.co2GramsPerVisit = co2GramsPerVisit;
    }

    public boolean isGreenHosting() {
        return greenHosting;
    }

    public void setGreenHosting(boolean greenHosting) {
        this.greenHosting = greenHosting;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
