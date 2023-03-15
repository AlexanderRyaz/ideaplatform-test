package org.example;

import org.example.service.CalculateService;

public class Main {
    public static void main(String[] args) {
        CalculateService calculateService = new CalculateService();
        calculateService.execute();
    }
}