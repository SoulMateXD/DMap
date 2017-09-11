package com.soulmatexd.dmap;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.soulmatexd.dmap.Utils.KeyBoardUtil;
import com.soulmatexd.dmap.Weather.WeatherActivity;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener , AMap.OnInfoWindowClickListener{
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    AMap aMap = null;
    @BindView(R.id.map)
    MapView mMapView;
    @BindView(R.id.weather_cardview)
    CardView weatherCardview;
    @BindView(R.id.search_poi_cardview)
    CardView searchPoiCardview;
    @BindView(R.id.rotateloading)
    RotateLoading rotateloading;
    @BindView(R.id.search_poi_edit)
    EditText searchPoiEdit;
    @BindView(R.id.search_poi_button)
    Button searchPoiButton;

    private double latitude; //纬度，在前
    private double longitude; //经度，在后
    private LatLng currentLatLng;
    private String cityCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //获取地图控件引用
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        initMap();
        //用于首次定位
        initLocationClient();


    }
    private void searchPOI() {
        String searchString;
        searchString = searchPoiEdit.getText().toString();
        if (searchString.equals("")){
            Toast.makeText(this, "查询地点不可为空哦", Toast.LENGTH_SHORT).show();
        }else {
            PoiSearch.Query query = new PoiSearch.Query(searchString, "", cityCode);
            query.setPageSize(10);
            query.setPageNum(1);
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
            showLoading();
            KeyBoardUtil.hideSoftKeyboard(this);
        }

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        currentLatLng = new LatLng(latitude, longitude);
        if (i == 1000) {
            ArrayList<PoiItem> poiItems = poiResult.getPois();
            if(poiItems.toString().equals("[]")){
                Toast.makeText(this, "没有查询到数据哦", Toast.LENGTH_SHORT).show();
                stopLoading();
                return;
            }
            aMap.clear();

            //用于计算最短距离的点
            float minDistance = Float.MAX_VALUE;
            PoiItem shortestItem = null;

            for (PoiItem poiItem : poiItems) {
                //将搜索到的poi点画到map上
                LatLonPoint latLonPoint = poiItem.getLatLonPoint();
                Marker marker = aMap.addMarker(new MarkerOptions().position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()))
                        .title(poiItem.toString()).snippet("点击导航"));
//
                Animation animation = new ScaleAnimation(0, 1, 0, 1);
                long duration = 1000L;
                animation.setDuration(duration);
                animation.setInterpolator(new LinearInterpolator());
                marker.setAnimation(animation);
                marker.startAnimation();

                //计算最短距离
                float currentDistance = AMapUtils.calculateLineDistance(currentLatLng, marker.getPosition());
                if (currentDistance < minDistance){
                    shortestItem = poiItem;
                    minDistance = currentDistance;
                }
            }
            //绑定信息窗点击事件
            aMap.setOnInfoWindowClickListener(this);

            //移动到最短距离
            mapPointMoveTo(shortestItem.getLatLonPoint().getLatitude(), shortestItem.getLatLonPoint().getLongitude());
            stopLoading();
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void initMap() {
        aMap = mMapView.getMap();
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        locationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        locationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//定位按钮
        aMap.setMyLocationStyle(locationStyle);
        aMap.setMyLocationEnabled(true);

        //经纬度监听
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }


    // 定位相关代码
    private void initLocationClient() {
        mLocationClient = null;
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());

        //设置定位回调监听
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
//                        String info = "";
//                        info = info + "/  " + amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                        info = info + "/  " + amapLocation.getLatitude();//获取纬度
//                        info = info + "/  " + amapLocation.getLongitude();//获取经度
//                        info = info + "/  " + amapLocation.getAccuracy();//获取精度信息
//                        info = info + "/  " + amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                        info = info + "/  " + amapLocation.getCountry();//国家信息
//                        info = info + "/  " + amapLocation.getProvince();//省信息
//                        info = info + "/  " + amapLocation.getCity();//城市信息
//                        info = info + "/  " + amapLocation.getDistrict();//城区信息
//                        info = info + "/  " + amapLocation.getStreet();//街道信息
//                        info = info + "/  " + amapLocation.getStreetNum();//街道门牌号信息
//                        info = info + "/  " + amapLocation.getCityCode();//城市编码
//                        info = info + "/  " + amapLocation.getAdCode();//地区编码
//                        info = info + "/  " + amapLocation.getAoiName();//获取当前定位点的AOI信息
//                        info = info + "/  " + amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
//                        info = info + "/  " + amapLocation.getFloor();//获取当前室内定位的楼层
//                        //获取定位时间
//                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        Date date = new Date(amapLocation.getTime());
//                        info = info + "/  " + df.format(date);

                        //定位成功后移动位置
                        latitude = amapLocation.getLatitude();
                        longitude = amapLocation.getLongitude();
                        cityCode = amapLocation.getCityCode();
                        mapPointMoveTo(latitude, longitude);
                    } else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }

            }
        };
        mLocationClient.setLocationListener(mLocationListener);

        //设置定位Option
        mLocationOption = new AMapLocationClientOption();
        //只定位一次
        mLocationOption.setOnceLocation(true);
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
//        mLocationOption.setInterval(1000);
        mLocationOption.setNeedAddress(true);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @OnClick({R.id.weather_cardview, R.id.search_poi_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.weather_cardview:
                startActivity(new Intent(MainActivity.this, WeatherActivity.class));
                break;
            case R.id.search_poi_button:
                searchPOI();
                searchPoiEdit.setText("");
                break;
        }
    }

    public void mapPointMoveTo(double latitude, double longtitude) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude, longtitude), 12, 0, 0));
        aMap.moveCamera(cameraUpdate);
    }

    public void showLoading() {
        rotateloading.start();
        searchPoiButton.setClickable(false);
        weatherCardview.setClickable(false);
    }

    public void stopLoading() {
        rotateloading.stop();
        searchPoiButton.setClickable(true);
        weatherCardview.setClickable(true);
    }

    //在地图上对单个Marker的点击回调函数
    @Override
    public void onInfoWindowClick(Marker marker) {
        Poi start = new Poi("我的位置", currentLatLng, "");

        Poi end = new Poi(marker.getTitle(), marker.getPosition(), "");
        AmapNaviPage.getInstance().showRouteActivity(MainActivity.this,
                new AmapNaviParams(start, null, end, AmapNaviType.RIDE), new INaviInfoCallback() {
                    @Override
                    public void onInitNaviFailure() {

                    }

                    @Override
                    public void onGetNavigationText(String s) {

                    }

                    @Override
                    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

                    }

                    @Override
                    public void onArriveDestination(boolean b) {

                    }

                    @Override
                    public void onStartNavi(int i) {

                    }

                    @Override
                    public void onCalculateRouteSuccess(int[] ints) {

                    }

                    @Override
                    public void onCalculateRouteFailure(int i) {

                    }

                    @Override
                    public void onStopSpeaking() {

                    }
                });
    }
}
