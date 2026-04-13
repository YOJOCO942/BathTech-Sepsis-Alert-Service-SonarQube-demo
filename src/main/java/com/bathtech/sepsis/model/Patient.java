package com.bathtech.sepsis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nhsNumber;

    private String name;
    private int age;
    private String ward;

    public Patient() {}

    public Patient(String nhsNumber, String name, int age, String ward) {
        this.nhsNumber = nhsNumber;
        this.name = name;
        this.age = age;
        this.ward = ward;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNhsNumber() { return nhsNumber; }
    public void setNhsNumber(String nhsNumber) { this.nhsNumber = nhsNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
}
