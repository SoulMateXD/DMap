package com.soulmatexd.dmap.Weather.Presenter;

import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.soulmatexd.dmap.Weather.View.IWeatherView;

/**
 * Created by SoulMateXD on 2017/9/6.
 */

public class WeatherPresenter implements IWeatherPresenter, WeatherSearch.OnWeatherSearchListener{
    private IWeatherView view;

    public WeatherPresenter(IWeatherView view) {
        this.view = view;
    }

    @Override
    public void searchWeather() {
        String city = view.getCityName();
        WeatherSearchQuery query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch search = view.getWeatherSearch();
        search.setQuery(query);
        search.setOnWeatherSearchListener(this);
        search.searchWeatherAsyn();
        clear();
    }

    @Override
    public void clear() {
        view.clearEditText();
    }


    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
        if (i == 1000) {
            if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult().getReportTime() != null) {
                view.onSearchSuccess(localWeatherLiveResult);
                view.hideKeyboard();
                return;
            }
        }

        view.onSearchFailure();
        view.hideKeyboard();
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }
}
