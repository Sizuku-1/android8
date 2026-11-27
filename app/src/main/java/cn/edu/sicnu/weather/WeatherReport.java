package cn.edu.sicnu.weather;

import java.util.List;

final class WeatherReport {
    int status;
    String date;
    Station cityInfo;
    Observations data;

    static final class Station {
        String citykey;
        String updateTime;
    }

    static final class Observations {
        String shidu;
        String pm25;
        String pm10;
        String quality;
        String wendu;
        List<Day> forecast;
    }

    static final class Day {
        String ymd;
        String week;
        String type;
        String high;
        String low;
        String fx;
        String fl;
        String sunrise;
        String sunset;
    }
}
