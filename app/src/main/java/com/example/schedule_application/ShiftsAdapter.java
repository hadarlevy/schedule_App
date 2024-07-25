package com.example.schedule_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ShiftsAdapter extends BaseAdapter {

    private Context context;
    private List<Shift> shiftList;

    public ShiftsAdapter(Context context, List<Shift> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }

    @Override
    public int getCount() {
        return shiftList.size();
    }

    @Override
    public Object getItem(int position) {
        return shiftList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.shift_item, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.shift_icon);
        TextView shiftNo = convertView.findViewById(R.id.shift_no);
        TextView shiftDate = convertView.findViewById(R.id.shift_date);

        Shift shift = (Shift) getItem(position);

        // Format the Date object to a String
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(shift.getDate());

        // Set the data to the views
        shiftNo.setText("Shift no: " + (position + 1));
        shiftDate.setText(formattedDate);

        return convertView;
    }
}
