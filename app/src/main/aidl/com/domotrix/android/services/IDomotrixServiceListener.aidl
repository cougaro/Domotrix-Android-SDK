package com.domotrix.android.services;

interface IDomotrixServiceListener {

    void onMessage(String wampEvent, String jsonMessage);
    void onFault(String description);

}
