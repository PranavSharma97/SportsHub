package com.example.admin.loginandregistration.helper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.StringReader;

/**
 * Created by admin on 5/7/2016.
 */
public class CustomPriorityRequest extends StringRequest{

    Priority priority = Priority.HIGH;

    public CustomPriorityRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    public Priority getPriority(){
        return priority;
    }

    public void setPriority(Priority priority){
        this.priority = priority;
    }
}


