package com.example.unisnapattend;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LectRegister extends AppCompatActivity {
    private List<String> selectedSubjects;
    private EditText usernameEditText; // Declare as instance variable
    private EditText passwordEditText; // Declare as instance variable
    private EditText nameEditText; // Declare as instance variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_lect);

        usernameEditText = findViewById(R.id.txtusername);
        passwordEditText = findViewById(R.id.txtpw);
        nameEditText = findViewById(R.id.txtname);

        selectedSubjects = new ArrayList<>();

        // Fetch the classes data from the server
        fetchClassesData();

        Button submitButton = findViewById(R.id.reg_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedSubjectsString = getSelectedSubjectsString();
                Toast.makeText(LectRegister.this, "Selected subjects: " + selectedSubjectsString, Toast.LENGTH_SHORT).show();
                registerLecturer();
            }
        });
        // Debug log to check if the activity is created
        Log.d("LectRegister", "onCreate: Activity created successfully");
    }

    private void fetchClassesData() {
      String baseUrl = "http://172.20.10.5/attendance/fetch_classes.php";
    //   String baseUrl = "http://192.168.137.209/attendance/fetch_classes.php";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(baseUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle network failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Parse the JSON response and get the list of classes
                    List<String> classesList = parseClassesData(responseData);

                    // Update the UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateCheckboxes(classesList);

                            // Enable the submit button after classes data is loaded
                            Button submitButton = findViewById(R.id.reg_button);
                            submitButton.setEnabled(true);
                        }
                    });
                } else {
                    // Handle error response
                    // e.g., show an error message or retry the request
                }
            }
        });
    }

    private void populateCheckboxes(List<String> classesList) {
        LinearLayout checkboxContainer = findViewById(R.id.classCheckboxContainer);

        for (String className : classesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(className);
            checkBox.setOnCheckedChangeListener(checkBoxListener);
            checkboxContainer.addView(checkBox);
        }
    }

    private final CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String subject = buttonView.getText().toString();
            if (isChecked) {
                selectedSubjects.add(subject);
            } else {
                selectedSubjects.remove(subject);
            }
        }
    };



    // Parse the JSON response to get the list of classes
    private List<String> parseClassesData(String jsonData) {
        List<String> classesList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject classObject = jsonArray.getJSONObject(i);
                String className = classObject.getString("classname");
                classesList.add(className);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return classesList;
    }


    private String getSelectedSubjectsString() {
        StringBuilder sb = new StringBuilder();
        for (String subject : selectedSubjects) {
            sb.append(subject).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);  // Remove the trailing comma and space
        }
        return sb.toString();
    }

    private void registerLecturer() {
//        String baseUrl = "http://172.20.10.5/attendance/lect_register_app.php";
       String baseUrl = "http://172.20.10.5/attendance/lect_register_app.php"; // Replace with your actual backend API URL
        OkHttpClient client = new OkHttpClient();

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String lectName = nameEditText.getText().toString();
        List<String> selectedClasses = selectedSubjects; // Get the selected classes from the checkboxes

        // Prepare the request body with the data
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("pw", password)
                .add("lectname", lectName)
                .add("classes", TextUtils.join(",", selectedClasses))
                .build();

        // Create the POST request
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle network failure
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LectRegister.this, "Failed to register lecturer. Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.d("LectRegister", "Response: " + responseData); // Log the response from the server
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LectRegister.this, message, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LectRegister.this, MainActivity.class));
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LectRegister.this, "Failed to register lecturer. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Log the data being sent in the request
        String requestData = "username=" + username + "&pw=" + password + "&lectname=" + lectName + "&classes=" + TextUtils.join(",", selectedClasses);
        Log.d("LectRegister", "Request Data: " + requestData);
    }
}

