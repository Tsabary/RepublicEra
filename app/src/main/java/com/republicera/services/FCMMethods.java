package com.republicera.services;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public interface FCMMethods {


    static void sendMessageTopic(String receiverId, String initiatorId, String post, Activity activity, String message, String title) {

        if (!receiverId.equals(initiatorId)){
            JSONObject notification = new JSONObject();
            JSONObject notificationBody = new JSONObject();
            try {
                notificationBody.put("title", title);
                notificationBody.put("message", message);
                notificationBody.put("initiator", initiatorId);
                notificationBody.put("post", post);

                notification.put("to", "/topics/" + receiverId);
                notification.put("data", notificationBody);
            } catch (
                    JSONException e) {
                Log.e("notificationStuff", "onCreate: " + e.getMessage());
            }

            sendNotification(notification, activity);
        }
    }

    static void sendNotification(JSONObject notification, Activity activity) {

        String FCM_API = "https://fcm.googleapis.com/fcm/send";
        String serverKey =
                "AAAAA6gibkM:APA91bG8UUtfNFwNLI6-Peu_KsbpTskmjutdJDyHq-qi5fj2UdCcjIVRCO5PlhZUNfJdeyW4-3oznOxMDWdjpfSAnpltlvtBFCoM_vir7pQLKbxc_aDzWJPs8xu27CADbMkHkq5tKgT7";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("notificationStuff", "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity, "Request error", Toast.LENGTH_LONG).show();
                        Log.i("notificationStuff", "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + serverKey);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        MySingleton.getInstance(activity.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

}
