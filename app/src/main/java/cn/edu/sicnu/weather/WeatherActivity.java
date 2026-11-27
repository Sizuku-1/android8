package cn.edu.sicnu.weather;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import java.util.ArrayList;
import java.util.List;

public final class WeatherActivity extends Activity {
    private RequestQueue queue;
    private WeatherRequest request;
    private String code;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_weather);
        PageInsets.apply(this);
        code = getIntent().getStringExtra("code");
        if (code == null || !code.matches("[0-9]{9}")) { finish(); return; }
        text(R.id.place, getIntent().getStringExtra("name"));
        String region = getIntent().getStringExtra("region");
        text(R.id.location, (region == null || region.isEmpty() ? "" : region + "  ·  ") + code);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.refresh).setOnClickListener(v -> load());
        findViewById(R.id.retry).setOnClickListener(v -> load());
        queue = Volley.newRequestQueue(getApplicationContext());
        load();
    }

    private void load() {
        if (request != null) request.cancel();
        findViewById(R.id.details).setVisibility(View.GONE);
        findViewById(R.id.state).setVisibility(View.VISIBLE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        findViewById(R.id.retry).setVisibility(View.GONE);
        findViewById(R.id.refresh).setEnabled(false);
        text(R.id.message, "正在查询天气…");
        request = new WeatherRequest(code, this::showReport, this::showError);
        queue.add(request);
    }

    private void showError(VolleyError error) {
        findViewById(R.id.progress).setVisibility(View.GONE);
        findViewById(R.id.retry).setVisibility(View.VISIBLE);
        findViewById(R.id.refresh).setEnabled(true);
        if (error instanceof WeatherRequest.NoWeather) {
            text(R.id.message, "气象站暂未提供该城市的数据");
        } else if (error instanceof ParseError) {
            text(R.id.message, "天气数据格式异常，请稍后重试");
        } else {
            text(R.id.message, "天气加载失败，请检查网络后重试");
        }
    }

    private void showReport(WeatherReport report) {
        WeatherReport.Observations data = report.data;
        List<WeatherReport.Day> days = new ArrayList<>();
        if (data.forecast != null) {
            for (WeatherReport.Day day : data.forecast) if (day != null) days.add(day);
        }
        WeatherReport.Day first = days.isEmpty() ? new WeatherReport.Day() : days.get(0);
        text(R.id.temp, unit(data.wendu, "°"));
        text(R.id.condition, value(first.type));
        text(R.id.range, "最低 " + degrees(first.low) + "   最高 " + degrees(first.high));
        ImageView icon = findViewById(R.id.weather_icon);
        int resource = iconFor(first.type);
        icon.setVisibility(resource == 0 ? View.GONE : View.VISIBLE);
        if (resource != 0) icon.setImageResource(resource);

        String date = value(report.date);
        if (date.matches("[0-9]{8}")) {
            date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
        }
        String time = report.cityInfo == null ? null : report.cityInfo.updateTime;
        text(R.id.updated, "气象站更新  " + date + "  " + value(time));
        text(R.id.metrics, "湿度  " + value(data.shidu) + "     空气质量  " + value(data.quality)
                + "\n风向风力  " + value(first.fx) + " " + value(first.fl)
                + "\nPM2.5  " + value(data.pm25) + "     PM10  " + value(data.pm10)
                + "\n日出  " + value(first.sunrise) + "     日落  " + value(first.sunset));
        showForecast(days);
        findViewById(R.id.state).setVisibility(View.GONE);
        findViewById(R.id.details).setVisibility(View.VISIBLE);
        findViewById(R.id.refresh).setEnabled(true);
    }

    private void showForecast(List<WeatherReport.Day> days) {
        LinearLayout list = findViewById(R.id.forecast);
        list.removeAllViews();
        text(R.id.forecast_heading, days.isEmpty() ? "暂无逐日预报" : "逐日预报  ·  " + days.size() + " 天");
        for (WeatherReport.Day day : days) {
            View row = getLayoutInflater().inflate(R.layout.forecast_row, list, false);
            String date = value(day.ymd);
            if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) date = date.substring(5);
            String week = value(day.week).replace("星期", "周");
            ((TextView) row.findViewById(R.id.day)).setText(date + "  " + week);
            ((TextView) row.findViewById(R.id.sky)).setText(value(day.type));
            ((TextView) row.findViewById(R.id.temps)).setText(degrees(day.low) + " / " + degrees(day.high));
            list.addView(row);
        }
    }

    private static int iconFor(String condition) {
        String sky = value(condition);
        if (sky.contains("雷")) return R.drawable.thunder;
        if (sky.contains("雪")) return R.drawable.snow;
        if (sky.contains("雨")) return R.drawable.rain;
        if (sky.contains("多云")) return R.drawable.cloud;
        if (sky.contains("晴")) return R.drawable.sun;
        if (sky.contains("阴") || sky.contains("雾") || sky.contains("霾")) return R.drawable.mcloud;
        return 0;
    }

    private static String value(String raw) {
        return raw == null || raw.trim().isEmpty() ? "—" : raw.trim();
    }

    private static String unit(String raw, String suffix) {
        return value(raw).equals("—") ? "—" : raw.trim() + suffix;
    }

    private static String degrees(String raw) {
        return value(raw).replace("高温", "").replace("低温", "").replace("℃", "°").trim();
    }

    private void text(int id, String content) {
        ((TextView) findViewById(id)).setText(content);
    }

    @Override protected void onDestroy() {
        if (request != null) request.cancel();
        if (queue != null) queue.stop();
        super.onDestroy();
    }
}
