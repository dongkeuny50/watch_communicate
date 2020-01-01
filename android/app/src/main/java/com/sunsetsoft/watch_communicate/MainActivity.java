package com.sunsetsoft.watch_communicate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.samsung.android.sdk.accessory.SAAgentV2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "connection";
  private static final String TAG = "HelloMessage(C)";

  @SuppressLint("StaticFieldLeak")
  private static MessageAdapter mMessageAdapter;
  private static boolean sendButtonClicked;
  private ConsumerService mConsumerService = null;
  private SAAgentV2.RequestAgentCallback mAgentCallback = new SAAgentV2.RequestAgentCallback() {
    @Override
    public void onAgentAvailable(SAAgentV2 agent) {
      mConsumerService = (ConsumerService) agent;
    }

    @Override
    public void onError(int errorCode, String message) {
      Log.e(TAG, "Agent initialization error: " + errorCode + ". ErrorMsg: " + message);
    }
  };

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {

    mMessageAdapter = new MessageAdapter();
    sendButtonClicked = false;

    SAAgentV2.requestAgent(getApplicationContext(), ConsumerService.class.getName(), mAgentCallback);
    // request agent instance
    GeneratedPluginRegistrant.registerWith(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler((call, result) -> {

                      // Note: this method is invoked on the main thread.
                      if (call.method.equals("connect")) {
                        mOnClick("connect");
                        result.success(receiver());
                      } else if(call.method.equals("send")) {
                        mOnClick("send");
                        result.success(receiver());
                      }else{
                        result.notImplemented();
                      }
                    }
            );


  }

  @Override
  protected void onDestroy() {
    // Clean up connections
    if (mConsumerService != null) {
      updateTextView("Disconnected");
      mMessageAdapter.clear();
      mConsumerService.clearToast();
      mConsumerService.releaseAgent();
      mConsumerService = null;
    }
    sendButtonClicked = false;
    super.onDestroy();
  }

  private String receiver(){
    String isSuccess = "connection or send data succeed";
    return isSuccess;
  }
  public void mOnClick(String s) {
    switch (s) {
      case "connect": {
        mConsumerService.findPeers();

        break;
      }
      case "send": {
        mConsumerService.sendData("Hello Message!");


        break;
      }
      default:
    }
  }

  public static void addMessage(String data) {
    mMessageAdapter.addMessage(new Message(data));
  }

  public static void updateTextView(final String str) {
  }

  public static void updateButtonState(boolean enable) {
    sendButtonClicked = enable;
  }

  private class MessageAdapter extends BaseAdapter {
    private static final int MAX_MESSAGES_TO_DISPLAY = 20;
    private List<Message> mMessages;

    MessageAdapter() {
      mMessages = Collections.synchronizedList(new ArrayList<Message>());
    }

    void addMessage(final Message msg) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
            mMessages.remove(0);
            mMessages.add(msg);
          } else {
            mMessages.add(msg);
          }
          notifyDataSetChanged();
        }
      });
    }

    void clear() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mMessages.clear();
          notifyDataSetChanged();
        }
      });
    }

    @Override
    public int getCount() {
      return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
      return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View messageRecordView = null;
      if (inflator != null) {
        Message message = (Message) getItem(position);
        //tvData.setText(message.data);
      }
      return messageRecordView;
    }
  }

  private static final class Message {
    String data;

    Message(String data) {
      super();
      this.data = data;
    }
  }

}
