package com.example.mychatapp;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychatapp.databinding.ActivityChatBinding;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Formatter;

import Classes.Adapter;
import Classes.MessagesData;
import Classes.User;

import android.net.wifi.WifiManager;
import android.content.Context;
import android.widget.Toast;

import java.net.InetAddress;
import java.nio.ByteOrder;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private RecyclerView.AdapterDataObserver adapterObserver;
    private ArrayList<MessagesData> mMessagesData = new ArrayList<>();
    private AbstractXMPPConnection mConnection;
    private EditText sendMessageEt;
    private Button sendBtn;
    User user, contactUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setReferences();
        mAdapter = new Adapter(mMessagesData);
        adapterObserver=new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.i("AdapterObserver: ","******************** OBSERVER DETECTED STATE CHANGE IN ADAPTER *********************");
            }
        };
        mAdapter.registerAdapterDataObserver(adapterObserver);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        DividerItemDecoration decoration = new DividerItemDecoration(this,manager.getOrientation());
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        setConnection();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageSend = sendMessageEt.getText().toString();
                sendMessageEt.setText("");
                if(messageSend.length() > 0 ){
                    sendMessage(messageSend,contactUser.getPhone()+"@host.docker.internal");
                }
            }
        });

    }
    private void setReferences() {
        mRecyclerView=(RecyclerView) findViewById(R.id.recView);
        sendMessageEt=(EditText) findViewById(R.id.etMsg);
        sendBtn=(Button) findViewById(R.id.btnSend);
        user=(User)getIntent().getSerializableExtra("user");
        contactUser=(User)getIntent().getSerializableExtra("contactUser");
    }
    private void sendMessage(String messageSend, String entityBareId) {

        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(entityBareId);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        if(mConnection != null) {

            ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
            Chat chat = chatManager.chatWith(jid);
            Message newMessage = new Message();
            newMessage.setBody(messageSend);

            try {
                chat.send(newMessage);
                MessagesData data = new MessagesData("You:",messageSend);
                mMessagesData.add(data);
                mAdapter.notifyDataSetChanged();
                ChatCreation cc = new ChatCreation(data);
                cc.execute("");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void setConnection(){
                new Thread(){
            @Override
            public void run() {
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
                        .setUsernameAndPassword(user.getPhone(),user.getPassword())
                        .setPort(5222)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(serviceName)
                        .setHostnameVerifier(verifier)
                        .setHostAddress(addr)
                        .setDebuggerEnabled(true)
                        .build();
                mConnection = new XMPPTCPConnection(config);

                try {
                    mConnection.connect();
                    mConnection.login();

                    if(mConnection.isAuthenticated() && mConnection.isConnected()){

                        Log.e("", "run: auth done and connected successfully" );
                        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
                        chatManager.addListener(new IncomingChatMessageListener() {
                            @Override
                            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                                MessagesData data = new MessagesData(from.toString()+":",message.getBody().toString());
                                mMessagesData.add(data);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });

                    }
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } .start();

    }
    @Override
    protected void onStop() {
        super.onStop();
        mConnection.disconnect();

    }
     class ChatCreation extends AsyncTask<String, String, String> {

        MessagesData msg;

         public ChatCreation(MessagesData msgData) {
             super();
             msg=msgData;
         }

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

                if (!mConnection.isConnected() && !mConnection.isAuthenticated()) {
                    mConnection = new XMPPTCPConnection(config);
                    mConnection.connect();
                    mConnection.login();
                    Log.i("", "connection established");
                }

            } catch (Exception e) {

                Log.e("Exception", e.toString());
            }
            return "";

        }


    }

}