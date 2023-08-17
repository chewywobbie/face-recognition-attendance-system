package com.example.unisnapattend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity {

    private Button lecturerButton;
    private Button studentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_activity);

        lecturerButton = findViewById(R.id.lecturer_button);
        studentButton = findViewById(R.id.student_button);

        lecturerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open lecturer sign-up activity
                Intent intent = new Intent(OptionsActivity.this, LectRegister.class);
                startActivity(intent);
            }
        });

        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open student sign-up activity
                Intent intent = new Intent(OptionsActivity.this, StdRegister.class);
                startActivity(intent);
            }
        });
    }
}
