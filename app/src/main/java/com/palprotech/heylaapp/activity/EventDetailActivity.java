package com.palprotech.heylaapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.palprotech.heylaapp.R;
import com.palprotech.heylaapp.bean.support.Event;
import com.palprotech.heylaapp.helper.AlertDialogHelper;
import com.palprotech.heylaapp.helper.ProgressDialogHelper;
import com.palprotech.heylaapp.interfaces.DialogClickListener;
import com.palprotech.heylaapp.servicehelpers.ServiceHelper;
import com.palprotech.heylaapp.serviceinterfaces.IServiceListener;
import com.palprotech.heylaapp.utils.HeylaAppConstants;
import com.palprotech.heylaapp.utils.PreferenceStorage;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Narendar on 03/11/17.
 */

public class EventDetailActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, OnMapReadyCallback, IServiceListener, DialogClickListener {

    private static final String TAG = EventDetailActivity.class.getName();
    private ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;
    private Event event;
    private ImageView imBack;
    private ImageView imEventBanner;
    private ImageView imEventShare;
    private TextView imEventQuestionAnswer;
    private TextView imEventsView;
    private ImageView imEventFavourite;
    MapView mMapView = null;

    private ImageView imEventOrganiserRequest;
    private TextView txtEventReview;
    private TextView txtCheckInEvent;
    private TextView txtBookEvent;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    String lat;
    String provider;
    protected Double latitude, longitude, latitude1,longitude1;
    protected boolean gps_enabled, network_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        findViewById(R.id.detail_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        event = (Event) getIntent().getSerializableExtra("eventObj");
        setUpUI();
    }

    @Override
    public void onClick(View v) {
        if (v == imBack) {
            finish();
        }
        if (v == imEventBanner) {
//            Toast.makeText(getApplicationContext(), "Hi", Toast.LENGTH_SHORT).show();
        }
        if (v == imEventShare) {
            SpannableString content = new SpannableString("http://www.heylaapp.com/");
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            String text = event.getEventName() + "\n" + event.getDescription() + "\n" + content;


            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
            i.putExtra(android.content.Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(i, "Share via"));

            sendShareStatus();
        }
        if (v == imEventQuestionAnswer) {
//            Toast.makeText(getApplicationContext(), "Hi", Toast.LENGTH_SHORT).show();
        }
        if (v == imEventFavourite) {
//            Toast.makeText(getApplicationContext(), "Hi", Toast.LENGTH_SHORT).show();
            addToFavourite();
        }
        if (v == imEventOrganiserRequest) {
//            Toast.makeText(getApplicationContext(), "Hi", Toast.LENGTH_SHORT).show();
        }
        if (v == txtEventReview) {
            Intent intent = new Intent(getApplicationContext(), EventReviewActivity.class);
            intent.putExtra("eventObj", event);
            startActivity(intent);
//            finish();
        }
        if (v == txtCheckInEvent) {
            checkdistance();
        }
        if (v == txtBookEvent) {
            Intent intent = new Intent(getApplicationContext(), BookingActivity.class);
            intent.putExtra("eventObj", event);
            startActivity(intent);
            finish();
        }
    }

    //    Setup UI page
    void setUpUI() {
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
//        Back button
        imBack = findViewById(R.id.back_res);
//        Event banner
        imEventBanner = findViewById(R.id.event_detail_img);
        imEventBanner.setOnClickListener(this);
        String url = event.getEventBanner();
        if (((url != null) && !(url.isEmpty()))) {
            Picasso.with(this).load(url).placeholder(R.drawable.event_img).error(R.drawable.event_img).into(imEventBanner);
        }
        imEventBanner.setMaxWidth(500);
//        Event title
        TextView txtEventName = findViewById(R.id.event_detail_name);
        txtEventName.setText(event.getEventName());
//        Share the event
        imEventShare = findViewById(R.id.share_event);
        imEventShare.setOnClickListener(this);
//        Chat live with event organiser
        imEventQuestionAnswer = findViewById(R.id.event_qa);
        imEventQuestionAnswer.setOnClickListener(this);
//        Event popularity views
        imEventsView = findViewById(R.id.event_views);
        imEventsView.setText("" + event.getPopularity());
//        Mark as favourite event
        imEventFavourite = findViewById(R.id.addfav);
        imEventFavourite.setOnClickListener(this);
//        Event address
        TextView txtEventAddress = findViewById(R.id.addresstxt);
        txtEventAddress.setText(event.getEventAddress());
//        Event start time
        TextView txtEventStartTime = findViewById(R.id.start_time_txt);
        txtEventStartTime.setText(event.getStartTime());
//        Event end time
        TextView txtEventEndTime = findViewById(R.id.end_time_txt);
        txtEventEndTime.setText(event.getEndTime());
//        Event start date
        TextView txtEventStartDate = findViewById(R.id.start_date_txt);
        txtEventStartDate.setText(event.getStartDate());
//        Event end date
        TextView txtEventEndDate = findViewById(R.id.end_date_txt);
        txtEventEndDate.setText(event.getEndDate());
//        Event details
        TextView txtEventDetails = findViewById(R.id.eventdetailtxt);
        txtEventDetails.setText(event.getDescription());
//        Event venue mapview
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        Event organiser follow
        imEventOrganiserRequest = findViewById(R.id.followrequest);
        imEventOrganiserRequest.setOnClickListener(this);
//        Event organiser name
        TextView txtOrganiserName = findViewById(R.id.organiser_name);
        txtOrganiserName.setText(event.getContactPerson());
//        Event organiser mobile number - primary and secondary
        TextView txtOrganiserMobileNumber = findViewById(R.id.organisermobiletxt);
        String eventOrganiserContactNumber = event.getPrimaryContactNo() + ", " + event.getSecondaryContactNo();
        txtOrganiserMobileNumber.setText(eventOrganiserContactNumber);
//        Event organiser contact emailId
        TextView txtOrganiserEmailId = findViewById(R.id.organisermailtxt);
        txtOrganiserEmailId.setText(event.getContactMail());
//        Event review
        txtEventReview = findViewById(R.id.event_review);
        txtEventReview.setOnClickListener(this);
//        Event check in
        txtCheckInEvent = findViewById(R.id.checkin);
        txtCheckInEvent.setOnClickListener(this);
//        Event booking
        txtBookEvent = findViewById(R.id.book_tickets);
        txtBookEvent.setOnClickListener(this);
        String isBooking = event.getEventBookingStatus();
        if (isBooking.equalsIgnoreCase("N")) {
            txtBookEvent.setVisibility(View.GONE);
        }
        updateEventViews();
    }

