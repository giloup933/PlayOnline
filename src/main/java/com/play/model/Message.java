package com.play.model;

public class Message {
	private MessageType type;
	private String sender;
	private String content;
	
	public enum MessageType {
		RESET,
		PLAY,
		DRAW,
		RESIGN,
		CHAT,
		JOIN,
		LEAVE,
		UPDATE,
		FOLLOW
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
