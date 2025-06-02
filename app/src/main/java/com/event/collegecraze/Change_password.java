package com.event.collegecraze;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Change_password extends AppCompatActivity {
    ImageButton back;

    EditText e1, e2, e3;
    Button b1;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);

        e1 = findViewById(R.id.cp_current);

        e2 = findViewById(R.id.cp_newpass);
        e3 = findViewById(R.id.cp_reeneterpass);
        b1 = findViewById(R.id.cp_button);

        back = findViewById(R.id.cp_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (e1.getText().toString().trim().equalsIgnoreCase("")) {
                    e1.setError("Old Password Required");
                } else if (e2.getText().toString().trim().equalsIgnoreCase("")) {
                    e2.setError("New Password Required");
                } else if (e3.getText().toString().trim().equalsIgnoreCase("")) {
                    e3.setError("Confirm Password Required");
                } else if (!e2.getText().toString().matches(e3.getText().toString())) {
                    e3.setError("Password Not Match");
                } else {
                    if (new ConnectionDetector(Change_password.this).isConnectingToInternet()) {
                        new changePass().execute();
                    } else {
                        new ConnectionDetector(Change_password.this).connectiondetect();
                    }
                }
            }
        });
        e1.setText(sp.getString(ConstantSp.PASSWORD, ""));

    }

    private class changePass extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Change_password.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", sp.getString(ConstantSp.ID, ""));
            hashMap.put("password", e1.getText().toString());
            hashMap.put("newPassword", e2.getText().toString());
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "changePassword.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("Status").equals("True")) {
                    Toast.makeText(Change_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                    e1.setText(e2.getText().toString());
                    sp.edit().putString(ConstantSp.PASSWORD, e2.getText().toString()).commit();
                    e2.setText("");
                    e3.setText("");
                } else {
                    Toast.makeText(Change_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
