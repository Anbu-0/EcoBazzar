package com.ecobazaar.ecobazaar.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cropName;
    private String soilType;
    private String pesticides;
    private LocalDate harvestDate;
    private String gpsLocation;
    private String imagePath;
    private String qualityGrade;
    private Double confidenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id")
    private User farmer;

   
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCropName() {
        return cropName;
    }
    public void setCropName(String cropName) {
        this.cropName = cropName;
    }
    public String getSoilType() {
        return soilType;
    }
    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }
    public String getPesticides() {
        return pesticides;
    }
    public void setPesticides(String pesticides) {
        this.pesticides = pesticides;
    }
    public LocalDate getHarvestDate() {
        return harvestDate;
    }
    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }
    public String getGpsLocation() {
        return gpsLocation;
    }
    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getQualityGrade() {
        return qualityGrade;
    }
    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    public User getFarmer() {
        return farmer;
    }
    public void setFarmer(User farmer) {
        this.farmer = farmer;
    }
}