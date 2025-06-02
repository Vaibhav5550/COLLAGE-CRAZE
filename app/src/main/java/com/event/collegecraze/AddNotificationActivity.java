package com.event.collegecraze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddNotificationActivity extends AppCompatActivity {

    Spinner spinner;
    EditText message;
    Button submit;
    ArrayList<String> arrayEventId;
    ArrayList<String> arrayEventName;

    String sEventId;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notification);
        getSupportActionBar().setTitle("Send Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        spinner = findViewById(R.id.add_notification_spinner);
        message = findViewById(R.id.add_notification_message);
        submit = findViewById(R.id.add_notification_submit);

        if (new ConnectionDetector(AddNotificationActivity.this).isConnectingToInternet()) {
            new getEvent().execute();
        } else {
            new ConnectionDetector(AddNotificationActivity.this).connectiondetect();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sEventId = arrayEventId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (message.getText().toString().equalsIgnoreCase("")) {
                    message.setError("Message Required");
                } else {
                    if (new ConnectionDetector(AddNotificationActivity.this).isConnectingToInternet()) {
                        new addNotfication().execute();
                    } else {
                        new ConnectionDetector(AddNotificationActivity.this).connectiondetect();
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class getEvent extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddNotificationActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getEventCollegeSpinner.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    JSONArray array = object.getJSONArray("response");
                    arrayEventId = new ArrayList<>();
                    arrayEventName = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        arrayEventId.add(jsonObject.getString("id"));
                        arrayEventName.add(jsonObject.getString("eventName") + " ( " + jsonObject.getString("collegeName") + " )");
                    }
                    ArrayAdapter collegeAdapter = new ArrayAdapter(AddNotificationActivity.this, android.R.layout.simple_list_item_checked, arrayEventName);
                    spinner.setAdapter(collegeAdapter);
                } else {
                    new ToastIntentClass(AddNotificationActivity.this, object.getString("Message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class addNotfication extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddNotificationActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("senderId", sp.getString(ConstantSp.ID, ""));
            hashMap.put("eventId", sEventId);
            hashMap.put("message", message.getText().toString());
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "addNotification.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    new ToastIntentClass(AddNotificationActivity.this, object.getString("Message"));
                    onBackPressed();
                } else {
                    new ToastIntentClass(AddNotificationActivity.this, object.getString("Message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}