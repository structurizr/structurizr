package com.structurizr.server.web;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public final class Messages implements Serializable {

    private final Stack<Message> messages = new Stack<>();

    public void addSuccessMessage(String message) {
        this.messages.push(new Message(MessageType.success, message));
    }

    public void addWarningMessage(String message) {
        this.messages.push(new Message(MessageType.warning, message));
    }

    public void addErrorMessage(String message) {
        this.messages.push(new Message(MessageType.danger, message));
    }

    public List<Message> getUnreadMessages() {
        List<Message> unreadMessages = new LinkedList<>();
        while (!this.messages.isEmpty()) {
            unreadMessages.add(this.messages.pop());
        }

        return unreadMessages;
    }

}
