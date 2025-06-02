package com.event.collegecraze;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class Forgot_password extends AppCompatActivity {
    ImageButton back;

    EditText e1, e2, e3, email;
    Button b1, next;
    LinearLayout firstLayout, secondLayout;
    SharedPreferences sp;
    String email_pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    final int min = 1111;
    final int max = 9999;
    final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);

        email = findViewById(R.id.forgot_cp_current);
        firstLayout = findViewById(R.id.forgot_password_first);
        secondLayout = findViewById(R.id.forgot_password_second);
        next = findViewById(R.id.forgot_cp_button_next);

        e1 = findViewById(R.id.forgot_cp_current_email);
        e2 = findViewById(R.id.forgot_cp_newpass);
        e3 = findViewById(R.id.forgot_cp_reeneterpass);
        b1 = findViewById(R.id.forgot_cp_button);

        back = findViewById(R.id.forgot_cp_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*int rNumber = random.nextInt((max - min) + 1) + min;

        //mail send
        new JavaAPI(ForgotPasswordActivity.this, email,
                "OTP Code For BookBox App", "Your OTP code is:" + "\t" +
                String.valueOf(rNumber)).execute();


        sp.edit().putString(ConstantSp.OTP, String.valueOf(rNumber)).commit();
        final Dialog otpverifyDialog = new Dialog(ForgotPasswordActivity.this);
        otpverifyDialog.setContentView(R.layout.getotp_custom_dialog);

        //init editbox and textbox
        final EditText otpNumberED = (EditText) otpverifyDialog.findViewById(R.id.otp_code_getActivity);
        Button submitTV = (Button) otpverifyDialog.findViewById(R.id.verify_btn_getActivity);
        TextView resendOTP = (TextView) otpverifyDialog.findViewById(R.id.resend_getotpcode);
        TextView cancelTv = (TextView) otpverifyDialog.findViewById(R.id.close_alert_dialogbox_getActivity);
        otpverifyDialog.show();

        resendOTP.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int rNumber = random.nextInt((max - min) + 1) + min;
                //mail send
                new JavaAPI(ForgotPasswordActivity.this, sp.getString(ConstantSp.FORGOT_EMAIL_ID, ""), "OTP Code For BookBox App", "Your OTP code is:" + "\t" + String.valueOf(rNumber)).execute();
                sp.edit().putString(ConstantSp.OTP, String.valueOf(rNumber)).commit();

                Toast.makeText(ForgotPasswordActivity.this, "OTP resend", Toast.LENGTH_SHORT).show();
            }
        });
        submitTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                otpNumber = otpNumberED.getText().toString().trim();
                if (otpNumber.isEmpty())
                {
                    otpNumberED.setError("Enter your otp");
                }
                if (otpNumber.equals(sp.getString(ConstantSp.OTP, "")))
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Matched", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class));
                    finish();
                }
                else
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Not Matched", Toast.LENGTH_SHORT).show();
                }

            }
        });
        cancelTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                otpverifyDialog.dismiss();
            }
        });*/

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().equalsIgnoreCase("")) {
                    email.setError("Please enter email-id");
                } else if (!email.getText().toString().matches(email_pattern)) {
                    email.setError("Valid Email Id Required");
                    //Toast.makeText(SignupActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    if (new ConnectionDetector(Forgot_password.this).isConnectingToInternet()) {
                        new checkMail().execute();
                    } else {
                        new ConnectionDetector(Forgot_password.this).connectiondetect();
                    }
                }

            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (e1.getText().toString().equalsIgnoreCase("")) {
                    e1.setError("Please enter email-id");
                } else if (!e1.getText().toString().matches(email_pattern)) {
                    e1.setError("Valid Email Id Required");
                    //Toast.makeText(SignupActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (e2.getText().toString().trim().equalsIgnoreCase("")) {
                    e2.setError("New Password Required");
                } else if (e3.getText().toString().trim().equalsIgnoreCase("")) {
                    e3.setError("Confirm Password Required");
                } else if (!e2.getText().toString().matches(e3.getText().toString())) {
                    e3.setError("Password Not Match");
                } else {
                    if (new ConnectionDetector(Forgot_password.this).isConnectingToInternet()) {
                        new forgotPass().execute();
                    } else {
                        new ConnectionDetector(Forgot_password.this).connectiondetect();
                    }
                }
            }
        });

    }

    private class forgotPass extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Forgot_password.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("email", e1.getText().toString());
            hashMap.put("password", e2.getText().toString());
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "forgotPassword.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("Status").equals("True")) {
                    Toast.makeText(Forgot_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(Forgot_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class checkMail extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Forgot_password.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("email", email.getText().toString());
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "checkMail.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("Status").equals("True")) {
                   /* int rNumber = random.nextInt((max - min) + 1) + min;
                    //mail send
                    new JavaAPI(Forgot_password.this, email.getText().toString(),
                            "OTP Code For Festival-Hub App", "Your OTP code is:" + "\t" +
                            String.valueOf(rNumber)).execute();

                    sp.edit().putString(ConstantSp.OTP, String.valueOf(rNumber)).commit();
                    final Dialog otpverifyDialog = new Dialog(Forgot_password.this);
                    otpverifyDialog.setContentView(R.layout.getotp_custom_dialog);

                    //init editbox and textbox
                    final EditText otpNumberED = (EditText) otpverifyDialog.findViewById(R.id.otp_code_getActivity);
                    Button submitTV = (Button) otpverifyDialog.findViewById(R.id.verify_btn_getActivity);
                    TextView resendOTP = (TextView) otpverifyDialog.findViewById(R.id.resend_getotpcode);
                    TextView cancelTv = (TextView) otpverifyDialog.findViewById(R.id.close_alert_dialogbox_getActivity);
                    otpverifyDialog.show();

                    resendOTP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int rNumber = random.nextInt((max - min) + 1) + min;
                            //mail send
                            new JavaAPI(Forgot_password.this, email.getText().toString(), "OTP Code For Festival-Hub App", "Your OTP code is:" + "\t" + String.valueOf(rNumber)).execute();
                            sp.edit().putString(ConstantSp.OTP, String.valueOf(rNumber)).commit();
                            Toast.makeText(Forgot_password.this, "OTP resend", Toast.LENGTH_SHORT).show();
                        }
                    });
                    submitTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String otpNumber = otpNumberED.getText().toString().trim();
                            if (otpNumber.isEmpty()) {
                                otpNumberED.setError("Enter your otp");
                            }
                            if (otpNumber.equals(sp.getString(ConstantSp.OTP, ""))) {
                                //Toast.makeText(SignupActivity.this, "Matched", Toast.LENGTH_SHORT).show();
                                otpverifyDialog.dismiss();
                                firstLayout.setVisibility(View.GONE);
                                secondLayout.setVisibility(View.VISIBLE);
                                e1.setText(email.getText().toString());
                            } else {
                                Toast.makeText(Forgot_password.this, "Not Matched", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    cancelTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            otpverifyDialog.dismiss();
                        }
                    });*/
                    //Toast.makeText(Forgot_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                    //onBackPressed();


                    firstLayout.setVisibility(View.GONE);
                    secondLayout.setVisibility(View.VISIBLE);
                    e1.setText(email.getText().toString());
                } else {
                    Toast.makeText(Forgot_password.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
