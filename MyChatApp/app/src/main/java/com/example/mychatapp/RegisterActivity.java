package com.example.mychatapp;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import Classes.User;

public class RegisterActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etPhone, etPwd;
    Button btnSave;
    User user;
    private AbstractXMPPConnection mConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setReferences();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUser();
                UserCreation obj = new UserCreation();
                obj.execute();
            }
        });
    }
    private void setUser() {
        user = new User();
        user.setFirstName(etFirstName.getText().toString());
        user.setLastName(etLastName.getText().toString());
        user.setPhone(etPhone.getText().toString());
        user.setPassword(etPwd.getText().toString());

    }
    private void setReferences() {
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etPwd = (EditText) findViewById(R.id.etPwd);
        btnSave = (Button) findViewById(R.id.btnSave);
    }
    public void SaveData() {


        try {
           /* boolean result = dbInstance.insertData(user);
            if (result) {
                Toast.makeText(this, "saved", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "user not saved", Toast.LENGTH_LONG).show();
            }*/

        } catch (Exception e) {
            Log.e("TAG", e.getMessage());

        }


    }
    private class UserCreation extends AsyncTask<String, String, String> {

        boolean taskResult = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                InetAddress addr = null;
                try {
                    addr = InetAddress.getByName(getString(R.string.ipAddress));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                };
                DomainBareJid serviceName = null;
                try {
                    serviceName = JidCreate.domainBareFrom("host.docker.internal");
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setPort(5222)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(serviceName)
                        .setHostnameVerifier(verifier)
                        .setHostAddress(addr)
                        .setDebuggerEnabled(true)
                        .build();

                mConnection = new XMPPTCPConnection(config);
                mConnection.connect();
                if (mConnection.isConnected()) {
                    Log.w("app", "connection done");
                    taskResult = true;

                    AccountManager accountManager = AccountManager.getInstance(mConnection);
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(Localpart.from(user.getPhone()), user.getPassword());
                }
                mConnection.disconnect();
            } catch (Exception e) {
                Log.w("app", e.toString());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String str) {

            if (taskResult) {
                SaveData();
            }

        }
    }
}

