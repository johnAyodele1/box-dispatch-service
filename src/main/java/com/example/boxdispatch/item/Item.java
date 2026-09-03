package com.example.boxdispatch.item;

import com.example.boxdispatch.box.Box;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "items_box_code_unique",
            columnNames = {"box_id", "code"}
        )
    }
)
public class Item {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "weight_grams", nullable = false)
    private int weightGrams;

    @Column(nullable = false, length = 255)
    private String code;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Item() {
    }

    public Item(
        UUID id,
        Box box,
        String name,
        int weightGrams,
        String code
    ) {
        this.id = id;
        this.box = box;
        this.name = name;
        this.weightGrams = weightGrams;
        this.code = code;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Box getBox() {
        return box;
    }

    public String getName() {
        return name;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}