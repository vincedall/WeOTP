package com.github.weotp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<ListItem> {
    private Context context;
    private ArrayList<ListItem> itemsArrayList;
    private Canvas canvas;
    private Paint paint = new Paint();
    private Bitmap bitmap;

    public CustomAdapter(Context context, ArrayList<ListItem> itemsArrayList) {
        super(context, R.layout.list_item, itemsArrayList);
        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        ImageView imageView = view.findViewById(R.id.image);
        paint = new Paint();
        paint.setColor(Color.argb(125, 125, 125, 125));
        bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(bitmap);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawArc(0, 0, 100, 100, 270,
                itemsArrayList.get(position).getSeconds() * 12, true, paint);
        TextView appName = view.findViewById(R.id.appName);
        appName.setText(itemsArrayList.get(position).getAppName());
        TextView otp = view.findViewById(R.id.otp);
        otp.setText(itemsArrayList.get(position).getOtp());
        return view;
    }
}












