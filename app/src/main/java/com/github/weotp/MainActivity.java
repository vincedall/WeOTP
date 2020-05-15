package com.github.weotp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ListView listView;
    private ArrayList<ListItem> itemsList = new ArrayList<>();
    private CustomAdapter adapter;
    private CustomTimer timer;
    private EditText key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list_view);
        setListView();
        setOTPs();
        String[] permission = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(MainActivity.this, permission, 200);
        while(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            try {
                Thread.sleep(1000);
            }catch(Exception e){}
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                dialog.setContentView(R.layout.info_menu);
                dialog.show();
                Button deleteButton = dialog.findViewById(R.id.delete_button);
                Button closeButton = dialog.findViewById(R.id.close_button);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Are you sure?").setMessage("You may wish to save the app key before deleting." +
                                " You can see it by tapping on the blue eye above.");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                new File(MainActivity.this.getFilesDir().getParent() +"/shared_prefs/"+
                                        itemsList.get(position).getAppName() +".xml").delete();
                                setListView();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) { }
                        });
                        AlertDialog d = builder.create();
                        d.show();
                    }
                });
                ImageView showKey = dialog.findViewById(R.id.show);
                showKey.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences prefs = getSharedPreferences(itemsList.get(position).getAppName(), MODE_PRIVATE);
                        String key = prefs.getString("key", "");
                        EditText show = dialog.findViewById(R.id.show_key);
                        show.setText(key);
                    }
                });
            }
        });

        ImageView addKey = findViewById(R.id.add);
        addKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                dialog.setContentView(R.layout.key_menu);
                key = dialog.findViewById(R.id.key);
                final EditText appName = dialog.findViewById(R.id.app_name);
                Button cancelButton = dialog.findViewById(R.id.cancel_button);
                Button addButton = dialog.findViewById(R.id.add_button);
                Button scanButton = dialog.findViewById(R.id.scan_button);
                scanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, Scanner.class);
                        MainActivity.this.startActivityForResult(intent, 0);
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(key.getText().toString().isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Empty key").setMessage("Key is empty try again");
                            AlertDialog d = builder.create();
                            d.show();
                        }else if(appName.getText().toString().isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Empty app name").setMessage("App name must not be empty");
                            AlertDialog d = builder.create();
                            d.show();
                        }else if(new File(MainActivity.this.getFilesDir().getParent() +"/shared_prefs/"+
                                appName.getText().toString() +".xml").exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("App name taken").setMessage("App name is taken choose another");
                            AlertDialog d = builder.create();
                            d.show();
                        }else {
                            SharedPreferences prefs = getSharedPreferences(appName.getText().toString(), MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("key", key.getText().toString().replaceAll("\\s+","")).commit();
                            setListView();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        timer = new CustomTimer(30000 - (System.currentTimeMillis() % 30000),
                1000, itemsList, adapter, this);
        timer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
            if (key != null)
                key.setText(data.getStringExtra("result"));
    }

    public void setOTPs(){
        for (ListItem item : itemsList){
            SharedPreferences prefs = getSharedPreferences(item.getAppName(), MODE_PRIVATE);
            String key = prefs.getString("key", "");
            long counter = System.currentTimeMillis() / 30000L;
            HOTP hotp = new HOTP(key, counter, 6);
            hotp.update();
            item.setOtp(hotp.getHOTP());
        }
        adapter.notifyDataSetChanged();
    }

    public void setListView(){
        itemsList = new ArrayList<>();
        File[] files = new File(this.getFilesDir().getParent() + "/shared_prefs").listFiles();
        if (files != null)
            for (File file : files)
                itemsList.add(new ListItem((int) (System.currentTimeMillis() / 30000) % 30,
                        file.getName().substring(0, file.getName().length() - 4)));
        if (timer != null)
            timer.cancel();
        adapter = new CustomAdapter(this, itemsList);
        listView.setAdapter(adapter);
        setOTPs();
        timer = new CustomTimer(30000 - (System.currentTimeMillis() % 30000),
                1000, itemsList, adapter, this);
        timer.start();
    }

    @Override
    public void handleResult(Result rawResult) {

    }
}















