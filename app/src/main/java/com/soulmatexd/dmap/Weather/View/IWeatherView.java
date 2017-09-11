package com.soulmatexd.dmap.Weather.View;

import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;

/**
 * Created by SoulMateXD on 2017/9/6.
 */

public interface IWeatherView {

    String getCityName();

    void onSearchSuccess(LocalWeatherLiveResult result);

    void onSearchFailure();

    WeatherSearch getWeatherSearch();

    void clearEditText();

    void hideKeyboard();
}
