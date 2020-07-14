package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {

    public Message(){}

    public Date getTime(){
        return getCreatedAt();
    }

    public String getText(){
        return getString("text");
    }

    public void setText(String text){
        put("text", text);
    }
}
