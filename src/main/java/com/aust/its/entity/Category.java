package com.aust.its.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    public long id;

    @Column(name = "category_name")
    public String categoryName;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore // ⬅️ prevents Issue -> Category -> Issue infinite loop
    private List<Issue> issues;
}
