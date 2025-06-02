package com.event.collegecraze;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddPaymentActivity extends AppCompatActivity implements PaymentResultListener {

    SharedPreferences sp;
    String sTransactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);
        getSupportActionBar().hide();
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        Checkout.preload(getApplicationContext());
        launchPaymentFlow();
    }

    /*private void launchPaymentFlow() {
        PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();
        payUmoneyConfig.setPayUmoneyActivityTitle(sp.getString(ConstantSp.EVENTNAME, ""));
        payUmoneyConfig.setDoneButtonText("Pay " + getResources().getString(R.string.Rupees) + sp.getString(ConstantSp.EVENTPRICES, ""));
        //setTxnId(System.currentTimeMillis() + "")
        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount(String.valueOf(convertStringToDouble(sp.getString(ConstantSp.EVENTPRICES, ""))))
                .setTxnId("1234567890")
                .setPhone(sp.getString(ConstantSp.CONTACT, "")) //Add Dynamic Contact
                .setProductName(sp.getString(ConstantSp.EVENTNAME, ""))
                .setFirstName(sp.getString(ConstantSp.NAME, "")) //Add Dynamic Name
                .setEmail(sp.getString(ConstantSp.EMAIL, "")) //Add Dynamic Email
                .setsUrl(ConstantSp.SURL)
                .setfUrl(ConstantSp.FURL)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(ConstantSp.DEBUG)
                .setKey(ConstantSp.MERCHANT_KEY)
                .setMerchantId(ConstantSp.MERCHANT_ID);
        try {
            PayUmoneySdkInitializer.PaymentParam mPaymentParams = builder.build();
            calculateHashInServer(mPaymentParams);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Double convertStringToDouble(String str) {
        return Double.parseDouble(str);
    }

    private void calculateHashInServer(
            final PayUmoneySdkInitializer.PaymentParam mPaymentParams) {
        ProgressUtils.showLoadingDialog(this);
        String url = ConstantSp.MONEY_HASH;
        StringRequest request = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String merchantHash = "";

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            merchantHash = jsonObject.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ProgressUtils.cancelLoading();

                        if (merchantHash.isEmpty() || merchantHash.equals("")) {
                            //new ToastIntentClass(AddPaymentActivity.this, "Could not generate hash");
                            Toast.makeText(AddPaymentActivity.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                        } else {
                            mPaymentParams.setMerchantHash(merchantHash);
                            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, AddPaymentActivity.this, R.style.PayUMoney, true);
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            //new ToastIntentClass(AddPaymentActivity.this, "Connect to internet Volley");
                            Toast.makeText(AddPaymentActivity.this, "Connect to internet Volley", Toast.LENGTH_SHORT).show();
                        } else {
                            //new ToastIntentClass(AddPaymentActivity.this, error.getMessage());
                            Toast.makeText(AddPaymentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        ProgressUtils.cancelLoading();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return mPaymentParams.getParams();
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {

            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);
            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //showAlert("Payment Successful");
                    Toast.makeText(this, "Payment Successfully", Toast.LENGTH_SHORT).show();
                    sTransactionId = transactionResponse.getTransactionDetails();
                    if (new ConnectionDetector(AddPaymentActivity.this).isConnectingToInternet()) {
                        new registerData().execute();
                    } else {
                        new ConnectionDetector(AddPaymentActivity.this).connectiondetect();
                    }
                    //Log.d("RESPONSE", transactionResponse.getTransactionDetails());
                    //new addShipping().execute();
                } else if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.CANCELLED)) {
                    showAlert("Payment Cancelled");
                } else if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.FAILED)) {
                    showAlert("Payment Failed");
                }

            } else if (resultModel != null && resultModel.getError() != null) {
                Toast.makeText(this, "Error check log", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Both objects are null", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_CANCELED) {
            showAlert("Payment Cancelled");
        }
    }

    private void showAlert(String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }*/

    public void launchPaymentFlow() {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        double payableAmount = Double.parseDouble(sp.getString(ConstantSp.EVENTPRICES, "")) * 100;
        final Activity activity = this;

        final Checkout co = new Checkout();

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Razorpay Corp");
            options.put("description", sp.getString(ConstantSp.EVENTNAME, ""));
            options.put("send_sms_hash", true);
            options.put("allow_rotation", true);
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount", payableAmount);

            JSONObject preFill = new JSONObject();
            preFill.put("email", sp.getString(ConstantSp.EMAIL, ""));
            preFill.put("contact", sp.getString(ConstantSp.CONTACT, ""));

            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            Log.d("RESPONSE", e.getMessage());
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    /**
     * The name of the function has to be
     * onPaymentSuccess
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    @SuppressWarnings("unused")
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            //Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();
            sTransactionId = razorpayPaymentID;
            if (new ConnectionDetector(AddPaymentActivity.this).isConnectingToInternet()) {
                new registerData().execute();
            } else {
                new ConnectionDetector(AddPaymentActivity.this).connectiondetect();
            }
            /*if (new ConnectionDetector(PackageActivity.this).isConnectingToInternet()) {
                pd = new BeautifulProgressDialog(PackageActivity.this,
                        BeautifulProgressDialog.withGIF,
                        null);
                pd.setViewType(BeautifulProgressDialog.withGIF);
                Uri myUri = Uri.fromFile(new File("//android_asset/loading.gif"));
                pd.setGifLocation(myUri);
                pd.setLayoutColor(getResources().getColor(android.R.color.transparent));
                pd.setCancelableOnTouchOutside(false);
                pd.show();
                doBuyPackage();
            } else {
                new ConnectionDetector(PackageActivity.this).connectiondetect();
            }*/
        } catch (Exception e) {
            Log.e("RESPONSE", "Exception in onPaymentSuccess", e);
        }
    }

    /**
     * The name of the function has to be
     * onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    @SuppressWarnings("unused")
    @Override
    public void onPaymentError(int code, String response) {
        try {
            //Toast.makeText(this, "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("RESPONSE", "Exception in onPaymentError", e);
        }
    }

    private class registerData extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddPaymentActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("ec_id", sp.getString(ConstantSp.EVENTCOLLEGEID, ""));
            hashMap.put("max_allow", sp.getString(ConstantSp.EVENTMAXALLOW, ""));
            hashMap.put("userId", sp.getString(ConstantSp.ID, ""));
            hashMap.put("price", sp.getString(ConstantSp.EVENTPRICES, ""));
            hashMap.put("name", sp.getString(ConstantSp.EVENTNAMES, ""));
            hashMap.put("transactionId", sTransactionId);
            //hashMap.put("id",sp.getString(ConstantSp.ADOPTIONId,""));
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "addTransaction.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equals("True")) {
                    Toast.makeText(AddPaymentActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(AddPaymentActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}