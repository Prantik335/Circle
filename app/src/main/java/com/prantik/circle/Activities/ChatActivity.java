package com.prantik.circle.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prantik.circle.Adapters.MessagesAdapter;
import com.prantik.circle.Models.Message;
import com.prantik.circle.R;
import com.prantik.circle.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    String receiverUid;
    String senderUid;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String image = intent.getStringExtra("image");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Toolbar toolbar = binding.myToolBar;

        toolbar.setTitle(name);

        Glide.with(this).load(image)
                .placeholder(R.drawable.avatar)
                .into(binding.profileImage);

        binding.backButton.setOnClickListener(view -> {
            finish();
        });

        receiverUid = intent.getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, new String[]{senderRoom, receiverRoom});
        binding.msgRecyclerView.setAdapter(adapter);


        database = FirebaseDatabase.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                            Message message = msgSnapshot.getValue(Message.class);
                            message.setMessageId(msgSnapshot.getKey());
                            messages.add(message);
                        }
                        Collections.reverse(messages);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendButton.setOnClickListener(this::sentMessage);
    }

    private void sentMessage(View view) {
        String msg = binding.messageTextField.getText().toString();
        binding.messageTextField.setText("");

        Message message = new Message(msg, senderUid, new Date().getTime());

        String key = database.getReference().push().getKey();


        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .child(key)
                .setValue(message)
                .addOnSuccessListener(v -> {
                    database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(key)
                            .setValue(message)
                            .addOnFailureListener(e -> {
                                binding.messageTextField.setText(msg);
                            });

                    HashMap<String, Object> lastMessage = new HashMap<>();
                    lastMessage.put("last_message", message.getMessage());
                    lastMessage.put("last_message_time", new Date().getTime());
                    lastMessage.put("last_message_sender_id", senderUid);

                    database.getReference().child("chats").child(senderRoom)
                            .updateChildren(lastMessage);
                    database.getReference().child("chats").child(receiverRoom)
                            .updateChildren(lastMessage);

                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}