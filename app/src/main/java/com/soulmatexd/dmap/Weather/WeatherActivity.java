package com.soulmatexd.dmap.Weather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.soulmatexd.dmap.R;
import com.soulmatexd.dmap.Utils.KeyBoardUtil;
import com.soulmatexd.dmap.Weather.Presenter.WeatherPresenter;
import com.soulmatexd.dmap.Weather.View.IWeatherView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherActivity extends AppCompatActivity implements IWeatherView{

    @BindView(R.id.city_input)
    EditText cityInput;
    @BindView(R.id.search_weather)
    Button searchWeather;
    @BindView(R.id.city_name)
    TextView cityName;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.weather)
    TextView weather;
    @BindView(R.id.temperature)
    TextView temperature;
    @BindView(R.id.wind)
    TextView wind;
    @BindView(R.id.humidity)
    TextView humidity;

    private String cityString;

    private WeatherPresenter presenter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        presenter = new WeatherPresenter(this);

    }

    @OnClick(R.id.search_weather)
    public void onViewClicked() {
        presenter.searchWeather();
    }


    @Override
    public String getCityName() {
        cityString = cityInput.getText().toString();
        return cityString;
    }

    @Override
    public void onSearchSuccess(LocalWeatherLiveResult result) {
        LocalWeatherLive weatherlive = result.getLiveResult();
        cityName.setText(cityString);
        time.setText(weatherlive.getReportTime()+"发布");
        weather.setText(weatherlive.getWeather());
        temperature.setText(weatherlive.getTemperature()+"°");
        wind.setText(weatherlive.getWindDirection()+"风     "+weatherlive.getWindPower()+"级");
        humidity.setText("湿度         "+weatherlive.getHumidity()+"%");
    }

    @Override
    public void onSearchFailure() {
        cityName.setText("给我认真查询啊喂！");
    }

    @Override
    public WeatherSearch getWeatherSearch() {
        return new WeatherSearch(this);
    }

    @Override
    public void clearEditText() {
        cityInput.setText("");
    }

    @Override
    public void hideKeyboard() {
        KeyBoardUtil.hideSoftKeyboard(this);
    }
}
