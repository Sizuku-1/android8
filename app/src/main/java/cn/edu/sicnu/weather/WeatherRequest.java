package cn.edu.sicnu.weather;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.nio.charset.StandardCharsets;

final class WeatherRequest extends Request<WeatherReport> {
    static final class NoWeather extends VolleyError {}
    private static final String BASE = "http://t.weather.itboy.net/api/weather/city/";
    private final String code;
    private final Response.Listener<WeatherReport> listener;

    WeatherRequest(String code, Response.Listener<WeatherReport> listener, Response.ErrorListener errors) {
        super(Method.GET, BASE + code, errors);
        this.code = code;
        this.listener = listener;
        setShouldCache(false);
        setRetryPolicy(new DefaultRetryPolicy(7500, 0, 1f));
    }

    @Override protected Response<WeatherReport> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, StandardCharsets.UTF_8);
            WeatherReport report = new Gson().fromJson(json, WeatherReport.class);
            if (report == null || report.status != 200 || report.data == null) {
                return Response.error(new NoWeather());
            }
            if (report.cityInfo != null && report.cityInfo.citykey != null
                    && !code.equals(report.cityInfo.citykey)) {
                return Response.error(new NoWeather());
            }
            return Response.success(report, null);
        } catch (JsonParseException error) {
            return Response.error(new ParseError(error));
        }
    }

    @Override protected void deliverResponse(WeatherReport report) {
        listener.onResponse(report);
    }
}
