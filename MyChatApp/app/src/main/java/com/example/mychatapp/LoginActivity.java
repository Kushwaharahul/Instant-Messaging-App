package com.example.mychatapp;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import Classes.User;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin, btnRegister;
    EditText etPhone, etPwd;
    boolean taskResult=false;
    User user;
    private AbstractXMPPConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setReferences();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUser();
                login();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    private void setUser()
    {
        user = new User();
        user.setPhone(etPhone.getText().toString());
        user.setPassword(etPwd.getText().toString());
    }

    public void setReferences() {
        etPhone = (EditText) findViewById(R.id.etPhone);
        etPwd = (EditText) findViewById(R.id.etPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void login() {

        try {
            LoginCheck obj = new LoginCheck();
            obj.execute("");

        } catch (Exception e) {
            Log.e("", "login error", e);
        }
    }

    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private class LoginCheck extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                InetAddress addr = InetAddress.getByName(getString(R.string.ipAddress));

                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                };
                DomainBareJid serviceName = JidCreate.domainBareFrom("host.docker.internal");

                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(user.getPhone(), user.getPassword())
                        .setPort(5222)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(serviceName)
                        .setHostnameVerifier(verifier)
                        .setHostAddress(addr)
                        .build();

                mConnection = new XMPPTCPConnection(config);
                mConnection.connect();
                mConnection.login();
                if (mConnection.isConnected() && mConnection.isAuthenticated()) {
                        Log.i("", "connection established");
                        taskResult= true;
                }
                mConnection.disconnect();
            } catch (Exception e) {
                taskResult=false;
                Log.e("Exception", e.toString());
            }
            return "";

        }
        @Override
        protected void onPostExecute(String str) {
            if(taskResult)
            {

                    //move to contacts activity
                    Intent intent = new Intent(LoginActivity.this, SelecttContactActivity.class);
                    intent.putExtra("user",user);
                    startActivity(intent);
                }
            else {
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();
            }
            }
        }



}