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

var canvas = document.getElementById("boardCanvas");
var ctx = canvas.getContext("2d");
var sqSize = 50;
var topX = 50;
var topY = 50;

var origin = null;

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

currentPos = initPos;

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
	let pos = position.value.trim();
	let dest = destination.value.trim();
	if (pos && dest && stompClient) {
		let moveMessage = {
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
	let messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		let chatMessage = {
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
	let message = JSON.parse(payload.body);
	let messageElement = document.createElement('li');

	if (message.type === 'JOIN') {
		messageElement.classList.add('event-message');
		message.content = message.sender + ' joined!';
	} else if (message.type === 'LEAVE') {
		messageElement.classList.add('event-message');
		message.content = message.sender + ' left!';
	} else if (message.type === 'PLAY') {
		messageElement.classList.add('event-message');
		updateMove(message.content);
		message.content = message.sender + ' played '+message.content;
	} else {
		messageElement.classList.add('chat-message');
	}
	let textElement = document.createElement('p');
	let messageText = document.createTextNode(message.content);
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

var pieceImageNames = {
	'K': 'white_king.svg', 
	'k': 'black_king.svg', 
	'Q': 'white_queen.svg', 
	'q': 'black_queen.svg', 
	'R': 'white_rook.svg', 
	'r': 'black_rook.svg', 
	'B': 'white_bishop.svg', 
	'b': 'black_bishop.svg', 
	'N': 'white_knight.svg', 
	'n': 'black_knight.svg', 
	'P': 'white_pawn.svg',
	'p': 'black_pawn.svg'
};

var pieceImages = {};
for (let p in pieceImages) {
	if (position.hasOwnProperty(key))
	{
		pieceImages[p] = new Image();
		pieceImages[p].src = "../images/"+pieceImageNames[p];
		//drawPiece(position[key], key.charAt(0), key.charAt(1))
	}
}


function getImage(piece) {
	return pieceImages[piece];
}

function drawPiece(piece, file, rank) {
	//console.log("drawing "+piece+" in "+file+""+rank+"!");
	//ctx.moveTo(0, 0); // will be based on the coordinates!!
	ctx.moveTo(50+30*(file.charCodeAt(0)-97), 50+30*(8-rank));
	let img = new Image();
	img.src = "../images/"+getImage(piece);
	ctx.drawImage(img, 30, 30); // size will be based on scale
	ctx.restore();
}

function drawChessBoard(position) {
	let i=1;
	let j=1;
	function draw(i, j) {
		ctx.fillStyle = ((i+j)%2==1) ? "#F5F5F5" : "#282828";
		let xOffset = topX + (i-1)*sqSize;
		let yOffset = topY + (8-j)*sqSize;
		ctx.fillRect(xOffset, yOffset, sqSize, sqSize);
		let key = String.fromCharCode(i+96)+""+j;
		if (position.hasOwnProperty(key)) {
			let imgName = pieceImageNames[position[key]];
			let img = new Image();
			img.src = "../images/"+imgName;
			img.onload = function() {
				//console.log(i+""+j);
				ctx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
				if (j>=8)
				{
					if (i>=8) {
						return;
					}
					else {
						draw(i+1, 1);
					}
				}
				else {
					draw(i, j+1);
				}
			};
		}
		else {
			if (j>=8) {
				if (i>=8) {
					return;
				}
				else {
					draw(i+1, 1);
				}
			}
			else {
				draw(i, j+1);
			}
		}
	};
	draw(1, 1);

	/*for (let i=1;i<=8;i++) {
		for (let j=1;j<=8;j++) {
			cntx.fillStyle = ((i+j)%2==0) ? "#F5F5F5" : "#282828";
			let xOffset = topX + (i-1)*sqSize;
			let yOffset = topY + (8-j)*sqSize;
			cntx.fillRect(xOffset, yOffset, sqSize, sqSize);
			//now draw piece
			var key = String.fromCharCode(i+96)+""+j;
			//console.log(key);
			if (position.hasOwnProperty(key)) {
				var imgName = pieceImageNames[position[key]];
				var img = new Image();
				img.src = "../images/"+imgName;

				/*img.onload = function()

				function caller(other) {
					other(xOffset, yOffset, img);
				}
				caller(function(xOffset, yOffset, img) {
					cntx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
					console.log(position[key]+""+key);
				});*/
				/*img.onload = function(xOffset, yOffset, img) {
					//let the image load
					cntx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
					console.log(position[key]+""+key);
				}*/
				/*var img = pieceImages[key];
				img.onload = function() {
					cntx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
				};*/
				//console.log(position[key]+""+key);
				//var img = new Image();
				//console.log(key);
				//console.log(position[key]);
				//console.log('\n');
				//img.src = "../images/"+getImage(position[key]);
				/*img.onload = function() {
					//let the image load
					cntx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
				};*/
				//img.src = "../images/"+getImage(position[key]);
				//console.log(img.src);
				//cntx.drawImage(img, xOffset, yOffset, sqSize, sqSize);
				//drawPiece(position[key], key.charAt(0), key.charAt(1));
			//}
		//}
	//}*/
	ctx.strokeStyle = "black";
	ctx.strokeRect(topX, topY, sqSize*8, sqSize*8);
}

function drawBoard() {
	ctx.save();
	ctx.moveTo(0, 0);
	let img = new Image();
	img.src = "../images/chessboard.png";
	ctx.drawImage(img, 400, 400);
	//ctx.restore();
}

function drawPosition(position) {
	//drawBoard();
	drawChessBoard(position);
	/*for (var key in position) {
		if (position.hasOwnProperty(key))
		{
			drawPiece(position[key], key.charAt(0), key.charAt(1))
		}
	}*/
}

function updateMove(move) {
	let [pos, dest] = move.split('-');
	if (currentPos.hasOwnProperty(pos)) {
		currentPos[dest] = currentPos[pos];
		delete currentPos[pos];
	}
	drawPosition(currentPos);
}

function getSquare(x, y) {
	//let xOffset = topX + (i-1)*sqSize;
	//let yOffset = topY + (8-j)*sqSize;
	let file = Math.ceil((x-topX) / sqSize);
	let rank = Math.ceil(8 - (y-topY) / sqSize);
	if (file<1 || file>8 || rank<1 || rank>8) {
		return '';
	}
	let key = String.fromCharCode(file+96)+""+rank;
	console.log(key);
	return key;
}

//grab piece
canvas.addEventListener('mousedown', e => {
  let x = e.offsetX;
  let y = e.offsetY;
  origin = getSquare(x, y);
});

//leave piece, if one was grabbed
canvas.addEventListener('mouseup', e => {
  let x = e.offsetX;
  let y = e.offsetY;
  let dest = getSquare(x, y);
  if (origin!='' || dest!='') {
  	if (stompClient) {
  		let moveMessage = {
  			sender: username,
  			content: origin+"-"+dest,
  			type: 'PLAY'
  		};
  		stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(moveMessage));
  		dest = '';
  	}
  }
  origin = '';
});