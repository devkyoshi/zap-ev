package com.ead.zap.models;

public class Vehicle {
    private String id;
    private String name;
    private String plateNumber;
    private String type;

    public Vehicle(String id, String name, String plateNumber, String type) {
        this.id = id;
        this.name = name;
        this.plateNumber = plateNumber;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getType() {
        return type;
    }
}