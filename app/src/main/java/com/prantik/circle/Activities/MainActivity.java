package com.prantik.circle.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prantik.circle.Adapters.TopStatusAdapter;
import com.prantik.circle.Models.Status;
import com.prantik.circle.Models.UserStatus;
import com.prantik.circle.R;
import com.prantik.circle.Models.User;
import com.prantik.circle.Adapters.UsersAdapter;
import com.prantik.circle.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter topStatusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image....");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        topStatusAdapter = new TopStatusAdapter(this, userStatuses);
        binding.statusRecyclerView.setAdapter(topStatusAdapter);
        binding.statusRecyclerView.showShimmerAdapter();

        usersAdapter = new UsersAdapter(this, users);
        binding.recyclerView.setAdapter(usersAdapter);
        binding.recyclerView.showShimmerAdapter();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getUid().equals(currentUserUid)) {
                        MainActivity.this.user = dataSnapshot.getValue(User.class);
                        continue;
                    }

                    users.add(user);
                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("statuses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    userStatuses.clear();

                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(storySnapshot.child("name").getValue(String.class));
                        userStatus.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        userStatus.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status status = statusSnapshot.getValue(Status.class);
                            statuses.add(status);
                        }
                        userStatus.setStatuses(statuses);

                        userStatuses.add(userStatus);
                    }
                    binding.statusRecyclerView.hideShimmerAdapter();
                    topStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this::onOptionsItemSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                showToast("Search icon clicked.");
                break;
            case R.id.settings:
                showToast("Settings icon clicked.");
                break;
            case R.id.chats:
                break;
            case R.id.status:
                if (user == null) break;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 88);
                break;
            case R.id.calls:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);

        if (intentData == null) return;
        Uri data = intentData.getData();

        if (requestCode == 88 && data != null) {
            dialog.show();
            Date date = new Date();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference reference = storage.getReference().child("statuses")
                    .child(date.getTime() + "");

            reference.putFile(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(user.getName());
                        userStatus.setProfileImage(user.getProfileImage());
                        userStatus.setLastUpdated(date.getTime());

                        HashMap<String, Object> obj = new HashMap<>();

                        obj.put("name", userStatus.getName());
                        obj.put("profileImage", userStatus.getProfileImage());
                        obj.put("lastUpdated", userStatus.getLastUpdated());

                        Status status = new Status(uri.toString(), userStatus.getLastUpdated());

                        System.out.println("UID = " + user.getUid());

                        database.getReference()
                                .child("statuses")
                                .child(user.getUid())
                                .updateChildren(obj);

                        database.getReference()
                                .child("statuses")
                                .child(user.getUid())
                                .child("statuses")
                                .push()
                                .setValue(status);

                        dialog.dismiss();
                    });
                }
            });
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}