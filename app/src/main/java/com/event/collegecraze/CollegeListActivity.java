package com.event.collegecraze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CollegeListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<CollegeLists> arrayList;
    CollegeAdapter adapter;
    FloatingActionButton floatingActionButton1;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_list);
        getSupportActionBar().setTitle("College Lists");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        recyclerView = findViewById(R.id.college_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(CollegeListActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        floatingActionButton1 = findViewById(R.id.college_list_fab1);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CollegeListActivity.this, AddCollegeActivity.class);
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
        if (new ConnectionDetector(CollegeListActivity.this).isConnectingToInternet()) {
            new getData().execute();
        } else {
            new ConnectionDetector(CollegeListActivity.this).connectiondetect();
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
            pd = new ProgressDialog(CollegeListActivity.this);
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
                JSONObject object = new JSONObject(s);
                if (object.getString("Status").equalsIgnoreCase("True")) {
                    arrayList = new ArrayList<>();
                    JSONArray array = object.getJSONArray("response");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        CollegeLists lists = new CollegeLists();
                        lists.setId(jsonObject.getString("id"));
                        lists.setName(jsonObject.getString("name"));
                        lists.setDate(jsonObject.getString("created_date"));
                        arrayList.add(lists);
                    }
                    adapter = new CollegeAdapter(CollegeListActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(CollegeListActivity.this, object.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class CollegeAdapter extends RecyclerView.Adapter<CollegeAdapter.MyHolder> {

        Context context;
        ArrayList<CollegeLists> arrayList;
        int iPosition;
        String sId;

        public CollegeAdapter(CollegeListActivity collegeListActivity, ArrayList<CollegeLists> arrayList) {
            this.context = collegeListActivity;
            this.arrayList = arrayList;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_college, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.name.setText(arrayList.get(position).getName());

            holder.map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri gmmIntentUriHospital = Uri.parse("geo:0,0?q="+arrayList.get(position).getName());
                    Intent mapIntentHospital = new Intent(Intent.ACTION_VIEW, gmmIntentUriHospital);
                    mapIntentHospital.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntentHospital);
                }
            });

            if (sp.getString(ConstantSp.TYPE, "").equalsIgnoreCase("Admin")) {
                holder.map.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.map.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.GONE);
            }

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

            TextView name;
            ImageView delete, map;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.custom_college_name);
                delete = itemView.findViewById(R.id.custom_college_delete);
                map = itemView.findViewById(R.id.custom_college_map);
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
                return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "deleteCollege.php", MakeServiceCall.POST, hashMap);
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