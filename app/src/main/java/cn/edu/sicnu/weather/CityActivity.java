package cn.edu.sicnu.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CityActivity extends Activity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final List<CityDirectory.City> visible = new ArrayList<>();
    private List<CityDirectory.City> cities = Collections.emptyList();
    private final CityAdapter adapter = new CityAdapter();
    private EditText search;
    private TextView count;
    private TextView empty;
    private boolean loaded;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_city);
        PageInsets.apply(this);
        search = findViewById(R.id.search);
        count = findViewById(R.id.count);
        empty = findViewById(R.id.empty);
        ListView list = findViewById(R.id.cities);
        list.setAdapter(adapter);
        list.setEmptyView(empty);
        list.setOnItemClickListener((parent, row, position, id) -> open(visible.get(position)));
        favorite(R.id.chengdu, "101270101");
        favorite(R.id.beijing, "101010100");
        favorite(R.id.tianjin, "101030100");
        search.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable text) { filter(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        worker.execute(() -> {
            try {
                List<CityDirectory.City> result = CityDirectory.read(getResources());
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    cities = result;
                    loaded = true;
                    filter();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    count.setText("城市列表读取失败");
                    empty.setText("请重新打开应用");
                    findViewById(R.id.favorites).setVisibility(View.GONE);
                });
            }
        });
    }

    private void favorite(int button, String code) {
        findViewById(button).setOnClickListener(v -> {
            for (CityDirectory.City city : cities) {
                if (code.equals(city.code)) { open(city); return; }
            }
        });
    }

    private void filter() {
        if (!loaded) return;
        String query = search.getText().toString().trim();
        visible.clear();
        for (CityDirectory.City city : cities) {
            if (city.matches(query)) visible.add(city);
        }
        findViewById(R.id.favorites).setVisibility(query.isEmpty() ? View.VISIBLE : View.GONE);
        count.setText((query.isEmpty() ? "全部城市" : "搜索结果") + "  ·  " + visible.size());
        empty.setText("未找到匹配的城市，请更换关键词");
        adapter.notifyDataSetChanged();
    }

    private void open(CityDirectory.City city) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(search.getWindowToken(), 0);
        Intent detail = new Intent(this, WeatherActivity.class);
        detail.putExtra("name", city.name);
        detail.putExtra("code", city.code);
        detail.putExtra("region", city.region);
        startActivity(detail);
    }

    @Override protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }

    private final class CityAdapter extends BaseAdapter {
        @Override public int getCount() { return visible.size(); }
        @Override public CityDirectory.City getItem(int position) { return visible.get(position); }
        @Override public long getItemId(int position) { return getItem(position).id; }
        @Override public View getView(int position, View row, ViewGroup parent) {
            if (row == null) row = getLayoutInflater().inflate(R.layout.city_row, parent, false);
            CityDirectory.City city = getItem(position);
            ((TextView) row.findViewById(R.id.city_name)).setText(city.name);
            String prefix = city.region.isEmpty() ? "" : city.region + "  ·  ";
            ((TextView) row.findViewById(R.id.region)).setText(prefix + city.code);
            return row;
        }
    }
}
