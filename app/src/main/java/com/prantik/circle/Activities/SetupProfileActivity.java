package com.prantik.circle.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prantik.circle.R;
import com.prantik.circle.Models.User;
import com.prantik.circle.databinding.ActivitySetupProfileBinding;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.editUserName.requestFocus();
        binding.imageView.setOnClickListener(this::selectImage);

        binding.saveButton.setOnClickListener(this::onSave);
    }

    private void onSave(View view) {
        String name = binding.editUserName.getText().toString().trim();

        if (name.isEmpty()) {
            binding.editUserName.setError("please type your name.");
            return;
        }

        binding.savingStatus.setVisibility(View.VISIBLE);

        if (selectedImage != null) {
            StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
            reference.putFile(selectedImage).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> saveUserData(uri, name));
                }
            });
        } else {
            saveUserData(Uri.parse(""), name);
        }
    }

    public void saveUserData(Uri uri, String name) {
        String imageUrl = uri.toString();
        String uid = auth.getUid();
        String phone = auth.getCurrentUser().getPhoneNumber();
        User user = new User(uid, name, phone, imageUrl);

        // saving user's data to firebase
        database.getReference()
                .child("users")
                .child(uid)
                .setValue(user)
                .addOnSuccessListener(this::onSaveSuccess);
    }

    public void onSaveSuccess(Void aVoid) {
        Intent intent = new Intent(this, MainActivity.class);
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    private void selectImage(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (requestCode == 99 && data.getData() != null) {
            binding.imageView.setImageURI(data.getData());
            selectedImage = data.getData();
        }
    }
}