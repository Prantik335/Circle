package com.prantik.circle.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.prantik.circle.R;
import com.prantik.circle.databinding.ActivityPhoneAuthBinding;

public class PhoneAuthActivity extends AppCompatActivity {

    ActivityPhoneAuthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_auth);
        // Removing appBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Checking user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.etPhoneNumber.requestFocus();
        binding.continueButton.setOnClickListener(this::onContinue);
    }

    public void onContinue(View view) {
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        if (phoneNumber.length() > 6 && phoneNumber.length() < 16) {
            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("phoneNumber", phoneNumber);
            hideKeyboard(view);
            startActivity(intent);
        } else {
            Toast.makeText(this,
                    "Enter a valid phone number",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}