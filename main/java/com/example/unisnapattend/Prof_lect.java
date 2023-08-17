package com.example.unisnapattend;

import static com.example.unisnapattend.MainActivity.KEY_USERNAME;
import static com.example.unisnapattend.MainActivity.SHARED_PREF_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class Prof_lect extends AppCompatActivity {
    private TextView lecturerNameTextView;
    private TextView lecturerClassesTextView;
    private Button logoutBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prof_lect);

        lecturerNameTextView = findViewById(R.id.txt_lectname);
        lecturerClassesTextView = findViewById(R.id.txt_lectclasses);
        logoutBtn = findViewById(R.id.lectbtn_logout);


        // Fetch the lecturer's username from shared preferences
        String username = getSavedUsername();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        // Pass the username to the PHP script and receive data from the database
        fetchLecturerNameData(username);
        fetchLecturerClassesData(username);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(Prof_lect.this, LectPage.class));
                    return true;
                case R.id.action_attend:
                    startActivity(new Intent(Prof_lect.this, take_attendance.class));

                    return true;
                case R.id.action_profile:
                    startActivity(new Intent(Prof_lect.this, Prof_lect.class));
                    Toast.makeText(Prof_lect.this, "You are already in Profile", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });
    }

    private void logoutUser() {
        // Clear login credentials from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(Prof_lect.this, "LOGGED OUT", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Prof_lect.this, MainActivity.class));
        finish(); // Finish the current activity to prevent going back to it using the back button
    }

    private void fetchLecturerNameData(String username) {
        // Instantiate the FetchLecturerNameTask correctly
        FetchLecturerNameTask task = new FetchLecturerNameTask(this);
        task.execute(username);
    }


    private void fetchLecturerClassesData(String username) {
        // Instantiate the FetchLecturerClassesTask correctly
        FetchLecturerClassesTask task = new FetchLecturerClassesTask(this);
        task.execute(username);
    }


    public class FetchLecturerNameTask extends AsyncTask<String, Void, String> {
        private Prof_lect activity;

        public FetchLecturerNameTask(Prof_lect activity) {
            this.activity = activity;
        }


        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_lect_name.php");
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
                    Log.e("FetchLecturerNameTask", "Error response code: " + responseCode);
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FetchLecturerNameTask", "Exception while fetching data: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            super.onPostExecute(responseData);
            if (responseData != null) {
                activity.handleLecturerNameData(responseData);
            } else {
                Log.e("FetchLecturerNameTask", "Response data is null.");
            }
        }
    }

    public void handleLecturerNameData(String responseData) {
        Log.d("Prof_lect", "JSON response: " + responseData);

        try {
            // Check if the response contains "<br", which is not part of the JSON data
            if (responseData.contains("<br")) {
                Log.e("Prof_lect", "Unexpected response data: " + responseData);
                return;
            }

            JSONObject jsonObject = new JSONObject(responseData);

            if (jsonObject.has("lecturer_name")) {
                String lecturerName = jsonObject.getString("lecturer_name");
                lecturerNameTextView.setText(lecturerName);
            } else if (jsonObject.has("error")) {
                String error = jsonObject.getString("error");
                // Handle error response here if necessary
                // ...
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Prof_lect", "JSON parsing error: " + e.getMessage());
            // Handle JSON parsing error here, if necessary
            // ...
        }
    }


    public class FetchLecturerClassesTask extends AsyncTask<String, Void, String> {
        private Prof_lect activity;

        public FetchLecturerClassesTask(Prof_lect activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_classescodes_lect.php");
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
                // Log the responseData to see what is being received
                Log.d("ResponseData", responseData);

                // Now try parsing the JSON data
                activity.handleLecturerClassesData(responseData);
            } else {
                Log.e("FetchLecturerClassesTask", "Response data is null.");
            }
        }
    }

    // Define the Subject class
    public static class Subject {
        private String subjectName;
        private String subjectCode;

        public Subject(String subjectName, String subjectCode) {
            this.subjectName = subjectName;
            this.subjectCode = subjectCode;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getSubjectCode() {
            return subjectCode;
        }
    }

    public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

        private List<Subject> subjectList;

        public SubjectAdapter(List<Subject> subjectList) {
            this.subjectList = subjectList;
        }

        @NonNull
        @Override
        public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
            return new SubjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
            Subject subject = subjectList.get(position);
            holder.subjectNameTextView.setText(subject.getSubjectName());
            holder.subjectCodeTextView.setText(subject.getSubjectCode());
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public class SubjectViewHolder extends RecyclerView.ViewHolder {

            private TextView subjectNameTextView;
            private TextView subjectCodeTextView;

            public SubjectViewHolder(@NonNull View itemView) {
                super(itemView);
                subjectNameTextView = itemView.findViewById(R.id.subjectNameTextView);
                subjectCodeTextView = itemView.findViewById(R.id.subjectCodeTextView);
            }
        }
    }

    public void handleLecturerClassesData(String responseData) {
        try {
            // Check if the response contains "<br", which is not part of the JSON data
            if (responseData.contains("<br")) {
                Log.e("Prof_lect", "Unexpected response data: " + responseData);
                return;
            }

            JSONArray jsonArray = new JSONArray(responseData);

            // Initialize a list to store class data
            List<Subject> subjectList = new ArrayList<>();

            // Loop through the JSON array and extract class data
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject classObject = jsonArray.getJSONObject(i);
                String classCode = classObject.getString("classcode");
                String className = classObject.getString("classname");
                subjectList.add(new Subject(classCode,className));
            }


            // Create a string to display all the classes for the lecturer
            StringBuilder classesText = new StringBuilder();
            for (Subject subject : subjectList) {
                classesText.append(subject.getSubjectName()).append(" (").append(subject.getSubjectCode()).append(")\n");
            }

            // Set the text in the TextView to display the classes
            lecturerClassesTextView.setText(classesText.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Prof_lect", "JSON parsing error: " + e.getMessage());
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
