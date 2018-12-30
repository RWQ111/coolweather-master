package com.bogaara.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bogaara.coolweather.db.City;
import com.bogaara.coolweather.db.County;
import com.bogaara.coolweather.db.MyDataBase;
import com.bogaara.coolweather.db.Province;
import com.bogaara.coolweather.util.HttpUtil;
import com.bogaara.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment{

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    public static final int LEVEL_CATCHE = 3;

    public static final int LEVEL_SELECT = 4;

    public static final int LEVEL_COLLECTION = 5;

    private ProgressDialog progressDialog;

    private int currentLevel;

    private TextView titleText;

    private Button backButton;

    private Button catcheButton;

    private Button selectButton;

    private Button colloctButton;

    private Button goBack;

    private EditText selectCountyName;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    private Province  selectedProvince;

    private City selectedCity;

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private List<County> countyAllList = new ArrayList<>();

    private int select_city_id;

    private String select_county_name;

    private MyDataBase myDataBase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        setHasOptionsMenu(true);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);

        catcheButton = (Button)view.findViewById(R.id.catche_button);
        selectButton = (Button)view.findViewById(R.id.select);
        selectCountyName = (EditText)view.findViewById(R.id.select_county_name);
        colloctButton = (Button)view.findViewById(R.id.collect_button);
        myDataBase=new MyDataBase(getActivity());
        goBack = (Button)view.findViewById(R.id.go_back_button);

        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        init();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("list",";list");
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY||currentLevel==LEVEL_COLLECTION){
                    String weathreId = countyList.get(i).getWeatherId();
                    int city_id = countyList.get(i).getCityId();
                    String ci = String.valueOf(city_id);
                    Catche(countyList.get(i).getCountyName(),ci);
                  /*  for(County county:countyList){
                        Log.d("duan3",county.getCountyName());
                    }*/
                   if (getActivity() instanceof MainActivity){
                       Intent intent = new Intent(getActivity(), WeatherActivity.class);
                       intent.putExtra("weather_id", weathreId);
                       intent.putExtra("cityId",ci);
                       intent.putExtra("countyName",countyList.get(i).getCountyName());
                       startActivity(intent);
                       getActivity().finish();
                   }else if (getActivity() instanceof WeatherActivity){
                       WeatherActivity activity = (WeatherActivity) getActivity();
                       activity.drawerLayout.closeDrawers();
                       activity.swipeRefresh.setRefreshing(true);
                       activity.requestWeather(weathreId);
                       activity.SetCollect(ci,countyList.get(i).getCountyName());
                   }
                }else if(currentLevel == LEVEL_CATCHE){
                    String weatherId = getCatcheWeatherId(i);
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        intent.putExtra("cityId",String.valueOf(select_city_id));
                        intent.putExtra("countyName",select_county_name);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                        activity.SetCollect(String.valueOf(select_city_id),select_county_name);
                    }
                }else if(currentLevel == LEVEL_SELECT){
                    String weatherId="";
                    String county_select_id="";
                    String city_select_id="";
                    String county_select_name="";
                    querySelect();
                    countyList = DataSupport.where("cityId = ?", String.valueOf(select_city_id)).find(County.class);
                    for (County county : countyList){
                        if(county.getCountyName().equals(select_county_name)){
                            weatherId = county.getWeatherId();
                            county_select_id=String.valueOf(county.getId());
                            city_select_id = String.valueOf(county.getCityId());
                            county_select_name = county.getCountyName();
                            break;
                        }
                    }
                    Catche(select_county_name,String.valueOf(select_city_id));
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        intent.putExtra("cityId",city_select_id);
                        intent.putExtra("countyName",county_select_name);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                        activity.SetCollect(city_select_id,county_select_name);
                    }
                }
            }
        });

        catcheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryCatche();
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querySelect();
            }
        });

        colloctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryCollection();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryProvinces();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }else if(currentLevel == LEVEL_CATCHE){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.INVISIBLE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_CITY;
            }
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryCatche() {
        titleText.setText("记录");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String p1 = prefs.getString("1", null);
        String p2 = prefs.getString("2", null);
        String p3 = prefs.getString("3", null);
        if (p1 == null) {
            return;
        }
        dataList.clear();
        if (p1 != null)
            dataList.add(p1);
        if (p2 != null)
            dataList.add(p2);

        if (p3 != null)
            dataList.add(p3);
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_CATCHE;

    }

    private void querySelect(){
        titleText.setText("");
        select_county_name=null;
        int k=0;
        for(County county : countyAllList){
            if(county.getCountyName().equals(selectCountyName.getText().toString())){
                select_city_id = county.getCityId();
                select_county_name = county.getCountyName();
                break;
            }
        }
        if(select_county_name==null){
            Toast.makeText(getActivity(),"未找到该地区",Toast.LENGTH_SHORT).show();
            currentLevel = LEVEL_PROVINCE;
        }
        countyList = DataSupport.where("cityId = ?", String.valueOf(select_city_id)).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                if(county.getCountyName().equals(select_county_name)){
                    dataList.add(county.getCountyName());
                    break;
                }
            }
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        currentLevel = LEVEL_SELECT;
    }

    private void queryCollection(){
        titleText.setText("");
 //       List<County> p=new ArrayList<>();
        countyList = myDataBase.getArray();
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
/*            for(County county1:countyList)
                for(County county:countyAllList){
                    if(county1.getCountyName().equals(county.getCountyName()))
                        p.add(county);
            }*/
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
 ///           countyList = p;
            currentLevel = LEVEL_COLLECTION;
        }
    }

    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void _queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
        //                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                provinceList = DataSupport.findAll(Province.class);
                            }else if("city".equals(type)){
                                cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
                            }else if("county".equals(type)){
                                countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.show();
        }
    }

    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void init(){
        provinceList = DataSupport.findAll(Province.class);
        countyAllList.clear();
   /*    if(provinceList.size()<=0){
            String address = "http://guolin.tech/api/china";
            _queryFromServer(address, "province");
        }*/
        for (Province province : provinceList) {
            selectedProvince = province;
            cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
      /*     if(cityList.size()<=0){
                int provinceCode = selectedProvince.getProvinceCode();
                String address = "http://guolin.tech/api/china/" + provinceCode;
                _queryFromServer(address, "city");
            }*/
            for (City city : cityList) {
                selectedCity = city;
                countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
          /*    if(countyList.size()<=0){
                    int provinceCode = selectedProvince.getProvinceCode();
                    int cityCode = selectedCity.getCityCode();
                    String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
                    _queryFromServer(address, "county");
                }*/
                for (County county : countyList) {
                    countyAllList.add(county);
                }
            }
        }
//        provinceList=null;
 //       cityList=null;
  //      countyList=null;
    }

    private void Catche(String pi,String ci){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String p1 = prefs.getString("1",null);
        String c1 = prefs.getString("c1",null);
        String p2 = prefs.getString("2",null);
        String c2 = prefs.getString("c2",null);
        String p3 = prefs.getString("3",null);
        String c3 = prefs.getString("c3",null);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        if(p1==null){
            editor.putString("1",pi);
            editor.putString("c1",ci);
        }else if(p2==null){
            editor.putString("2",pi);
            editor.putString("c2",ci);
        }else if(p3==null){
            editor.putString("3",pi);
            editor.putString("c3",ci);
        }else {
            editor.putString("1",p2);
            editor.putString("c1",c2);
            editor.putString("2",p3);
            editor.putString("c2",c3);
            editor.putString("3",pi);
            editor.putString("c3",ci);
        }
        editor.apply();
    }

    private String getCatcheWeatherId(int i){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String p1 = prefs.getString("1",null);
        String c1 = prefs.getString("c1",null);
        String p2 = prefs.getString("2",null);
        String c2 = prefs.getString("c2",null);
        String p3 = prefs.getString("3",null);
        String c3 = prefs.getString("c3",null);

        String weatherId="";
        int catche_city_id=0;
        String catche_county_name="";
        if(i==0&&p1!=null){
            catche_city_id = Integer.valueOf(c1);
            catche_county_name = p1;
        }else if(i==1&&p2!=null){
            catche_city_id = Integer.valueOf(c2);
            catche_county_name = p2;
        }else if(i==2&&p3!=null){
            catche_city_id = Integer.valueOf(c3);
            catche_county_name = p3;
        }
        countyList = DataSupport.where("cityId = ?", String.valueOf(catche_city_id)).find(County.class);
        String p = "";
        for (County county : countyList){
            if(county.getCountyName().equals(catche_county_name)){
                weatherId = county.getWeatherId();
                Log.d("duan4",weatherId);
                p = county.getCountyName();
                break;
            }
        }
        select_city_id = catche_city_id;
        select_county_name = p;
        return weatherId;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.catche_item:
                Toast.makeText(getActivity(),"you clicked Add",Toast.LENGTH_SHORT).show();
                break;
            case R.id.collection_item:
                Toast.makeText(getActivity(),"you clicked Remove",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;

    }
}

