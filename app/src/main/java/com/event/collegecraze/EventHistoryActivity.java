package com.event.collegecraze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EventHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<EventHistoryLists> arrayList;
    EventHistoryAdapter adapter;

    TextView totalStudent, totalAmount;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);
        getSupportActionBar().setTitle("Event Registered History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        recyclerView = findViewById(R.id.event_history_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(EventHistoryActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        totalStudent = findViewById(R.id.event_history_total_student);
        totalAmount = findViewById(R.id.event_history_total_amount);

        if (sp.getString(ConstantSp.TYPE, "").equalsIgnoreCase("Admin")) {
            totalStudent.setVisibility(View.VISIBLE);
            totalAmount.setVisibility(View.VISIBLE);
        } else {
            if (sp.getString(ConstantSp.VOLUNTEER, "").equalsIgnoreCase("Yes")) {
                totalStudent.setVisibility(View.VISIBLE);
                totalAmount.setVisibility(View.VISIBLE);
            } else {
                totalStudent.setVisibility(View.GONE);
                totalAmount.setVisibility(View.GONE);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (new ConnectionDetector(EventHistoryActivity.this).isConnectingToInternet()) {
            new getData().execute();
        } else {
            new ConnectionDetector(EventHistoryActivity.this).connectiondetect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class getData extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(EventHistoryActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userId", sp.getString(ConstantSp.ID, ""));
            hashMap.put("type", sp.getString(ConstantSp.TYPE, ""));
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getHistory.php", MakeServiceCall.POST, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    arrayList = new ArrayList<>();
                    JSONArray array = object.getJSONArray("response");
                    totalStudent.setText("Total Student : " + array.length());
                    totalAmount.setText("Total Amount : " + getResources().getString(R.string.Rupees) + object.getInt("total"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        EventHistoryLists lists = new EventHistoryLists();
                        lists.setId(jsonObject.getString("id"));
                        lists.setEcId(jsonObject.getString("ec_id"));
                        lists.setUserId(jsonObject.getString("userId"));
                        lists.setName(jsonObject.getString("name"));
                        lists.setPrice(jsonObject.getString("price"));
                        lists.setTransactionId(jsonObject.getString("transactionId"));
                        lists.setAttendance(jsonObject.getString("attendance"));
                        lists.setCreated_date(jsonObject.getString("created_date"));
                        lists.setCollegeName(jsonObject.getString("collegeName"));
                        lists.setEventName(jsonObject.getString("eventName"));
                        lists.setEventDate(jsonObject.getString("event_date"));
                        lists.setEventTime(jsonObject.getString("event_time"));
                        arrayList.add(lists);
                    }
                    adapter = new EventHistoryAdapter(EventHistoryActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(EventHistoryActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.MyHolder> {

        Context context;
        ArrayList<EventHistoryLists> arrayList;
        int iPosition;
        String sId;

        public EventHistoryAdapter(EventHistoryActivity eventCollegeListActivity, ArrayList<EventHistoryLists> arrayList) {
            this.context = eventCollegeListActivity;
            this.arrayList = arrayList;
        }

        @NonNull
        @Override
        public EventHistoryAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_event_history, parent, false);
            return new EventHistoryAdapter.MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventHistoryAdapter.MyHolder holder, int position) {
            holder.collegeName.setText(arrayList.get(position).getCollegeName());
            holder.eventName.setText(arrayList.get(position).getEventName() + " ( " + context.getResources().getString(R.string.Rupees) + arrayList.get(position).getPrice() + " )");
            holder.eventDate.setText(arrayList.get(position).getEventDate());
            holder.eventTime.setText(arrayList.get(position).getEventTime());
            holder.registrationDate.setText(arrayList.get(position).getCreated_date());
            holder.name.setText(arrayList.get(position).getName());

            if (arrayList.get(position).getAttendance().equalsIgnoreCase("0")) {
                holder.avaibility.setText("Pending");
            } else if (arrayList.get(position).getAttendance().equalsIgnoreCase("A")) {
                holder.avaibility.setText("Absent");
            } else if (arrayList.get(position).getAttendance().equalsIgnoreCase("P")) {
                holder.avaibility.setText("Present");
            } else {
                holder.avaibility.setText("Pending");
            }

            if (sp.getString(ConstantSp.TYPE, "").equalsIgnoreCase("Admin")) {
                holder.showQrCode.setVisibility(View.GONE);
            } else {
                if (sp.getString(ConstantSp.VOLUNTEER, "").equalsIgnoreCase("Yes")) {
                    holder.showQrCode.setVisibility(View.GONE);
                } else {
                    if (arrayList.get(position).getAttendance().equalsIgnoreCase("0")) {
                        holder.showQrCode.setVisibility(View.VISIBLE);
                    } else {
                        holder.showQrCode.setVisibility(View.GONE);
                    }
                }
            }

            //holder.showQrCode.setVisibility(View.VISIBLE);

            holder.showQrCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ViewQrCodeDialog(context, arrayList.get(position).getId());
                }
            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder {

            TextView collegeName, eventName, eventDate, eventTime, registrationDate, showQrCode, avaibility, name;
            LinearLayout avaibilityLayout;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                collegeName = itemView.findViewById(R.id.custom_event_history_name);
                eventName = itemView.findViewById(R.id.custom_event_history_event_name);
                eventDate = itemView.findViewById(R.id.custom_event_history_event_date);
                eventTime = itemView.findViewById(R.id.custom_event_history_event_time);
                registrationDate = itemView.findViewById(R.id.custom_event_history_registration_date);
                showQrCode = itemView.findViewById(R.id.custom_event_history_qr_code);
                avaibility = itemView.findViewById(R.id.custom_event_history_avaibility);
                avaibilityLayout = itemView.findViewById(R.id.custom_event_history_avaibility_layout);
                name = itemView.findViewById(R.id.custom_event_history_student_name);
            }
        }
    }
}