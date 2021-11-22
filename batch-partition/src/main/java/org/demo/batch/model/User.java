package org.demo.batch.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
	private String username;
    private int userId;
    private int age;
    private String postCode;
    private LocalDateTime transactionDate;
    private double amount;
}
