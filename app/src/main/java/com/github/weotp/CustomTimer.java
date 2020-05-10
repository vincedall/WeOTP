package com.github.weotp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class CustomTimer extends CountDownTimer {
    private ArrayList<ListItem> itemsList;
    private CustomAdapter adapter;
    private Context context;

    public CustomTimer(long millisInFuture, long countDownInterval, ArrayList<ListItem> itemsList,
                       CustomAdapter adapter, Context context){
        super(millisInFuture, countDownInterval);
        this.itemsList = itemsList;
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        for (ListItem item : itemsList) {
            item.setSeconds((int) (30 - (millisUntilFinished / 1000)));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFinish() {
        for (ListItem item : itemsList){
            SharedPreferences prefs = context.getSharedPreferences(item.getAppName(), MODE_PRIVATE);
            String key = prefs.getString("key", "");
            long counter = System.currentTimeMillis() / 30000L;
            HOTP hotp = new HOTP(key, counter, 6);
            hotp.update();
            item.setOtp(hotp.getHOTP());
            item.setSeconds(0);
        }
        CustomTimer timer = new CustomTimer(30000 - (System.currentTimeMillis() % 30000),
                1000, itemsList, adapter, context);
        timer.start();
    }
}
