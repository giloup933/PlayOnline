package com.play.controller;

import org.apache.tomcat.util.net.openssl.ciphers.MessageDigest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.play.model.Chess;
import com.play.model.Game;
import com.play.model.Message;

@Controller
public class GameController {
	Chess game;
	GameController() {
		this.game = new Chess();
	}
	@MessageMapping("/chat.sendMessage")
	@SendTo("/topic/public")
	public Message sendMessage(@Payload Message message) throws Exception {
		Message.MessageType type = message.getType();
		if (type == Message.MessageType.RESIGN) {
			throw new Exception("Not supported yet");
		}
		else if (type == Message.MessageType.DRAW) {
			throw new Exception("Not supported yet");
		}
		else if (type == Message.MessageType.PLAY) {
			//move format: e2-e4
			String move = message.getContent();
			boolean playMove = game.playMove(move);
			if (!playMove)
				throw new Exception("illegal input: "+move);
			return message;
		}
		else if (type == Message.MessageType.JOIN) {
			return message;
		}
		return message;
	}
	
	@MessageMapping("/chat.addUser")
	@SendTo("/topic/public")
	public Message addUser(@Payload Message chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		return chatMessage;
	}
}
