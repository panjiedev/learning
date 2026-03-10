package com.example.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Prize {
    private String id;
    private String name;
    private Integer weight; // 权重值
}
