package com.example.unisnapattend;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import android.os.AsyncTask;

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
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREF_NAME = "MyPrefs";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView username = findViewById(R.id.txtusername);
        TextView pw = findViewById(R.id.txtpw);
        Button loginbtn = findViewById(R.id.loginbtn);
        TextView signup_btn = findViewById(R.id.signup_btn);


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send login credentials to the PHP server
                String enteredUsername = username.getText().toString();
                String enteredPassword = pw.getText().toString();

                sendLoginDataToServer(enteredUsername, enteredPassword);

            }
        });
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to options activity or sign up activity
                // Replace OptionsActivity.class with your intended activity
                startActivity(new Intent(MainActivity.this, OptionsActivity.class));
            }
        });

    }

    private void saveLoginCredentials(String username, String password) {
        try {
            // Create a JSON object to store the login credentials
            JSONObject loginCredentials = new JSONObject();
            loginCredentials.put("username", username);
            loginCredentials.put("password", password);

            // Convert the JSON object to a JSON string
            String jsonCredentials = loginCredentials.toString();

            // Save the JSON string in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("login_credentials", jsonCredentials);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendLoginDataToServer(String username, String password) {
        String url = "http://172.20.10.5/attendance/check_logininfo_app.php";
        new LoginTask().execute(url, username, password);
    }
    private class LoginTask extends AsyncTask<String, Void, String> {
        private String username;
        private String password;

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String username = params[1];
            String password = params[2];
            String response = "";
            this.username = username;
            this.password = password;

            try {
                URL loginUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                Map<String, String> postDataParams = new HashMap<>();
                postDataParams.put("login", "true");
                postDataParams.put("username", username);
                postDataParams.put("pw", password);

                // Log request parameters
                for (Map.Entry<String, String> entry : postDataParams.entrySet()) {
                    System.out.println("Parameter: " + entry.getKey() + " = " + entry.getValue());
                }

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(getPostDataString(postDataParams).getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response from server
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }
                    reader.close();
                } else {
                    response = "ERROR";
                }

                // Log server response
                System.out.println("Server Response: " + response);

                conn.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                response = "ERROR";
            }

            return response;
        }


        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    // Login was successful, extract accesslvl and redirect accordingly
                    int accesslvl = jsonResponse.getInt("accesslvl");
                    if (accesslvl == 2) {
                        saveLoginCredentials(username, password);
                        Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LectPage.class));
                    } else if (accesslvl == 3) {
                        saveLoginCredentials(username, password);
                        Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, StdPage.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Unknown access level", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Login failed
                    Toast.makeText(MainActivity.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing error here, if necessary
                Toast.makeText(MainActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }

        private String getPostDataString(Map<String, String> params) throws IOException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(entry.getKey());
                result.append("=");
                result.append(entry.getValue());
            }
            return result.toString();
        }
    }


}

