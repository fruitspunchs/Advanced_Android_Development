/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listens to DataItems and Messages from the local node.
 */
public class DataLayerListenerService extends WearableListenerService {
    private static final String WEATHER_CONDITION_KEY = "WEATHER_CONDITION_KEY";
    private static final String MIN_TEMP_KEY = "MIN_TEMP_KEY";
    private static final String MAX_TEMP_KEY = "MAX_TEMP_KEY";

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void saveDataMap(DataMap dataMap) {
        int weatherId = dataMap.getInt(WEATHER_CONDITION_KEY);
        int high = dataMap.getInt(MAX_TEMP_KEY);
        int low = dataMap.getInt(MIN_TEMP_KEY);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putInt(WEATHER_CONDITION_KEY, weatherId)
                .putInt(MAX_TEMP_KEY, high)
                .putInt(MIN_TEMP_KEY, low)
                .apply();


        Intent intent = new Intent(this, MyWatchFace.class);
        intent.setAction(MyWatchFace.ACTION_UPDATE);
        this.startService(intent);
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        String LOG_TAG = this.getClass().getSimpleName();
        Log.d(LOG_TAG, "onDataChanged: " + dataEvents);

        // Loop through the events and send a message back to the node that created the data item
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/weather") == 0) {
                    Log.d(LOG_TAG, "Weather data received");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    saveDataMap(dataMap);

                }
            }
        }
    }
}

