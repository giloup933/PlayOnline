'use strict';

var usernamePage = document.querySelector('#username-page');
var usernameForm = document.querySelector('#usernameForm');
//works?
//and now?
var chatWindow = document.querySelector('#chat-window');
var game = document.querySelector('#game');
var board = document.querySelector('#board');
var moveForm = document.querySelector('#moveForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('#connecting');

var stompClient = null;
var username = null;

var currentPos = null;
var initPos = null;

function connect(event) {
	username = document.querySelector('#name').value.trim();

	if (username) {
		usernamePage.classList.add('hidden');
		connecting.classList.remove('hidden');

		var socket = new SockJS('/ws');
		stompClient = Stomp.over(socket);

		stompClient.connect({}, onConnected, onError);
	}
	event.preventDefault();
}

function onConnected() {
	connecting.classList.add('hidden');
	chatWindow.classList.remove('hidden');
	game.classList.remove('hidden');
	board.classList.remove('hidden');
	stompClient.subscribe('/topic/public', onMessageReceived);
	stompClient.send("/app/chat.addUser", 
		{},
		JSON.stringify({sender: username, type: "JOIN"})
	);

	connectingElement.classList.add('hidden');
}

function onError(error) {
	connectingElement.textContent = 'Could not connect to WebSocket';
	connectingElement.style.color = 'red';
}

function playMove(event) {
	var pos = position.value.trim();
	var dest = destination.value.trim();
	if (pos && dest && stompClient) {
		var moveMessage = {
			sender: username,
			content: pos+"-"+dest,
			type: 'PLAY'
		};
		stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(moveMessage));
		position.value='';
		destination.value='';
	}
	event.preventDefault();
}

function sendMessage(event) {
	var messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		var chatMessage = {
			sender: username,
			content: messageInput.value,
			type: 'CHAT'
		};
		stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
		messageInput.value = '';
	}
	event.preventDefault();
}

function onMessageReceived(payload) {
	var message = JSON.parse(payload.body);
	var messageElement = document.createElement('li');

	if (message.type === 'JOIN') {
		messageElement.classList.add('event-message');
		message.content = message.sender + ' joined!';
	} else if (message.type === 'LEAVE') {
		messageElement.classList.add('event-message');
		message.content = message.sender + ' left!';
	} else if (message.type === 'PLAY') {
		messageElement.classList.add('event-message');
		message.content = message.sender + ' played '+message.content;
	} else {
		messageElement.classList.add('chat-message');
	}
	var textElement = document.createElement('p');
	var messageText = document.createTextNode(message.content);
	textElement.appendChild(messageText);
	messageElement.appendChild(textElement);
	messageArea.appendChild(messageElement);
	messageArea.scrollTop = messageArea.scrollHeight;
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
moveForm.addEventListener('submit', playMove, true);
//event listeners for draw and resignation
//add listener to server notification about end of game
//and then an option to offer rematch or get new game

var pieceImages = {
	'K': 'kingw.svg', 
	'k': 'kingb.svg', 
	'Q': 'queenw.svg', 
	'q': 'queenb.svg', 
	'R': 'rookw.svg', 
	'r': 'rookb.svg', 
	'B': 'bishopw.svg', 
	'b': 'bishopb.svg', 
	'N': 'knightw.svg', 
	'n': 'knightb.svg', 
	'P': 'pawnw.svg',
	'p': 'pawnb.svg'
};

var initPos = {
	'a2': 'P',
	'b2': 'P',
	'c2': 'P',
	'd2': 'P',
	'e2': 'P',
	'f2': 'P',
	'g2': 'P',
	'h2': 'P',
	'b1': 'N',
	'g1': 'N',
	'c1': 'B',
	'f1': 'B',
	'a1': 'R',
	'h1': 'R',
	'd1': 'Q',
	'e1': 'K',
	'a7': 'p',
	'b7': 'p',
	'c7': 'p',
	'd7': 'p',
	'e7': 'p',
	'f7': 'p',
	'g7': 'p',
	'h7': 'p',
	'b8': 'n',
	'g8': 'n',
	'c8': 'b',
	'f8': 'b',
	'a8': 'r',
	'h8': 'r',
	'd8': 'q',
	'e8': 'k'
};

function getImage(piece) {
	console.log(pieceImages[piece])
	return pieceImages[piece];
}

function drawPiece(piece, file, rank) {
	var canvas = document.getElementById("boardCanvas");
	var ctx = canvas.getContext("2d");
	ctx.moveTo(0, 0); // will be based on the coordinates!!
	var img = new Image();
	img.src = "../images/"+getImage(piece);
	ctx.drawImage(img, 50, 50); // size will be based on scale
}

function drawBoard() {
	var canvas = document.getElementById("boardCanvas");
	var ctx = canvas.getContext("2d");
	ctx.moveTo(0, 0);
	var img = new Image();
	img.src = "../images/chessboard.png";
	ctx.drawImage(img, 400, 400);
}

function drawPosition(position) {
	drawBoard();
	for (var key in position) {
		if (position.hasOwnProperty(key))
		{
			drawPiece(position[key], key.charAt(0).concat(key.charAt(1)))
		}
	}
}

drawPosition(initPos);