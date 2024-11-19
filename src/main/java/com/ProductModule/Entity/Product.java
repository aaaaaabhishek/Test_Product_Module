package com.ProductModule.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "product")  // Ensure this matches your actual table name

public class Product {
    @Id
    public Long product_id;
    public String product_name;
    public String price;
}
