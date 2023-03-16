package org.example.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculateService {

    public void execute() {
        JSONParser parser = new JSONParser();
        DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("d.M.yy;H:m");
        try {
            List<Duration> durations = new ArrayList<>();
            Object obj = parser.parse(new FileReader("tickets.json"));
            JSONObject jo = (JSONObject) obj;
            JSONArray array = (JSONArray) jo.get("tickets");
            for (int i = 0; i < array.size(); i++) {
                jo = (JSONObject) array.get(i);
                String departureDate = (String) jo.get("departure_date");
                String departureTime = (String) jo.get("departure_time");
                String arrivalDate = (String) jo.get("arrival_date");
                String arrivalTime = (String) jo.get("arrival_time");

                LocalDateTime departureDateTime = LocalDateTime.parse(departureDate + ";" + departureTime, simpleDateFormat);
                ZoneId zoneIdVdv = ZoneId.of("Asia/Vladivostok");
                LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDate + ";" + arrivalTime, simpleDateFormat);
                ZoneId zoneIdTlv = ZoneId.of("Asia/Jerusalem");
                ZonedDateTime zonedDateTimeVdv = ZonedDateTime.of(departureDateTime, zoneIdVdv);
                ZonedDateTime zonedDateTimeTlv = ZonedDateTime.of(arrivalDateTime, zoneIdTlv);

                Instant instantVdv = zonedDateTimeVdv.toInstant();
                Instant instantTlv = zonedDateTimeTlv.toInstant();

                ZonedDateTime zdtUtcVdv = instantVdv.atZone(ZoneId.of("Etc/UTC"));
                ZonedDateTime zdtUtcTlv = instantTlv.atZone(ZoneId.of("Etc/UTC"));

                Duration duration = Duration.between(zdtUtcVdv, zdtUtcTlv);
                durations.add(duration);
            }

            double averageDuration = durations.stream()
                    .mapToLong(Duration::getSeconds)
                    .average()
                    .orElseThrow(() -> new RuntimeException("Невозможно рассчитать среднее время полета."));

            int differenceInHours = (int) (averageDuration / 3600) % 24;
            int differenceInMinutes = (int) (averageDuration / 60) % 60;

            System.out.printf
                    ("Среднее время полета между Владивостоком и Тель-Авивом %d часов и %d минут(ы).\n"
                            , differenceInHours, differenceInMinutes);

            Duration percentile = getPercentile(durations);
            int percentileHours = (int) (percentile.getSeconds() / 3600.0) % 24;
            int percentileMinutes = (int) (percentile.getSeconds() / 60.0) % 60;

            System.out.printf
                    ("90-й процентиль времени полета между городами Владивосток и Тель-Авив %d часов и %d минут(ы)."
                            , percentileHours, percentileMinutes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Duration getPercentile(List<Duration> durations) {
        Collections.sort(durations);
        int index = (int) (0.9 * durations.size());
        return durations.get(index - 1);
    }
}
