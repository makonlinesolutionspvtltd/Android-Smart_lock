package com.smartlock.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.gson.reflect.TypeToken;
import com.smartlock.R;
import com.smartlock.adapter.KeyAdapter;
import com.smartlock.app.SmartLockApp;
import com.smartlock.dao.DbService;
import com.smartlock.model.Key;
import com.smartlock.model.KeyObj;
import com.smartlock.net.ResponseService;
import com.ttlock.bl.sdk.util.GsonUtil;
import com.ttlock.bl.sdk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NearbyLockActivity extends BaseActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<Nearby_model> nearby_models;

    private List<Key> keys;
    private Context mContext;
    public static Key curKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_lock);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recylerview);

        mContext = NearbyLockActivity.this;
        layoutManager = new LinearLayoutManager(mContext);

      /*  Nearby_model list = new Nearby_model();
        list.setName("Cottage-CTH");
        nearby_models.add(list);*/
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        nearby_models = new ArrayList<>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        init();

    }

    private void init() {
        //turn on bluetooth
        SmartLockApp.mTTLockAPI.requestBleEnable(this);
        LogUtil.d("start bluetooth service", DBG);
        SmartLockApp.mTTLockAPI.startBleService(this);
        //It need location permission to start bluetooth scan,or it can not scan device
        if (requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            SmartLockApp.mTTLockAPI.startBTDeviceScan();
        }

//        accessToken = MyPreference.getStr(this, MyPreference.ACCESS_TOKEN);
        keys = new ArrayList<>();
        syncData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void syncData() {
        showProgressDialog();
        new AsyncTask<Void, String, String>() {

            @Override
            protected String doInBackground(Void... params) {
                //you can synchronizes all key datas when lastUpdateDate is 0
                String json = ResponseService.syncData(0);
                LogUtil.d("json:" + json, DBG);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if (jsonObject.has("errcode")) {
//                        toast(jsonObject.getString("description"));
                        Intent intent = new Intent(NearbyLockActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                        finish();
                        return json;
                    }
                    //use lastUpdateDate you can get the newly added key and data after the time
                    long lastUpdateDate = jsonObject.getLong("lastUpdateDate");
                    String keyList = jsonObject.getString("keyList");
//                    JSONArray jsonArray = jsonObject.getJSONArray("keyList");
                    keys.clear();
                    ArrayList<KeyObj> list = GsonUtil.toObject(keyList, new TypeToken<ArrayList<KeyObj>>() {
                    });
                    keys.addAll(convert2DbModel(list));
                    //clear local keys and save new keys
                    DbService.deleteAllKey();
                    DbService.saveKeyList(keys);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                progressDialog.cancel();
                adapter = new Nearby_Adapter(NearbyLockActivity.this, keys);
//                keyAdapter = new KeyAdapter(MainActivity.this, keys);
                recyclerView.setAdapter(adapter);
                recyclerView.setOnCreateContextMenuListener(NearbyLockActivity.this);
            }
        }.execute();
    }


    private static ArrayList<Key> convert2DbModel(ArrayList<KeyObj> list) {
        ArrayList<Key> keyList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (KeyObj key : list) {
                Key DbKey = new Key();
                DbKey.setUserType(key.userType);
                DbKey.setKeyStatus(key.keyStatus);
                DbKey.setLockId(key.lockId);
                DbKey.setKeyId(key.keyId);
                DbKey.setLockVersion(GsonUtil.toJson(key.lockVersion));
                DbKey.setLockName(key.lockName);
                DbKey.setLockAlias(key.lockAlias);
                DbKey.setLockMac(key.lockMac);
                DbKey.setElectricQuantity(key.electricQuantity);
                DbKey.setLockFlagPos(key.lockFlagPos);
                DbKey.setAdminPwd(key.adminPwd);
                DbKey.setLockKey(key.lockKey);
                DbKey.setNoKeyPwd(key.noKeyPwd);
                DbKey.setDeletePwd(key.deletePwd);
                DbKey.setPwdInfo(key.pwdInfo);
                DbKey.setTimestamp(key.timestamp);
                DbKey.setAesKeyStr(key.aesKeyStr);
                DbKey.setStartDate(key.startDate);
                DbKey.setEndDate(key.endDate);
                DbKey.setSpecialValue(key.specialValue);
                DbKey.setTimezoneRawOffset(key.timezoneRawOffset);
                DbKey.setKeyRight(key.keyRight);
                DbKey.setKeyboardPwdVersion(key.keyboardPwdVersion);
                DbKey.setRemoteEnable(key.remoteEnable);
                DbKey.setRemarks(key.remarks);

                keyList.add(DbKey);
            }
        }
        return keyList;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NearbyLockActivity.this, MainActivity.class));
        finish();
    }
}
