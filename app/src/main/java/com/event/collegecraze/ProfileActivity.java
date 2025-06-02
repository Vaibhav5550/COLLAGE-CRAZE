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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    EditText name, email, contact;
    RadioGroup gender;
    RadioButton male, female, transgender;
    Button signup;
    Spinner collegeName, stream, semester;
    String sGender, sCollegeName, sCollegeNameId, sStream, sSemester;

    String[] streamArray = {"Diploma", "Degree", "BCA"};
    String[] semesterArray = {"1", "2", "3", "4", "5", "6", "7", "8"};

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ArrayList<String> collegeNameArray;
    ArrayList<String> collegeNameIdArray;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.profile_email);
        contact = findViewById(R.id.profile_cno);

        gender = findViewById(R.id.profile_gender);
        male = findViewById(R.id.profile_male);
        female = findViewById(R.id.profile_female);
        transgender = findViewById(R.id.profile_transgender);

        signup = findViewById(R.id.profile_button);

        collegeName = findViewById(R.id.profile_college);

        stream = findViewById(R.id.profile_stream);
        ArrayAdapter streamAdapter = new ArrayAdapter(ProfileActivity.this, android.R.layout.simple_list_item_checked, streamArray);
        stream.setAdapter(streamAdapter);

        for (int i = 0; i < streamArray.length; i++) {
            if (streamArray[i].equalsIgnoreCase(sp.getString(ConstantSp.STREAM, ""))) {
                stream.setSelection(i);
            }
        }
        sStream = sp.getString(ConstantSp.STREAM, "");

        stream.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sStream = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        semester = findViewById(R.id.profile_semester);
        ArrayAdapter semesterAdapter = new ArrayAdapter(ProfileActivity.this, android.R.layout.simple_list_item_checked, semesterArray);
        semester.setAdapter(semesterAdapter);
        semester.setSelection(Integer.parseInt(sp.getString(ConstantSp.SEMESTER, "")) - 1);
        sSemester = sp.getString(ConstantSp.SEMESTER, "");

        semester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sSemester = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (new ConnectionDetector(ProfileActivity.this).isConnectingToInternet()) {
            new getCollegeData().execute();
        } else {
            new ConnectionDetector(ProfileActivity.this).connectiondetect();
        }

        if (sp.getString(ConstantSp.GENDER, "").equalsIgnoreCase("Male")) {
            male.setChecked(true);
        } else if (sp.getString(ConstantSp.GENDER, "").equalsIgnoreCase("Female")) {
            female.setChecked(true);
        } else if (sp.getString(ConstantSp.GENDER, "").equalsIgnoreCase("Transgender")) {
            transgender.setChecked(true);
        } else {

        }
        sGender = sp.getString(ConstantSp.GENDER, "");
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = gender.getCheckedRadioButtonId();
                RadioButton rb = findViewById(id);
                sGender = rb.getText().toString();
            }
        });

        collegeName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sCollegeName = collegeNameArray.get(i);
                sCollegeNameId = collegeNameIdArray.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().trim().equalsIgnoreCase("")) {
                    name.setError("Name Required");
                } else if (email.getText().toString().trim().equalsIgnoreCase("")) {
                    email.setError("Email Id Required");
                } else if (!email.getText().toString().trim().matches(emailPattern)) {
                    email.setError("Valid Email Id Required");
                } else if (contact.getText().toString().trim().equalsIgnoreCase("")) {
                    contact.setError("Contact No. Required");
                } else if (contact.getText().toString().length() < 10 || contact.getText().toString().length() > 10) {
                    contact.setError("Valid Contact No. Required");
                } else if (gender.getCheckedRadioButtonId() == -1) {
                    new ToastIntentClass(ProfileActivity.this, "Please Select Gender");
                } else {
                    //onBackPressed();
                    if (new ConnectionDetector(ProfileActivity.this).isConnectingToInternet()) {
                        new updateData().execute();
                    } else {
                        new ConnectionDetector(ProfileActivity.this).connectiondetect();
                    }
                }
            }
        });

        name.setText(sp.getString(ConstantSp.NAME, ""));
        email.setText(sp.getString(ConstantSp.EMAIL, ""));
        contact.setText(sp.getString(ConstantSp.CONTACT, ""));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class updateData extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(ProfileActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", sp.getString(ConstantSp.ID, ""));
            hashMap.put("name", name.getText().toString());
            hashMap.put("email", email.getText().toString());
            hashMap.put("contact", contact.getText().toString());
            hashMap.put("gender", sGender);
            hashMap.put("collegeName", sCollegeName);
            hashMap.put("collegeId", sCollegeNameId);
            hashMap.put("stream", sStream);
            hashMap.put("semester", sSemester);
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "updateProfile.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    sp.edit().putString(ConstantSp.NAME, name.getText().toString()).commit();
                    sp.edit().putString(ConstantSp.EMAIL, email.getText().toString()).commit();
                    sp.edit().putString(ConstantSp.CONTACT, contact.getText().toString()).commit();
                    sp.edit().putString(ConstantSp.GENDER, sGender).commit();
                    sp.edit().putString(ConstantSp.COLLEGENAME, sCollegeName).commit();
                    sp.edit().putString(ConstantSp.COLLEGEID, sCollegeNameId).commit();
                    sp.edit().putString(ConstantSp.STREAM, sStream).commit();
                    sp.edit().putString(ConstantSp.SEMESTER, sSemester).commit();
                    new ToastIntentClass(ProfileActivity.this, object.getString("Message"));
                    onBackPressed();
                } else {
                    new ToastIntentClass(ProfileActivity.this, object.getString("Message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class getCollegeData extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(ProfileActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getCollege.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                int iCollegeIndex = 0;
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    JSONArray array = object.getJSONArray("response");
                    collegeNameArray = new ArrayList<>();
                    collegeNameIdArray = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        collegeNameIdArray.add(jsonObject.getString("id"));
                        collegeNameArray.add(jsonObject.getString("name"));
                        if (sp.getString(ConstantSp.COLLEGENAME, "").equalsIgnoreCase(jsonObject.getString("name"))) {
                            iCollegeIndex = i;
                        }
                    }
                    ArrayAdapter collegeAdapter = new ArrayAdapter(ProfileActivity.this, android.R.layout.simple_list_item_checked, collegeNameArray);
                    collegeName.setAdapter(collegeAdapter);
                    collegeName.setSelection(iCollegeIndex);
                    sCollegeName = sp.getString(ConstantSp.COLLEGENAME, "");
                    sCollegeNameId = sp.getString(ConstantSp.COLLEGEID, "");
                } else {
                    new ToastIntentClass(ProfileActivity.this, object.getString("Message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}