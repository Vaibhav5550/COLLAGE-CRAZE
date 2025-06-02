package com.event.collegecraze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdulhakeem.seemoretextview.SeeMoreTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class EventCollegeListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<EventCollegeLists> arrayList;
    EventCollegeAdapter adapter;
    FloatingActionButton floatingActionButton1;

    SharedPreferences sp;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_college_list);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        getSupportActionBar().setTitle(sp.getString(ConstantSp.EVENTNAME,""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchView = findViewById(R.id.event_college_list_searchview);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(String.valueOf(newText))) {
                    adapter.filter("");
                } else {
                    adapter.filter(String.valueOf(newText));
                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.event_college_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(EventCollegeListActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        floatingActionButton1 = findViewById(R.id.event_college_list_fab1);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventCollegeListActivity.this, AddEventCollegeActivity.class);
                startActivity(intent);
            }
        });

        if (sp.getString(ConstantSp.TYPE, "").equalsIgnoreCase("Admin")) {
            floatingActionButton1.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton1.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (new ConnectionDetector(EventCollegeListActivity.this).isConnectingToInternet()) {
            new getData().execute();
        } else {
            new ConnectionDetector(EventCollegeListActivity.this).connectiondetect();
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
            pd = new ProgressDialog(EventCollegeListActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("eventId", sp.getString(ConstantSp.EVENTID, ""));
            return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getEventCollege.php", MakeServiceCall.POST, hashMap);
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
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        EventCollegeLists lists = new EventCollegeLists();
                        lists.setId(jsonObject.getString("id"));
                        lists.setName(jsonObject.getString("name"));
                        lists.setCollegeName(jsonObject.getString("collegeName"));
                        lists.setEventName(jsonObject.getString("eventName"));
                        lists.setPrice(jsonObject.getString("price"));
                        lists.setMaxAllowed(jsonObject.getString("max_allowed"));
                        lists.setEventDate(jsonObject.getString("event_date"));
                        lists.setEventTime(jsonObject.getString("event_time"));
                        lists.setDescription(jsonObject.getString("description"));
                        lists.setDate(jsonObject.getString("created_date"));
                        arrayList.add(lists);
                    }
                    adapter = new EventCollegeAdapter(EventCollegeListActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(EventCollegeListActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class EventCollegeAdapter extends RecyclerView.Adapter<EventCollegeAdapter.MyHolder> {

        Context context;
        ArrayList<EventCollegeLists> arrayList;
        int iPosition;
        String sId;
        private ArrayList<EventCollegeLists> searchList;

        public EventCollegeAdapter(EventCollegeListActivity eventCollegeListActivity, ArrayList<EventCollegeLists> arrayList) {
            this.context = eventCollegeListActivity;
            this.arrayList = arrayList;
            this.searchList = new ArrayList<EventCollegeLists>();
            this.searchList.addAll(arrayList);
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            arrayList.clear();
            if (charText.length() == 0) {
                arrayList.addAll(searchList);
            } else {
                for (EventCollegeLists s : searchList) {
                    if (s.getCollegeName().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getPrice().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getDate().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getDescription().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getEventDate().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getEventTime().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getEventName().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getMaxAllowed().toLowerCase(Locale.getDefault()).contains(charText)) {
                        arrayList.add(s);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public EventCollegeAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_event_college, parent, false);
            return new EventCollegeAdapter.MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventCollegeAdapter.MyHolder holder, int position) {
            holder.collegeName.setText(arrayList.get(position).getCollegeName());
            holder.eventName.setText(arrayList.get(position).getName() + " ( " + context.getResources().getString(R.string.Rupees) + arrayList.get(position).getPrice() + " )");
            holder.maxAllowed.setText(arrayList.get(position).getMaxAllowed());
            holder.eventDate.setText(arrayList.get(position).getEventDate());
            holder.eventTime.setText(arrayList.get(position).getEventTime());

            holder.description.setContent(arrayList.get(position).getDescription());
            holder.description.setTextMaxLength(100);
            holder.description.toggle();
            holder.description.expandText(false);
            holder.description.setSeeMoreTextColor(android.R.color.holo_red_dark);
            holder.description.setSeeMoreText("ShowMore", "ShowLess");

            if (sp.getString(ConstantSp.TYPE, "").equalsIgnoreCase("Admin")) {
                holder.register.setVisibility(View.GONE);
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.register.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.GONE);
            }

            holder.register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //new AddNameDialog(context, arrayList.get(position).getMaxAllowed(),arrayList.get(position).getId(),arrayList.get(position).getPrice());
                    sp.edit().putString(ConstantSp.EVENTCOLLEGEID, arrayList.get(position).getId()).commit();
                    sp.edit().putString(ConstantSp.EVENTCOLLEGEPRICE, arrayList.get(position).getPrice()).commit();
                    sp.edit().putString(ConstantSp.EVENTMAXALLOW, arrayList.get(position).getMaxAllowed()).commit();
                    new ToastIntentClass(context, RegisterGameNameActivity.class);
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sId = arrayList.get(position).getId();
                    iPosition = position;
                    if (new ConnectionDetector(context).isConnectingToInternet()) {
                        new deleteData().execute();
                    } else {
                        new ConnectionDetector(context).connectiondetect();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder {

            TextView collegeName, eventName, maxAllowed, eventDate, eventTime, register;
            SeeMoreTextView description;
            ImageView delete;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                collegeName = itemView.findViewById(R.id.custom_event_college_name);
                eventName = itemView.findViewById(R.id.custom_event_college_event_name);
                maxAllowed = itemView.findViewById(R.id.custom_event_college_max_allowed);
                eventDate = itemView.findViewById(R.id.custom_event_college_event_date);
                eventTime = itemView.findViewById(R.id.custom_event_college_event_time);
                description = itemView.findViewById(R.id.custom_event_college_description);
                delete = itemView.findViewById(R.id.custom_event_college_event_delete);
                register = itemView.findViewById(R.id.custom_event_college_register);
            }
        }

        private class deleteData extends AsyncTask<String, String, String> {

            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(context);
                pd.setMessage("Please Wait...");
                pd.setCancelable(false);
                pd.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", sId);
                return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "deleteEventCollege.php", MakeServiceCall.POST, hashMap);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                pd.dismiss();
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.getString("Status").equalsIgnoreCase("True")) {
                        Toast.makeText(context, object.getString("Message"), Toast.LENGTH_SHORT).show();
                        arrayList.remove(iPosition);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, object.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}