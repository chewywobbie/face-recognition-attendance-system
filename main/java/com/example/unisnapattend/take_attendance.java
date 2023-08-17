package com.example.unisnapattend;

import static com.example.unisnapattend.MainActivity.KEY_USERNAME;
import static com.example.unisnapattend.MainActivity.SHARED_PREF_NAME;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class take_attendance extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_BROWSE = 2;

    private static final String SERVER_URL = "http://172.20.10.5/attendance/take_attendance.php";
    private TextView messageText;
    private Intent data;
    private String capturedImagePath;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.takeattend);
        fetchClassesData();
        setupSpinner();
        messageText = findViewById(R.id.messageText);

        // Button for opening camera
        Button btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(v -> openCamera());
        // Button for browsing image
        Button btnBrowse = findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(v -> openFileBrowser()); // Call the method for file browsing

        // Button for uploading the captured image (Implement your upload logic here)
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> uploadImage());

        Spinner subjSpinner = findViewById(R.id.classspinner);
        subjSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Exclude the "Select Class:" position
                    String selectedClassName = (String) parent.getItemAtPosition(position);
                    // Handle the selected class name here (if needed)
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where nothing is selected (if needed)
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(take_attendance.this, LectPage.class));
                    return true;
                case R.id.action_attend:
                    startActivity(new Intent(take_attendance.this, take_attendance.class));
                    Toast.makeText(take_attendance.this, "You are already in the Take Attendance Page", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_profile:
                    startActivity(new Intent(take_attendance.this, Prof_lect.class));
                    return true;
                default:
                    return false;
            }
        });
    }

    private void openFileBrowser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_BROWSE);
        } else {
            Toast.makeText(this, "Permission required to access images.", Toast.LENGTH_SHORT).show();
        }
    }

    // Open camera to capture an image
    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                return;
            }
        }

        // Create a file to store the captured image
        File imageFile = new File(getExternalFilesDir(null), "captured_image.jpg");
        capturedImagePath = imageFile.getAbsolutePath();

        // Create the camera intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); // Pass the photo URI to the camera intent
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.data = data;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Load the full-resolution image from the file path
            Bitmap imageBitmap = BitmapFactory.decodeFile(capturedImagePath);
            ImageView imageView = findViewById(R.id.imageView_pic);
            imageView.setImageBitmap(imageBitmap);
            messageText.setText("Image captured successfully.");

        } else if (requestCode == REQUEST_IMAGE_BROWSE && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();
            String imagepath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            ImageView imageView = findViewById(R.id.imageView_pic);
            imageView.setImageBitmap(bitmap);
            messageText.setText("Image selected from gallery: " + imagepath);
        }
    }

    // ...

    private void uploadImage() {
        Bitmap imageBitmap = null;

        // Get the Bitmap from the ImageView
        ImageView imageView = findViewById(R.id.imageView_pic);
        if (imageView.getDrawable() instanceof BitmapDrawable) {
            imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        if (imageBitmap == null) {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the classname from the selected item in the spinner
        Spinner classSpinner = findViewById(R.id.classspinner);
        String className = classSpinner.getSelectedItem().toString();

        if (className.equals("Select Class:")) {
            Toast.makeText(this, "Please select a class.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the selected class name for debugging purposes
        Log.d("UploadImageTask", "Selected class name: " + className);

        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String fileName = timeStamp+".jpg"; // You may change this to an appropriate filename

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();


        // Call the UploadImageTask with the correct parameters
        UploadImageTask uploadImageTask = new UploadImageTask();
        uploadImageTask.execute(className, byteArray, fileName);
    }

    private class UploadImageTask extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(take_attendance.this, "", "Uploading file...", true);
            messageText.setText("uploading started.....");
        }

        @Override
        protected String doInBackground(Object... params) {
            String className = (String) params[0];
            byte[] imageData = (byte[]) params[1];
            String fileName = (String) params[2];
            String boundary = "*****";
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            try {
                // Create a URL object
                URL url = new URL(SERVER_URL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true);
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // Write the classname parameter to the request
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"classname\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(className);
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                dos.write(imageData);

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Read the response from the server
                int responseCode = conn.getResponseCode();
                Log.d("UploadImageTask", "Response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    is.close();
                    return response.toString();
                } else {
                    return "Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("UploadImageTask", "Exception while uploading image: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            Toast.makeText(take_attendance.this, result, Toast.LENGTH_SHORT).show();
        }
    }


    private void setupSpinner() {
        Spinner dropdown = findViewById(R.id.classspinner);
        List<String> classNamesList = new ArrayList<>();
        classNamesList.add(0, "Select Class:");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout_class, android.R.id.text1, classNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);
    }


    public class FetchClassesTask extends AsyncTask<String, Void, String> {
        private take_attendance activity;

        public FetchClassesTask(take_attendance activity) { // Change the constructor parameter type
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
            // Instantiate the FetchClassesTask correctly
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
            Spinner dropdown = findViewById(R.id.classspinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout_class,  android.R.id.text1, classNamesList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dropdown.setAdapter(adapter);
            // Set the default initial value as "Select Course:"
//            int defaultPosition = adapter.getPosition("Select Course:");
//            dropdown.setSelection(defaultPosition);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("take_attendance", "JSON parsing error: " + e.getMessage());
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
