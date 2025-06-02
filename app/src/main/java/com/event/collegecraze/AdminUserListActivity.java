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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AdminUserListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<UserList> arrayList;
    UserListAdapter adapter;
    SharedPreferences sp;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        if (sp.getString(ConstantSp.ADMIN_CLICK_POSITION, "").equalsIgnoreCase("1")) {
            getSupportActionBar().setTitle("Manage Volunteer");
        } else {
            getSupportActionBar().setTitle("Manage User");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.admin_manage_user_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdminUserListActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        searchView = findViewById(R.id.admin_manage_user_searchview);

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

        if (new ConnectionDetector(AdminUserListActivity.this).isConnectingToInternet()) {
            new getUserData().execute();
        } else {
            new ConnectionDetector(AdminUserListActivity.this).connectiondetect();
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

    private class getUserData extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AdminUserListActivity.this);
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (sp.getString(ConstantSp.ADMIN_CLICK_POSITION, "").equalsIgnoreCase("1")) {
                HashMap<String, String> hashMap = new HashMap<>();
                return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getVolunteer.php", MakeServiceCall.POST, hashMap);
            } else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("type", "User");
                return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "getUser.php", MakeServiceCall.POST, hashMap);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("Status").equalsIgnoreCase("True")) {
                    JSONArray array = jsonObject.getJSONArray("response");
                    arrayList = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        UserList list = new UserList();
                        list.setId(object.getString("id"));
                        list.setName(object.getString("name"));
                        list.setEmail(object.getString("email"));
                        list.setContact(object.getString("contact"));
                        list.setGender(object.getString("gender"));
                        list.setCollegeName(object.getString("collegeName"));
                        list.setStream(object.getString("stream"));
                        list.setSemester(object.getString("semester"));
                        list.setCreatedDate(object.getString("created_date"));
                        list.setVolunteer(object.getString("volunteer"));
                        arrayList.add(list);
                    }
                    adapter = new UserListAdapter(AdminUserListActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(AdminUserListActivity.this, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {

            }
        }
    }

    private class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyHolder> {

        Context context;
        ArrayList<UserList> arrayList;
        private ArrayList<UserList> searchList;
        int iPosition;
        String sId;

        UserListAdapter(Context context, ArrayList<UserList> arrayList) {
            this.context = context;
            this.arrayList = arrayList;
            this.searchList = new ArrayList<UserList>();
            this.searchList.addAll(arrayList);
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            arrayList.clear();
            if (charText.length() == 0) {
                arrayList.addAll(searchList);
            } else {
                for (UserList s : searchList) {
                    if (s.getVolunteer().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getStream().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getSemester().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getCollegeName().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getContact().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getCreatedDate().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getEmail().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getGender().toLowerCase(Locale.getDefault()).contains(charText)
                            || s.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        arrayList.add(s);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_user, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
            holder.name.setText(arrayList.get(position).getName());
            holder.email.setText(arrayList.get(position).getEmail());
            holder.contact.setText(arrayList.get(position).getContact());
            holder.college.setText(arrayList.get(position).getCollegeName() + " ( " + arrayList.get(position).getStream() + " , Semester : " + arrayList.get(position).getSemester() + " ) ");
            holder.date.setText(arrayList.get(position).getCreatedDate());

            if (arrayList.get(position).getVolunteer().equalsIgnoreCase("Yes")) {
                holder.addVolunteer.setVisibility(View.GONE);
            } else {
                if (sp.getString(ConstantSp.ADMIN_CLICK_POSITION, "").equalsIgnoreCase("1")) {
                    holder.addVolunteer.setVisibility(View.GONE);
                } else {
                    holder.addVolunteer.setVisibility(View.VISIBLE);
                }
            }

            holder.addVolunteer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sId = arrayList.get(position).getId();
                    if (new ConnectionDetector(context).isConnectingToInternet()) {
                        sId = arrayList.get(position).getId();
                        new addVolunteerData().execute();
                    } else {
                        new ConnectionDetector(context).connectiondetect();
                    }
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (new ConnectionDetector(context).isConnectingToInternet()) {
                        sId = arrayList.get(position).getId();
                        iPosition = position;
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
                if (sp.getString(ConstantSp.ADMIN_CLICK_POSITION, "").equalsIgnoreCase("1")) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", sId);
                    return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "deleteVolunteer.php", MakeServiceCall.POST, hashMap);
                } else {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", sId);
                    return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "deleteUser.php", MakeServiceCall.POST, hashMap);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                pd.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("Status").equalsIgnoreCase("True")) {
                        Toast.makeText(context, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                        arrayList.remove(iPosition);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {

                }
            }
        }

        private class addVolunteerData extends AsyncTask<String, String, String> {

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
                hashMap.put("userId", sId);
                return new MakeServiceCall().MakeServiceCall(ConstantSp.URL + "addVolunteer.php", MakeServiceCall.POST, hashMap);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                pd.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("Status").equalsIgnoreCase("True")) {
                        Toast.makeText(context, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, AdminUserListActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        Toast.makeText(context, jsonObject.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {

                }
            }
        }

        private class MyHolder extends RecyclerView.ViewHolder {

            TextView name, email, contact, college, date, addVolunteer;
            ImageView delete;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.custom_user_name);
                email = itemView.findViewById(R.id.custom_user_email);
                contact = itemView.findViewById(R.id.custom_user_contact);
                college = itemView.findViewById(R.id.custom_user_college);
                delete = itemView.findViewById(R.id.custom_user_delete);
                date = itemView.findViewById(R.id.custom_user_date);
                addVolunteer = itemView.findViewById(R.id.custom_user_add_volunteer);
            }
        }
    }
}
