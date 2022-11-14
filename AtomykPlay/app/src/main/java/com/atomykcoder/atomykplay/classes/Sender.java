package com.atomykcoder.atomykplay.classes;

import com.atomykcoder.atomykplay.interfaces.INotifyChangeLayout;

import java.util.ArrayList;

public class Sender {

    ArrayList<INotifyChangeLayout> listeners = new ArrayList<>();

    public void addListeners(INotifyChangeLayout listener) {
        listeners.add(listener);
    }

    public void execute() {
        for(INotifyChangeLayout listener : listeners)
        {
            listener.execute();
        }
    }

}