    private void updateEventViews() {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(HeylaAppConstants.KEY_EVENT_ID, event.getId());
            jsonObject.put(HeylaAppConstants.KEY_USER_ID, PreferenceStorage.getUserId(getApplicationContext()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = HeylaAppConstants.BASE_URL + HeylaAppConstants.EVENT_POPULARITY;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void sendShareStatus() {

        //A user can only get points 3 times a day for event detail sharing. So restrict beyond that
        long currentTime = System.currentTimeMillis();
        long lastsharedTime = PreferenceStorage.getEventSharedTime(this);
        int sharedCount = PreferenceStorage.getEventSharedcount(this);

        if ((currentTime - lastsharedTime) > HeylaAppConstants.TWENTY4HOURS) {
            Log.d(TAG, "event time elapsed more than 24hrs");
            PreferenceStorage.saveEventSharedtime(this, currentTime);
            PreferenceStorage.saveEventSharedcount(this, 1);

            shareStatus();

        } else {
            if (sharedCount < 5) {
                Log.d(TAG, "event shared count is" + sharedCount);
                sharedCount++;
                PreferenceStorage.saveEventSharedcount(this, sharedCount);
                shareStatus();
            }
        }
    }

    private void shareStatus() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        String date_activity = (dateFormat.format(date)).toString();
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(HeylaAppConstants.KEY_USER_ID, PreferenceStorage.getUserId(this));
            jsonObject.put(HeylaAppConstants.KEY_RULE_ID, "2");
            jsonObject.put(HeylaAppConstants.KEY_EVENT_ID, event.getId());
            jsonObject.put(HeylaAppConstants.PARAMS_DATE, date_activity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = HeylaAppConstants.BASE_URL + HeylaAppConstants.USER_ACTIVITY;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void checkdistance() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        longitude1 = Double.parseDouble(event.getEventLongitude());
        latitude1 = Double.parseDouble(event.getEventLatitude());
        if (distance(latitude,longitude,latitude1,longitude1)<0.1){
            Toast.makeText(getApplicationContext(), "You have successfully checked-in for the event - " + event.getEventName().toString() + "\nGet ready for the fun! ", Toast.LENGTH_LONG).show();
            sendCheckinStatus();
        } else {
            Toast.makeText(getApplicationContext(), "Try again at - " + event.getEventName().toString() + "\nOnce you reached! ", Toast.LENGTH_LONG).show();
        }
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    private void sendCheckinStatus(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        String date_activity = (dateFormat.format(date)).toString();
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(HeylaAppConstants.KEY_USER_ID, PreferenceStorage.getUserId(this));
            jsonObject.put(HeylaAppConstants.KEY_RULE_ID, "3");
            jsonObject.put(HeylaAppConstants.KEY_EVENT_ID, event.getId());
            jsonObject.put(HeylaAppConstants.PARAMS_DATE, date_activity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = HeylaAppConstants.BASE_URL + HeylaAppConstants.USER_ACTIVITY;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void addToFavourite(){
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(HeylaAppConstants.KEY_USER_ID, PreferenceStorage.getUserId(this));
            jsonObject.put(HeylaAppConstants.PARAMS_WISH_LIST_MASTER_ID, "1");
            jsonObject.put(HeylaAppConstants.KEY_EVENT_ID, event.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = HeylaAppConstants.BASE_URL + HeylaAppConstants.WISH_LIST_ADD;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(event.getEventLatitude()), Double.parseDouble(event.getEventLongitude())))
                .title(event.getEventName()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(event.getEventLatitude()), Double.parseDouble(event.getEventLongitude())), 14.0f));
    }

    private boolean validateSignInResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(HeylaAppConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        Log.d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateSignInResponse(response)) {

        }
    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

    }

    public void getlatlong(Location location){
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}