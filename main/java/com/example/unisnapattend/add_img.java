package com.example.unisnapattend;

import static com.example.unisnapattend.MainActivity.KEY_USERNAME;
import static com.example.unisnapattend.MainActivity.SHARED_PREF_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.AsyncTask;



import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class add_img extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private TextView messageText;
    private Button uploadButton, btnselectpic;
    private ImageView imageview;

    private String upLoadServerUri = "http://172.20.10.5/attendance/upload_addimg.php";
    private String imagepath = null;
    private String username;
    private ProgressDialog dialog;
    private int serverResponseCode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagetest);

        uploadButton = findViewById(R.id.uploadButton);
        messageText = findViewById(R.id.messageText);
        btnselectpic = findViewById(R.id.button_selectpic);
        imageview = findViewById(R.id.imageView_pic);
        username = getSavedUsername();

        btnselectpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Check for the permission before selecting the image
                if (ContextCompat.checkSelfPermission(add_img.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
                } else {
                    // You can show a message here indicating that permission is required to select images
                    Toast.makeText(add_img.this, "Permission required to access images.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Check if the imagepath and username are available
                if (imagepath != null && !username.isEmpty()) {
                    new UploadFileTask().execute(imagepath, username);
                } else {
                    Toast.makeText(add_img.this, "Please select an image and log in again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check for runtime permissions (Android 6.0 and above)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    private class UploadFileTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(add_img.this, "", "Uploading file...", true);
            messageText.setText("uploading started.....");
        }

        @Override
        protected Integer doInBackground(String... params) {
            String sourceFileUri = params[0];
            String username = params[1];

            return uploadFile(sourceFileUri, username);
        }

        @Override
        protected void onPostExecute(Integer serverResponseCode) {
            dialog.dismiss();
            if (serverResponseCode == 200) {
                String msg = "File Upload Completed.";
                messageText.setText(msg);
                Toast.makeText(add_img.this, "Image Upload Successful.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(add_img.this, Prof_std.class));
            } else {
                messageText.setText("Failed to upload file.");
                Toast.makeText(add_img.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToExternalStorage(Bitmap image) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // Use the Pictures directory
        File imageFile = new File(storageDir, imageFileName);
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out); // Compress the image
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageFile.getAbsolutePath();
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int uploadFile(String sourceFileUri, String username) {
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist: " + sourceFileUri);

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist: " + sourceFileUri);
                }
            });

            return 0;

        } else {
            try {

                URL url = new URL(upLoadServerUri);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                dos = new DataOutputStream(conn.getOutputStream());

                // Write the username parameter to the request
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"username\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(username);
                dos.writeBytes(lineEnd);

                // Create FileInputStream for the source file
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // Create a buffer of the maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is: " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.";
                            messageText.setText(msg);
                            Toast.makeText(add_img.this, "Image Upload Successful.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(add_img.this, Prof_std.class));
                        }
                    });
                }
                fileInputStream.close();
                dos.flush();
                dos.close();

                dialog.dismiss();
                return serverResponseCode;

            } catch (MalformedURLException ex) {
                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception: check script URL.");
                        Toast.makeText(add_img.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "Error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception: see logcat");
                        Toast.makeText(add_img.this, "Got Exception: see logcat", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "Exception: " + e.getMessage(), e);
            }
            dialog.dismiss();
            return 0;
        } // End else block
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imagepath = saveImageToExternalStorage(bitmap);
                    imageview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
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