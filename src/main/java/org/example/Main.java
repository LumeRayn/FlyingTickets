package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String filePath = "src/main/java/resources/tickets.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            //System.out.println("Содержимое файла: " + content); // отладка
            JSONObject jsonObject = new JSONObject(content);
            JSONArray tickets = jsonObject.getJSONArray("tickets");

            Map<String, List<Integer>> flightTimes = new HashMap<>();
            List<Double> prices = new ArrayList<>();

            for (int i = 0; i < tickets.length(); i++) {
                JSONObject ticket = tickets.getJSONObject(i);
                String originName = ticket.getString("origin_name");
                String destinationName = ticket.getString("destination_name");
                String carrier = ticket.getString("carrier");
                double price = ticket.getDouble("price");

                int flightTime = calculateFlightTime(ticket);

                if (originName.equals("Владивосток") && destinationName.equals("Тель-Авив")) {
                    flightTimes.putIfAbsent(carrier, new ArrayList<>());
                    flightTimes.get(carrier).add(flightTime);
                    prices.add(price);
                }
            }
            // Минимальное время полета для каждого авиаперевозчика
            System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом:");
            for (Map.Entry<String, List<Integer>> entry : flightTimes.entrySet()) {
                String carrier = entry.getKey();
                Integer minTime = Collections.min(entry.getValue());
                double TimeHour = minTime /60d;
                System.out.println(carrier + ": " + minTime + " минут "+ "(если в часах "+ TimeHour+")");
            }
            // Выводим данные по всем билетам (не только самым быстрым)
            System.out.println("\nВсе данные по билетам:");
            for (Map.Entry<String, List<Integer>> entry : flightTimes.entrySet()) {
                String carrier = entry.getKey();
                List<Integer> times = entry.getValue();
                for (Integer time : times) {
                    double TimeHour = time / 60d;
                    System.out.println(carrier + ": " + time + " минут " + "(если в часах " + TimeHour + ")");
                }
            }
            // Разница между средней ценой и медианой
            double averagePrice = prices.stream().mapToDouble(val -> val).average().orElse(0.0);
            Collections.sort(prices);
            double medianPrice = prices.size() % 2 == 0 ?
                    (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2 :
                    prices.get(prices.size() / 2);
            double difference = averagePrice - medianPrice;
            System.out.printf("Разница между средней ценой (%.2f) и медианой (%.2f) = %.2f%n", averagePrice, medianPrice, difference);
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла", e);
        }
    }

    private static int calculateFlightTime(JSONObject ticket) {
        String departureDate = ticket.getString("departure_date");
        String departureTime = ticket.getString("departure_time");
        String arrivalDate = ticket.getString("arrival_date");
        String arrivalTime = ticket.getString("arrival_time");

        // Форматирование
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.yy H:mm")
                .toFormatter();

        // Получение LocalDateTime для времени вылета
        LocalDateTime departureDateTime = LocalDateTime.parse(departureDate + " " + departureTime, formatter);

        // Получение LocalDateTime для времени прибытия
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDate + " " + arrivalTime, formatter);

        // Вычисление времени полета в минутах
        long flightTimeInMinutes = java.time.Duration.between(departureDateTime, arrivalDateTime).toMinutes();
        return (int) flightTimeInMinutes;
    }
}
