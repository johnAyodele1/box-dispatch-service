package com.example.boxdispatch.box;

import com.example.boxdispatch.item.Item;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "boxes")
public class Box {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String txref;

    @Column(name = "weight_limit_grams", nullable = false)
    private int weightLimitGrams;

    @Column(name = "battery_percentage", nullable = false)
    private int batteryPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoxState state;

    @OneToMany(
        mappedBy = "box",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Item> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Box() {
    }

    public Box(
        UUID id,
        String txref,
        int weightLimitGrams,
        int batteryPercentage
    ) {
        this.id = id;
        this.txref = txref;
        this.weightLimitGrams = weightLimitGrams;
        this.batteryPercentage = batteryPercentage;
        this.state = BoxState.IDLE;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getTxref() {
        return txref;
    }

    public int getWeightLimitGrams() {
        return weightLimitGrams;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public BoxState getState() {
        return state;
    }

    public List<Item> getItems() {
        return items;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}