package com.palprotech.heylaapp.adapter;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.palprotech.heylaapp.R;
import com.palprotech.heylaapp.app.AppController;
import com.palprotech.heylaapp.bean.support.Event;
import com.palprotech.heylaapp.helper.HeylaAppHelper;
import com.palprotech.heylaapp.utils.HeylaAppValidator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zahid.r on 10/30/2015.
 */
public class EventsListAdapter extends BaseAdapter {

    private static final String TAG = EventsListAdapter.class.getName();
    private final Transformation transformation;
    private Context context;
    private ArrayList<Event> events;
    String className;
    private boolean mSearching = false;
    private boolean mAnimateSearch = false;
    private ArrayList<Integer> mValidSearchIndices = new ArrayList<Integer>();
    private ImageLoader imageLoader = AppController.getInstance().getUniversalImageLoader();

    public EventsListAdapter(Context context, ArrayList<Event> events, String className) {
        this.context = context;
        this.events = events;
        this.className = className;

        transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(0)
                .oval(false)
                .build();
        mSearching = false;
    }

    @Override
    public int getCount() {
        if (mSearching) {

            if (!mAnimateSearch) {
                mAnimateSearch = true;
            }
            return mValidSearchIndices.size();

        } else {

            return events.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mSearching) {
            return events.get(mValidSearchIndices.get(position));
        } else {
            return events.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.event_list_item, parent, false);

            holder = new ViewHolder();
            holder.dateLayout = convertView.findViewById(R.id.date_layout);
            holder.txtEventName = (TextView) convertView.findViewById(R.id.txt_event_name);
            holder.txtEventVenue = (TextView) convertView.findViewById(R.id.txt_event_location);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txt_event_date);
            holder.txtEndDate = (TextView) convertView.findViewById(R.id.txt_event_end_date);
            holder.txtMonth = convertView.findViewById(R.id.txt_event_month);
            holder.txtEndMonth = convertView.findViewById(R.id.txt_event_end_month);
            holder.txtTime = (TextView) convertView.findViewById(R.id.txt_event_time);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img_logo);
            holder.paidBtn = (Button) convertView.findViewById(R.id.event_paid_btn);
            holder.txtPrice = convertView.findViewById(R.id.txt_event_price);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mSearching) {

            position = mValidSearchIndices.get(position);

        } else {
            Log.d("Event List Adapter", "getview pos called" + position);
        }

        Event event = events.get(position);

        holder.txtEventName.setText(events.get(position).getEventName());

        String[] aux = events.get(position).getEventVenue().toString().split(",\\s*");
        String result = "";
        if (aux.length > 2) {
            result = aux[aux.length - 2];
        } else {
            result = aux[aux.length - 1];
        }
        holder.txtEventVenue.setText(result);

        String paidBtnVal = event.getEventType();
        if (paidBtnVal != null) {
            holder.txtPrice.setText(paidBtnVal);
            if (paidBtnVal.equalsIgnoreCase("invite")) {
                holder.paidBtn.setTextColor(context.getResources().getColor(R.color.white)); //Blue
            } else if (paidBtnVal.equalsIgnoreCase("free")) {
                holder.paidBtn.setTextColor(context.getResources().getColor(R.color.white)); //Green
            } else if (paidBtnVal.equalsIgnoreCase("paid")) {
                holder.paidBtn.setTextColor(context.getResources().getColor(R.color.white)); //rounder_button
            }
        }

        if (className.equalsIgnoreCase("HotspotFragment")) {
            holder.dateLayout.setVisibility(View.INVISIBLE);
        }
        else {
            holder.dateLayout.setVisibility(View.VISIBLE);
        }

        if (HeylaAppValidator.checkNullString(events.get(position).getEventBanner())) {
            Picasso.with(this.context).load(events.get(position).getEventBanner()).fit().transform(this.transformation).placeholder(R.drawable.heyla_logo_transparent).error(R.drawable.heyla_logo_transparent).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.heyla_logo_transparent);
        }
        String start = HeylaAppHelper.getDate(events.get(position).getStartDate());
        String end = HeylaAppHelper.getDate(events.get(position).getEndDate());

        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            Date date = (Date) formatter.parse(start);
            Date date1 = (Date) formatter.parse(end);
            SimpleDateFormat month_date = new SimpleDateFormat("MMM");
            SimpleDateFormat event_date = new SimpleDateFormat("dd");
            String month_name = month_date.format(date.getTime());
            String date_name = event_date.format(date.getTime());
            String month_end_name = month_date.format(date1.getTime());
            String date_end_name = event_date.format(date1.getTime());
            if ((start != null) && (end != null)) {
                holder.txtDate.setText(date_name);
                holder.txtMonth.setText(month_name);
                holder.txtEndDate.setText(date_end_name);
                holder.txtEndMonth.setText(month_end_name);
            } else {
                holder.txtDate.setText("N/A");
            }
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        start = events.get(position).getStartTime();
        end = events.get(position).getEndTime();

        holder.txtTime.setText(start + " - " + end);

        return convertView;
    }

    public void startSearch(String eventName) {
        mSearching = true;
        mAnimateSearch = false;
        Log.d("EventListAdapter", "serach for event" + eventName);
        mValidSearchIndices.clear();
        for (int i = 0; i < events.size(); i++) {
            String eventname = events.get(i).getEventName();
            if ((eventname != null) && !(eventname.isEmpty())) {
                if (eventname.toLowerCase().contains(eventName.toLowerCase())) {
                    mValidSearchIndices.add(i);
                }
            }
        }
        Log.d("Event List Adapter", "notify" + mValidSearchIndices.size());
    }

    public void exitSearch() {
        mSearching = false;
        mValidSearchIndices.clear();
        mAnimateSearch = false;
        // notifyDataSetChanged();
    }

    public void clearSearchFlag() {
        mSearching = false;
    }

    public class ViewHolder {
        public TextView txtEventName, txtEventVenue, txtDate, txtMonth, txtTime , txtPrice, txtEndDate, txtEndMonth;
        public RelativeLayout dateLayout;
        public ImageView imageView;
        public Button paidBtn;
    }

    public boolean ismSearching() {
        return mSearching;
    }

    public int getActualEventPos(int selectedSearchpos) {
        if (selectedSearchpos < mValidSearchIndices.size()) {
            return mValidSearchIndices.get(selectedSearchpos);
        } else {
            return 0;
        }
    }
}
