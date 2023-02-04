package com.example.mychatapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mychatapp.databinding.ActivityContactsBinding;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import Classes.User;

public class AddContactsActivity extends AppCompatActivity {

    private User user, contactUser;
    private EditText etFirstName, etLastName, etPhone;
    private Button btnSave;
    private AbstractXMPPConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setReferences();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUser();
                ContactCreation contactCreationTask = new ContactCreation();
                contactCreationTask.execute("");
            }
        });


    }

    private void setReferences() {
        user = (User) getIntent().getSerializableExtra("user");
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        btnSave = (Button) findViewById(R.id.btnSave);
    }

    private void setUser() {
        contactUser = new User();
        contactUser.setFirstName(etFirstName.getText().toString());
        contactUser.setLastName(etLastName.getText().toString());
        contactUser.setPhone(etPhone.getText().toString());

    }

    private class ContactCreation extends AsyncTask<String, String, String> {

        boolean taskResult = false;

        @Override
        protected String doInBackground(String... params) {

            InetAddress addr = null;
            String phone = user.getPhone();
            String pwd = user.getPassword();
            String jid = contactUser.getPhone() + "@host.docker.internal";
            BareJid bjid = null;
            String fname = contactUser.getFirstName();
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
            try {
                bjid = JidCreate.bareFrom(jid);
            } catch (XmppStringprepException e) {
                Log.e("", e.toString());
            }
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(phone, pwd)
                    .setPort(5222)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setXmppDomain(serviceName)
                    .setHostnameVerifier(verifier)
                    .setHostAddress(addr)
                    .build();
            mConnection = new XMPPTCPConnection(config);
            try {
                mConnection.connect();
                mConnection.login();

                if (mConnection.isAuthenticated() && mConnection.isConnected()) {

                    Roster r = Roster.getInstanceFor(mConnection);
                    if (!r.isLoaded())
                        try {
                            r.reloadAndWait();
                        } catch (SmackException.NotLoggedInException e) {
                            Log.i("", "NotLoggedInException");

                        } catch (SmackException.NotConnectedException e) {
                            Log.e("", "NotConnectedException");

                        }
                    r.createEntry(bjid, fname, null);
                    r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

                    for(RosterEntry entry:r.getEntries()){
                        System.out.println(entry.getName());
                    }
                    taskResult = true;
                }
                mConnection.disconnect();
            } catch (Exception e) {
                taskResult = false;
                Log.w("app", e.toString());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String str) {
            if (taskResult) {
                Toast.makeText(getApplicationContext(),"Contact added!",Toast.LENGTH_LONG).show();
            }
        }
    }

}








