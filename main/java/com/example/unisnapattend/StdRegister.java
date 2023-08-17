package com.example.unisnapattend;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class StdRegister extends AppCompatActivity {
    private List<String> selectedClasses;
    private EditText usernameEditText; // Declare as instance variable
    private EditText passwordEditText; // Declare as instance variable
    private EditText nameEditText; // Declare as instance variable
    private EditText matricNoEditText; // Declare as instance variable
    private Spinner courseSpinner; // Declare as instance variable



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_std);

        usernameEditText = findViewById(R.id.txtusername);
        passwordEditText = findViewById(R.id.txtpw);
        nameEditText = findViewById(R.id.txtstdname);
        matricNoEditText = findViewById(R.id.txtstdmatric);
        courseSpinner = findViewById(R.id.courseSpinner);
        selectedClasses = new ArrayList<>();


        // Fetch the courses data from the server
        fetchCoursesData();

        // Fetch the classes data from the server and populate checkboxes
        fetchClassesData();

        Button upl_btn = findViewById(R.id.upl_btn);
        upl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedClassesString = getSelectedClassesString();

                String selectedCourse = courseSpinner.getSelectedItem().toString();
                if (TextUtils.isEmpty(selectedCourse) || selectedCourse.equals("Select Course:")) {
                    Toast.makeText(StdRegister.this, "Please select a valid course.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method if the course is not selected or invalid
                }

                // Make sure selectedClasses is correctly updated before registration
                // selectedClasses = {class1, class2, ...}
                selectedClasses = getSelectedClasses();

                Toast.makeText(StdRegister.this, "Selected classes: " + selectedClassesString, Toast.LENGTH_SHORT).show();
                registerStudent();
                startActivity(new Intent(StdRegister.this, imgtest.class));
            }
        });


        // Debug log to check if the activity is created
        Log.d("StdRegister", "onCreate: Activity created successfully");
    }

    private void fetchCoursesData() {
        String baseUrl = "http://172.20.10.5/attendance/fetch_courses.php";
      //  String baseUrl = "http://192.168.137.209/attendance/fetch_courses.php"; // Replace with your actual backend API URL
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
                    // Parse the JSON response and get the list of courses
                    List<String> coursesList = parseCoursesData(responseData);

                    // Update the UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateCourseSpinner(coursesList);

                            // Enable the submit button after courses data is loaded
                            Button upl_btn = findViewById(R.id.upl_btn);
                            upl_btn.setEnabled(true);
                        }
                    });
                } else {
                    String responseData = response.body().string();
                    Log.d("StdRegister", "ResponseFail: " + responseData);
                }
            }
        });
    }

    // Parse the JSON response to get the list of courses
    private List<String> parseCoursesData(String jsonData) {
        List<String> coursesList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject courseObject = jsonArray.getJSONObject(i);
                String courseName = courseObject.getString("coursename");
                coursesList.add(courseName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return coursesList;
    }

    private void populateCourseSpinner(List<String> coursesList) {
        // Add the default value to the list of courses
        coursesList.add(0, "Select Course:");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout_course, android.R.id.text1, coursesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);

        // Set the default initial value as "Select Course:"
        int defaultPosition = adapter.getPosition("Select Course:");
        courseSpinner.setSelection(defaultPosition);
    }


    private void fetchClassesData() {
        String baseUrl = "http://172.20.10.5/attendance/fetch_classes.php";
 //       String baseUrl = "http://192.168.137.209/attendance/fetch_classes.php"; // Replace with your actual backend API URL
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
                            populateClassCheckboxes(classesList);

                            // Enable the submit button after classes data is loaded
                            Button upl_btn = findViewById(R.id.upl_btn);
                            upl_btn.setEnabled(true);
                        }
                    });
                } else {
                    // Handle error response
                    // e.g., show an error message or retry the request
                }
            }
        });
    }

    private void populateClassCheckboxes(List<String> classesList) {
        LinearLayout checkboxContainer = findViewById(R.id.std_classCheckboxContainer);

        for (String className : classesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(className);
            checkBox.setOnCheckedChangeListener(checkBoxListener);
            checkboxContainer.addView(checkBox);
        }
    }

    private List<String> getSelectedClasses() {
        List<String> selectedClasses = new ArrayList<>();

        LinearLayout checkboxContainer = findViewById(R.id.std_classCheckboxContainer);
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            View childView = checkboxContainer.getChildAt(i);
            if (childView instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) childView;
                if (checkBox.isChecked()) {
                    selectedClasses.add(checkBox.getText().toString());
                }
            }
        }

        return selectedClasses;
    }


    private final CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String subject = buttonView.getText().toString();
            if (isChecked) {
                selectedClasses.add(subject);
            } else {
                selectedClasses.remove(subject);
            }
        }
    };


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


    private String getSelectedClassesString() {
        StringBuilder sb = new StringBuilder();
        for (String className : selectedClasses) {
            sb.append(className).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);  // Remove the trailing comma and space
        }
        return sb.toString();
    }

    private void registerStudent() {
        String baseUrl = "http://172.20.10.5/attendance/std_register_app.php";
   //     String baseUrl = "http://192.168.137.209/attendance/std_register_app.php"; // Replace with your actual backend API URL
        OkHttpClient client = new OkHttpClient();

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String studentName = nameEditText.getText().toString();
        String matricNo = matricNoEditText.getText().toString();
        String selectedCourse = courseSpinner.getSelectedItem().toString();
        List<String> selectedClasses = this.selectedClasses;




        // Prepare the request body with the data
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("pw", password)
                .add("stdname", studentName)
                .add("stdmatric", matricNo)
                .add("course", selectedCourse)
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
                        Toast.makeText(StdRegister.this, "Failed to register student. Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.d("StdRegister", "Response: " + responseData); // Log the response from the server
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StdRegister.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StdRegister.this, "Failed to parse server response. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Log the data being sent in the request
        String requestData = "username=" + username + "&pw=" + password + "&stdname=" + studentName + "&stdmatric=" + matricNo + "&course=" + selectedCourse + "&classes=" + TextUtils.join(",", selectedClasses);
        Log.d("StdRegister", "Request Data: " + requestData);
    }
}