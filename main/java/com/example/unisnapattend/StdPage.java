package com.example.unisnapattend;
import static com.example.unisnapattend.MainActivity.KEY_USERNAME;
import static com.example.unisnapattend.MainActivity.SHARED_PREF_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class StdPage extends AppCompatActivity {

    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_std);

        tableLayout = findViewById(R.id.table_layout);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(StdPage.this, StdPage.class));
                    Toast.makeText(StdPage.this, "You are already in Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_profile:
                    startActivity(new Intent(StdPage.this, Prof_std.class));
                    return true;
                default:
                    return false;
            }
        });

        // Fetch attendance data for the current student
        String username = getSavedUsername();
        new FetchAttendanceDataTask().execute(username);
    }

    private class FetchAttendanceDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_attendance_std.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Set the request parameters
                String postData = "username=" + URLEncoder.encode(savedUsername, "UTF-8");

                // Write the request parameters to the output stream
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response from the server
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String responseData) {
            super.onPostExecute(responseData);
            if (responseData != null) {
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    // Add a log statement to print the JSON response for debugging
                    Log.d("JSON Response", jsonArray.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String date = jsonObject.getString("attend_date");
                        String classCode = jsonObject.getString("classcode");
                        String className = jsonObject.getString("classname");
                        String attendStatus = jsonObject.getString("attend_status");


                        // Create a new TableRow and populate it with data
                        TableRow row = new TableRow(StdPage.this);
                        row.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));
                        row.setBackgroundColor(ContextCompat.getColor(StdPage.this, android.R.color.white)); // Set row background color

                        TextView dateTextView = new TextView(StdPage.this);
                        dateTextView.setText(date);
                        dateTextView.setTextColor(ContextCompat.getColor(StdPage.this, android.R.color.black));
                        dateTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                        row.addView(dateTextView);

                        TextView classCodeTextView = new TextView(StdPage.this);
                        classCodeTextView.setText(classCode);
                        classCodeTextView.setTextColor(ContextCompat.getColor(StdPage.this, android.R.color.black));
                        classCodeTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                        row.addView(classCodeTextView);

                        TextView classNameTextView = new TextView(StdPage.this);
                        classNameTextView.setText(className);
                        classNameTextView.setTextColor(ContextCompat.getColor(StdPage.this, android.R.color.black));
                        classNameTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                        row.addView(classNameTextView);

                        TextView attendStatusTextView = new TextView(StdPage.this);
                        attendStatusTextView.setText(attendStatus);
                        attendStatusTextView.setTextColor(ContextCompat.getColor(StdPage.this, android.R.color.black));
                        attendStatusTextView.setPadding(16, 8, 16, 8); // Adjust padding as needed
                        row.addView(attendStatusTextView);


                        tableLayout.addView(row);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Add a log statement to print the error response for debugging
                Log.e("JSON Response Error", "Response is null or empty.");
            }
        }
    }

    private JSONObject getLoginCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String jsonCredentials = sharedPreferences.getString("login_credentials", null);

        if (jsonCredentials != null) {
            try {
                return new JSONObject(jsonCredentials);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private String getSavedUsername() {
        JSONObject loginCredentials = getLoginCredentials();
        if (loginCredentials != null) {
            try {
                return loginCredentials.getString(KEY_USERNAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
