package com.event.collegecraze;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddCollegeActivity extends AppCompatActivity {

    Button add;
    EditText college;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_college);
        getSupportActionBar().setTitle("Add College");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        add = findViewById(R.id.add_college_add);
        college = findViewById(R.id.add_college_title);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (college.getText().toString().trim().equalsIgnoreCase("")) {
                    college.setError("College Name Required");
                } else {
                    if (new ConnectionDetector(AddCollegeActivity.this).isConnectingToInternet()) {
                        new insertCollegedata().execute();
                    } else {
                        new ConnectionDetector(AddCollegeActivity.this).connectiondetect();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class insertCollegedata extends AsyncTask<String, String, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddCollegeActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", college.getText().toString());
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "addCollege.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equals("True")) {
                    Toast.makeText(AddCollegeActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(AddCollegeActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}