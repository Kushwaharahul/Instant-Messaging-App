package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import Classes.User;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import Classes.User;

public class SelecttContactActivity extends AppCompatActivity {

    Button btnAddContact, btnStartChat,btnSelect;
    EditText etContactNumber;
    User user,contactUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectt_contact);
        setReferences();

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelecttContactActivity.this, AddContactsActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactUser.setPhone(etContactNumber.getText().toString());
                getUser();
                Intent intent = new Intent(SelecttContactActivity.this, ChatActivity.class);
                intent.putExtra("contactUser",contactUser);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

    }
    private void setReferences() {
        user=(User)getIntent().getSerializableExtra("user");
        contactUser=new User();
        btnAddContact = (Button) findViewById(R.id.btnAddContact);
        btnStartChat = (Button) findViewById(R.id.btnStartChat);
        etContactNumber = (EditText) findViewById(R.id.etContactNumber);

    }
    private void getUser() {

    }
}