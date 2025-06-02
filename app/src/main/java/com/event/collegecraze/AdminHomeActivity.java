package com.event.collegecraze;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.event.collegecraze.notifications.Config;
import com.event.collegecraze.notifications.NotificationUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class AdminHomeActivity extends AppCompatActivity {

    ListView admin_Listview;
    String[] activities = {"Manage Students", "Manage Volunteer", "Manage Colleges", "Manage Events", "Manage Registered Student","Notification","Reports", "Log Out"};

    SharedPreferences sp;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        getSupportActionBar().setTitle("Admin Panel");
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        admin_Listview = findViewById(R.id.admin_list);

        AdminAdapter adminAdapter = new AdminAdapter(this, activities);
        admin_Listview.setAdapter((ListAdapter) adminAdapter);

        admin_Listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                sp.edit().putString(ConstantSp.ADMIN_CLICK_POSITION, String.valueOf(position)).commit();
                switch (position) {
                    case 0:
                        Intent intent = new Intent(AdminHomeActivity.this, AdminUserListActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        startActivity(new Intent(AdminHomeActivity.this, AdminUserListActivity.class));
                        break;
                    case 2:
                        Intent intent1 = new Intent(AdminHomeActivity.this, CollegeListActivity.class);
                        startActivity(intent1);
                        break;
                    case 3:
                        Intent intent2 = new Intent(AdminHomeActivity.this, EventListActivity.class);
                        startActivity(intent2);
                        break;
                    case 4:
                        startActivity(new Intent(AdminHomeActivity.this, EventHistoryActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(AdminHomeActivity.this, NotificationActivity.class));
                        break;
                    case 6:
                        startActivity(new Intent(AdminHomeActivity.this, ReportsActivity.class));
                        break;
                    case 7:
                        sp.edit().remove(ConstantSp.ID).commit();
                        sp.edit().remove(ConstantSp.TYPE).commit();
                        sp.edit().remove(ConstantSp.NAME).commit();
                        sp.edit().remove(ConstantSp.EMAIL).commit();
                        sp.edit().remove(ConstantSp.CONTACT).commit();
                        sp.edit().remove(ConstantSp.PASSWORD).commit();
                        sp.edit().remove(ConstantSp.GENDER).commit();
                        sp.edit().remove(ConstantSp.COLLEGEID).commit();
                        sp.edit().remove(ConstantSp.COLLEGENAME).commit();
                        sp.edit().remove(ConstantSp.STREAM).commit();
                        sp.edit().remove(ConstantSp.SEMESTER).commit();
                        sp.edit().remove(ConstantSp.VOLUNTEER).commit();
                        startActivity(new Intent(AdminHomeActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        break;
                }
            }
            // ListView Clicked item value
            //String  itemValue    = (String) profile_Listview.getItemAtPosition(position);


            //Toast.makeText(getActivity(), "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG).show(); }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    //Update FCM ID CODE
                    updateFCM();
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                }
            }
        };
        if (new ConnectionDetector(AdminHomeActivity.this).isConnectingToInternet()) {
            updateFCM();
        } else {
            new ConnectionDetector(AdminHomeActivity.this).connectiondetect();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void updateFCM() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String newToken = instanceIdResult.getToken();
            Log.e("newToken", newToken);
            sp.edit().putString(ConstantSp.FCM_ID, newToken).commit();
        });
        if (new ConnectionDetector(AdminHomeActivity.this).isConnectingToInternet()) {
            new updateFcmData().execute();
        }
    }

    private class updateFcmData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", sp.getString(ConstantSp.ID, ""));
            hashMap.put("fcm", sp.getString(ConstantSp.FCM_ID, ""));
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "updateFcm.php", MakeServiceCall.POST, hashMap);
        }
    }
}