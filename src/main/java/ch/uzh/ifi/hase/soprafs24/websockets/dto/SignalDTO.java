package ch.uzh.ifi.hase.soprafs24.websockets.dto;


import ch.uzh.ifi.hase.soprafs24.constant.SignalType;

public class SignalDTO {
    private String data, senderId, recipentId;
    private SignalType type;

    public SignalType getType() {
        return type;
    }

    public void setType(SignalType type) {
        this.type = type;
    }

    public String getRecipentId() {
        return recipentId;
    }

    public void setRecipentId(String recipentId) {
        this.recipentId = recipentId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
