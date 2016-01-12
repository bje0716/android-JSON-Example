package com.grapefruit.jsonexample;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    ConnectivityManager connectivityManager;
    NetworkInfo mobile, wifi;
    JSONObject jsonObject;
    JSONArray jsonArray;
    String str, json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (isNetwork()) {
            finish();
        } else {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("데이터 불러오는 중...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new post().execute();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new JsonAdapter());
    }

    private Boolean isNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mobile.isConnected() && wifi.isConnected();
    }

    private class post extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("http://bje0716.iptime.org/json.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "utf-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    builder.append(str + "\n");
                }
                json = builder.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("json", json);
                        SharedPreferences sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("json", json);
                        editor.commit();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private class JsonViewHolder extends RecyclerView.ViewHolder {

        TextView board, brand, device;

        public JsonViewHolder(View itemView) {
            super(itemView);
            board = (TextView) itemView.findViewById(R.id.board);
            brand = (TextView) itemView.findViewById(R.id.brand);
            device = (TextView) itemView.findViewById(R.id.device);
        }

    }

    private class JsonAdapter extends RecyclerView.Adapter<JsonViewHolder> {

        public List<JsonItem> items;

        public JsonAdapter() {
            super();
            items = new ArrayList<>();

            SharedPreferences shared = getSharedPreferences("pref", MODE_PRIVATE);
            String asdf = shared.getString("json", "");
            Log.d("asdf", asdf);
            try {
                jsonArray = new JSONArray(asdf);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String brand = jsonObject.getString("brand");
                    String board = jsonObject.getString("board");
                    String device = jsonObject.getString("device");
                    items.add(new JsonItem(board, brand, device));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public JsonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment, parent, false);
            return new JsonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(JsonViewHolder holder, int position) {
            JsonItem item = items.get(position);
            holder.board.setText(item.getBoard());
            holder.brand.setText(item.getBrand());
            holder.device.setText(item.getDevice());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class JsonItem {

        String board, brand, device;

        public JsonItem(String board, String brand,String device) {
            this.board = board;
            this.brand = brand;
            this.device = device;
        }

        public String getBoard() {
            return this.board;
        }

        public String getBrand() {
            return this.brand;
        }

        public String getDevice() {
            return this.device;
        }
    }
}
