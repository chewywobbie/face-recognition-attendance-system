package com.example.unisnapattend;
import static com.example.unisnapattend.MainActivity.KEY_PASSWORD;
import static com.example.unisnapattend.MainActivity.KEY_USERNAME;
import static com.example.unisnapattend.MainActivity.SHARED_PREF_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LectPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_lect);
        fetchClassesData();
        setupSpinner(); // Add this line to set up the spinner

        // Fetch attendance data when a class is selected from the spinner
        Spinner subjSpinner = findViewById(R.id.subj_spinner);
        subjSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Exclude the "Select Class:" position
                    String selectedClassName = (String) parent.getItemAtPosition(position);
                    fetchAttendanceData(selectedClassName);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(LectPage.this, LectPage.class));
                    Toast.makeText(LectPage.this, "You are already in Home", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_attend:
                    startActivity(new Intent(LectPage.this, take_attendance.class));
                    return true;
                case R.id.action_profile:
                    startActivity(new Intent(LectPage.this, Prof_lect.class));
                    return true;
                default:
                    return false;
            }
        });
    }

    public class FetchAttendanceTask extends AsyncTask<String, Void, String> {
        private LectPage activity;

        public FetchAttendanceTask(LectPage activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];
            String selectedClassName = params[1];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_attendance_lect.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Set the request parameters
                String postData = "username=" + URLEncoder.encode(savedUsername, "UTF-8")
                        + "&classname=" + URLEncoder.encode(selectedClassName, "UTF-8");

                // Write the request parameters to the output stream
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes());
                os.flush();
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
                    Log.e("FetchAttendanceTask", "Error response code: " + responseCode);
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FetchAttendanceTask", "Exception while fetching data: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            super.onPostExecute(responseData);
            if (responseData != null) {
                activity.displayAttendanceData(responseData);
            } else {
                Log.e("FetchAttendanceTask", "Response data is null.");
            }
        }
    }

    // ...

    public void displayAttendanceData(String responseData) {
        // Parse the responseData and populate the table rows
        try {
            JSONArray jsonArray = new JSONArray(responseData);

            // Get the table layout
            TableLayout tableLayout = findViewById(R.id.table_layout);

            // Remove any existing table rows (except the header row)
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1);
            }

            // Loop through the JSON array and extract attendance data
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject attendanceObject = jsonArray.getJSONObject(i);
                String stdName = attendanceObject.getString("stdname");
                String stdMatricNo = attendanceObject.getString("stdmatricno");
                String attendDate = attendanceObject.getString("attend_date");
                String attendStatus = attendanceObject.getString("attend_status");

                // Create a new table row and add data
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));
                tableRow.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white)); // Set row background color

                TextView tvName = new TextView(this);
                tvName.setText(stdName);
                tvName.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvName.setPadding(16, 8, 16, 8); // Adjust padding as needed
                tableRow.addView(tvName);

                TextView tvMatricNo = new TextView(this);
                tvMatricNo.setText(stdMatricNo);
                tvMatricNo.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvMatricNo.setPadding(16, 8, 16, 8); // Adjust padding as needed
                tableRow.addView(tvMatricNo);

                TextView tvDate = new TextView(this);
                tvDate.setText(attendDate);
                tvDate.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvDate.setPadding(16, 8, 16, 8); // Adjust padding as needed
                tableRow.addView(tvDate);

                TextView tvStatus = new TextView(this);
                tvStatus.setText(attendStatus);
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvStatus.setPadding(16, 8, 16, 8); // Adjust padding as needed
                tableRow.addView(tvStatus);

                // Add the table row to the table layout
                tableLayout.addView(tableRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("LectPage", "JSON parsing error: " + e.getMessage());
            // Handle JSON parsing error here, if necessary
            // ...
        }
    }


    // Set up the spinner with the "Select Class:" item as default selection
    private void setupSpinner() {
        Spinner dropdown = findViewById(R.id.subj_spinner);
        List<String> classNamesList = new ArrayList<>();
        classNamesList.add(0, "Select Class:");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout_class, android.R.id.text1, classNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);
    }

    // Fetch attendance data based on the selected class from the spinner
    private void fetchAttendanceData(String selectedClassName) {
        String savedUsername = getSavedUsername();

        if (!savedUsername.isEmpty()) {
            FetchAttendanceTask task = new FetchAttendanceTask(this);
            task.execute(savedUsername, selectedClassName);
        } else {
            Toast.makeText(this, "Username parameter is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchClassesTask extends AsyncTask<String, Void, String> {
        private LectPage activity;

        public FetchClassesTask(LectPage activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_classes_lect.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Set the request parameters
                String postData = "username=" + URLEncoder.encode(savedUsername, "UTF-8");

                // Write the request parameters to the output stream
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes());
                os.flush();
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
                    Log.e("FetchClassesTask", "Error response code: " + responseCode);
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FetchClassesTask", "Exception while fetching data: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            super.onPostExecute(responseData);
            if (responseData != null) {
                activity.handleClassesData(responseData);
            } else {
                Log.e("FetchClassesTask", "Response data is null.");
            }
        }
    }

    private void fetchClassesData() {
        String savedUsername = getSavedUsername();

        if (!savedUsername.isEmpty()) {
            FetchClassesTask task = new FetchClassesTask(this);
            task.execute(savedUsername);
        } else {
            Toast.makeText(this, "Username parameter is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleClassesData(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);

            // Initialize a list to store class names
            List<String> classNamesList = new ArrayList<>();
            classNamesList.add(0, "Select Class:");
            // Loop through the JSON array and extract class names
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject classObject = jsonArray.getJSONObject(i);
                String className = classObject.getString("classname");
                classNamesList.add(className);

            }

            // Now you have a list of class names that you can use to populate the spinner
            Spinner dropdown = findViewById(R.id.subj_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout_class,  android.R.id.text1, classNamesList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dropdown.setAdapter(adapter);
            // Set the default initial value as "Select Course:"
//            int defaultPosition = adapter.getPosition("Select Course:");
//            dropdown.setSelection(defaultPosition);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("LectPage", "JSON parsing error: " + e.getMessage());
            // Handle JSON parsing error here, if necessary
            // ...
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
