package com.example.unisnapattend;

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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class Prof_std extends AppCompatActivity {

    private TextView txtStdName, txtStdMatric, txtStdCourse, txtStdClasses, txtImageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prof_std);

        txtStdName = findViewById(R.id.txt_stdname);
        txtStdMatric = findViewById(R.id.txt_stdmatric);
        txtStdCourse = findViewById(R.id.txt_stdcourse);
        txtStdClasses = findViewById(R.id.txt_stdclasses);
        txtImageCount = findViewById(R.id.imagecount);
        Button btn_addimg = findViewById(R.id.btn_add_image);

        // Fetch image count from the server
        String username = getSavedUsername(); // Get the logged-in student's username
        new FetchImageCountTask().execute(username);

        // Fetch student details for the current student
        new FetchStudentDetailsTask().execute(username);

        btn_addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Prof_std.this, add_img.class));
            }
        });

        Button logoutBtn = findViewById(R.id.btn_logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(Prof_std.this, StdPage.class));
                    return true;
                case R.id.action_profile:
                    startActivity(new Intent(Prof_std.this, Prof_std.class));
                    Toast.makeText(Prof_std.this, "You are already in Profile", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(Prof_std.this, "LOGGED OUT", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Prof_std.this, MainActivity.class));
        finish(); // Finish the current activity to prevent going back to it using the back button
    }

    private class FetchImageCountTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String username = params[0];
            String url = "http://172.20.10.5/attendance/count_imgstd.php";

            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Create the data to send in the request body
                String postData = "username=" + username;

                // Write the data to the request body
                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                // Get the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse the response to get the image count
                    JSONObject jsonObject = new JSONObject(response.toString());
                    int count = jsonObject.optInt("num_imgpath", 0);
                    return count;
                } else {
                    // Debugging: Print the response code if it is not HTTP_OK
                    System.out.println("Response code: " + responseCode);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            // Return 0 if there was an error or the response code was not HTTP_OK
            return 0;
        }

        @Override
        protected void onPostExecute(Integer imageCount) {
            System.out.println("Response from server (imageCount): " + imageCount);

            // Update the TextView with the image count
            txtImageCount.setText("You have submitted " + imageCount + " of your images in our system");
        }
    }

    private class FetchStudentDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String savedUsername = params[0];

            try {
                URL url = new URL("http://172.20.10.5/attendance/fetch_stdprofile.php");
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
                    JSONObject jsonObject = new JSONObject(responseData);

                    String stdName = jsonObject.optString("stdname", "");
                    String stdMatric = jsonObject.optString("stdmatric", "");
                    String stdCourse = jsonObject.optString("stdcourse", "");
                    String stdClasses = jsonObject.optString("stdclasses", "");

                    // Update the TextViews with the student details
                    txtStdName.setText(stdName);
                    txtStdMatric.setText(stdMatric);
                    txtStdCourse.setText(stdCourse);
                    txtStdClasses.setText(stdClasses);
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

