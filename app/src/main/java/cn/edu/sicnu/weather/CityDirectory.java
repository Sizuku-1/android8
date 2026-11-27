package cn.edu.sicnu.weather;

import android.content.res.Resources;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CityDirectory {
    static final class City {
        int id;
        int pid;
        @SerializedName("city_name") String name;
        @SerializedName("city_code") String code;
        transient String region;

        boolean matches(String query) {
            return name.contains(query) || code.contains(query) || region.contains(query);
        }
    }

    static List<City> read(Resources resources) throws IOException {
        try (Reader reader = new InputStreamReader(
                resources.openRawResource(R.raw.citycode), StandardCharsets.UTF_8)) {
            City[] records = new Gson().fromJson(reader, City[].class);
            if (records == null) throw new IOException("城市文件为空");
            Map<Integer, City> byId = new HashMap<>();
            for (City city : records) byId.put(city.id, city);

            List<City> cities = new ArrayList<>();
            for (City city : records) {
                if (city.code == null || city.code.isEmpty()) continue;
                city.region = regionOf(city, byId);
                cities.add(city);
            }
            return cities;
        }
    }

    private static String regionOf(City city, Map<Integer, City> byId) {
        ArrayDeque<String> parents = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        visited.add(city.id);
        City parent = byId.get(city.pid);
        while (parent != null && visited.add(parent.id)) {
            parents.addFirst(parent.name);
            parent = byId.get(parent.pid);
        }
        return String.join(" · ", parents);
    }
}
