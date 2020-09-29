package com.pa.paperless.data.constant;

/**
 *
 * @author xlk
 * @date 2018/1/24
 */

public class EventMessage {
    Object[] objects;
    int action;
    Object object;

    public EventMessage(int action, Object... values) {
        this.action = action;
        this.objects = values;
    }

    public EventMessage(int action, Object object) {
        this.action = action;
        this.object = object;
    }

    public EventMessage(int action) {
        this.action = action;
    }

    public Object[] getObjects() {
        return objects;
    }

    public int getAction() {
        return action;
    }

    public Object getObject() {
        return object;
    }

}
