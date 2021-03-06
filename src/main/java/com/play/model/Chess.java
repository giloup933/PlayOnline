package com.play.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

public class Chess extends Game{
	static class Clock implements Runnable{
		private Chess game;
		private boolean gameNotOver;
		private boolean isWhite;
		private long msW;
		private long msB;
		private int increment;
		private long lastPlayedTime;
		private LocalDateTime lastMove;
		Clock(Chess game, boolean isWhite, long seconds, int increment) {
			this.game = game;
			this.gameNotOver = true;
			this.isWhite = isWhite;
			this.msW = seconds*1000;
			this.msB = seconds*1000;
			this.increment = increment;
			this.lastPlayedTime = System.currentTimeMillis();
			this.lastMove = LocalDateTime.now();
		}
		public void playMove()
		{
			System.out.println("in playMove()");
			//long ms = ChronoUnit.MILLIS.between(lastMove, LocalDateTime.now());
			long ms = System.currentTimeMillis()-lastPlayedTime;
			if (isWhite)
			{
				msW -= ms;
				if (msW<=0) {
					System.out.println("Time's up! 0-1");
					gameNotOver = false;
				}
				else
					msW += increment;
			}
			else
			{
				msB -= ms;
				if (msB<=0) {
					System.out.println("Time's up! 1-0");
					gameNotOver = false;
				}
				else
					msB += increment;
			}
			isWhite = isWhite ? false : true;
			this.lastMove = LocalDateTime.now();
			this.lastPlayedTime = System.currentTimeMillis();
			this.notify();
		}
		public void loseOnTime() {
			if (isWhite)
			{
				//System.out.println("Time's up! 0-1");
				this.game.endGame("time-white");
			}
			else
			{
				//System.out.println("Time's up! 1-0");
				this.game.endGame("time-black");
			}
			gameNotOver = false;
		}
		@Override
		public void run() {
			while (gameNotOver)
			{
				long ms = isWhite ? msW : msB;
				try {
					System.out.println("wait "+ms+" milliseconds for "+ (isWhite ? "white" : "black"));
					synchronized(this) {
						this.wait(ms);
						loseOnTime();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public String toString() {
			String s = msW+" - "+msB;
			return s;
		}
	}
	private LinkedList<Piece> white;
	private LinkedList<Piece> black;
	
	
	boolean whitePlays;
	boolean drawOffered;
	boolean[] canCastle;
	
	Clock clock;
	public Chess() {
		white = new LinkedList<>();
		black = new LinkedList<>();
		whitePlays = true;
		drawOffered = false;
		canCastle = new boolean[]{true,true,true,true};
		white.add(new Pawn(true, new Square('a', 2)));
		white.add(new Pawn(true, new Square('b', 2)));
		white.add(new Pawn(true, new Square('c', 2)));
		white.add(new Pawn(true, new Square('d', 2)));
		white.add(new Pawn(true, new Square('e', 2)));
		white.add(new Pawn(true, new Square('f', 2)));
		white.add(new Pawn(true, new Square('g', 2)));
		white.add(new Pawn(true, new Square('h', 2)));
		white.add(new Knight(true, new Square('b', 1)));
		white.add(new Knight(true, new Square('g', 1)));
		white.add(new Bishop(true, new Square('c', 1)));
		white.add(new Bishop(true, new Square('f', 1)));
		white.add(new Rook(true, new Square('a', 1)));
		white.add(new Rook(true, new Square('h', 1)));
		white.add(new Queen(true, new Square('d', 1)));
		white.add(new King(true, new Square('e', 1)));
		
		black.add(new Pawn(false, new Square('a', 7)));
		black.add(new Pawn(false, new Square('b', 7)));
		black.add(new Pawn(false, new Square('c', 7)));
		black.add(new Pawn(false, new Square('d', 7)));
		black.add(new Pawn(false, new Square('e', 7)));
		black.add(new Pawn(false, new Square('f', 7)));
		black.add(new Pawn(false, new Square('g', 7)));
		black.add(new Pawn(false, new Square('h', 7)));
		black.add(new Knight(false, new Square('b', 8)));
		black.add(new Knight(false, new Square('g', 8)));
		black.add(new Bishop(false, new Square('c', 8)));
		black.add(new Bishop(false, new Square('f', 8)));
		black.add(new Rook(false, new Square('a', 8)));
		black.add(new Rook(false, new Square('h', 8)));
		black.add(new Queen(false, new Square('d', 8)));
		black.add(new King(false, new Square('e', 8)));
		this.clock = new Clock(this, true, 3600, 5);
		synchronized(this.clock) {
			new Thread(this.clock).start();
		}
	}
	public class Square {
		private char file;
		private int rank;
		Square(char file, int rank) {
			this.file = file;
			this.rank = rank;
		}
		public int getRank() {
			return rank;
		}
		public char getFile() {
			return file;
		}
		public boolean isRankValid() {
			if (rank<0 || rank>8)
				return false;
			return true;
		}
		public boolean isFileValid() {
			if (file<'a' || file>'h') {
				return false;
			}
			return true;
		}
		@Override
		public boolean equals(Object e) {
			Square s = (Square)e;
			System.out.println(s.toString());
			System.out.println(this.toString());
			System.out.println();
			return this.getFile()==s.getFile() && this.getRank()==s.getRank();
		}
		@Override
		public String toString() {
			return this.getFile()+""+this.getRank();
		}
	}
	abstract class Piece {
		abstract boolean isWhite();
		abstract Square getPosition();
		abstract boolean isMoveLegal(Square dest);
		abstract void move(Square dest);
	}
	class Pawn extends Piece {
		private boolean isWhite;
		private Square pos;
		Pawn(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		@Override
		boolean isWhite() {
			return isWhite;
		}
		
		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			int coeff = -1;
			if (isWhite)
				coeff = 1;
			if (pos.getFile()==dest.getFile())
			{
				if (dest.getRank()-pos.getRank()==1*coeff)
					return true;
				if (dest.getRank()-pos.getRank()==2*coeff && pos.getRank()==(coeff==1 ? 2 : 7))
					return true;
				return false;
			}
			else if (Math.abs(pos.getFile()-dest.getFile())==1)
			{
				System.out.println("adjecent files!");
				if (dest.getRank()-pos.getRank()==1*coeff)
				{
					LinkedList<Piece> opponent;
					if (isWhite)
						opponent = black;
					else
						opponent = white;
					for (Piece p: opponent)
					{
						if (p.getPosition().equals(dest))
						{
							System.out.println("opponent piece found to be taken!");
							return true;
						}
					}
				}
				return false;
			}
			return false;
			//DO NOT FORGET TO IMPLEMENT EN PASSENT
		}
		
		@Override
		void move(Square dest) {
			this.pos = dest;
		}
		
		@Override
		public String toString() {
			return pos.toString();
		}
	}
	class Knight extends Piece {
		private boolean isWhite;
		private Square pos;
		Knight(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		
		@Override
		boolean isWhite() {
			return isWhite;
		}

		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			int rankDiff = Math.abs(dest.getRank()-pos.getRank());
			int fileDiff = Math.abs(dest.getFile()-pos.getFile());
			return (rankDiff==2 && fileDiff==1) || (rankDiff==1 && fileDiff==2);
		}
		
		@Override
		void move(Square dest) {
			this.pos = dest;
		}
		
		@Override
		public String toString() {
			String piece="n";
			if (isWhite())
				piece="N";
			return piece+""+pos.toString();
		}
	}
	class Bishop extends Piece {
		private boolean isWhite;
		private Square pos;
		Bishop(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		
		@Override
		boolean isWhite() {
			return isWhite;
		}

		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			int rankDiff = dest.getRank() - pos.getRank();
			int fileDiff = dest.getFile() - pos.getFile();
			if (rankDiff == fileDiff)
				return true;
			if (rankDiff == -1*fileDiff)
				return true;
			return false;
		}
		
		@Override
		void move(Square dest) {
			this.pos = dest;
		}
		
		@Override
		public String toString() {
			String piece="b";
			if (isWhite())
				piece="B";
			return piece+""+pos.toString();
		}
	}
	class Rook extends Piece {
		private boolean isWhite;
		private Square pos;
		Rook(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		
		@Override
		boolean isWhite() {
			return isWhite;
		}

		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			return (dest.getRank()==pos.getRank() ^ dest.getFile()==pos.getFile());
		}
		
		@Override
		void move(Square dest) {
			this.pos = dest;
			if (isWhite)
			{
				if (getPosition().equals(new Square('h', 1)))
				{
					canCastle[0]=false;
				}
				else if (getPosition().equals(new Square('a', 1)))
				{
					canCastle[1]=false;
				}
			}
			else
			{
				if (getPosition().equals(new Square('h', 8)))
				{
					canCastle[2]=false;
				}
				else if (getPosition().equals(new Square('a', 8)))
				{
					canCastle[3]=false;
				}
			}
		}
		
		@Override
		public String toString() {
			String piece="r";
			if (isWhite())
				piece="R";
			return piece+""+pos.toString();
		}
	}
	class Queen extends Piece {
		private boolean isWhite;
		private Square pos;
		Queen(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		
		@Override
		boolean isWhite() {
			return isWhite;
		}

		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			int rankDiff = dest.getRank() - pos.getRank();
			int fileDiff = dest.getFile() - pos.getFile();
			// Rook pattern
			if (rankDiff==0 ^ fileDiff==0)
				return true;
			// Bishop pattern
			if (rankDiff == fileDiff)
				return true;
			if (rankDiff == -1*fileDiff)
				return true;
			return false;
		}
		
		@Override
		void move(Square dest) {
			this.pos = dest;
		}
		
		@Override
		public String toString() {
			String piece="q";
			if (isWhite())
				piece="Q";
			return piece+""+pos.toString();
		}
	}
	class King extends Piece {
		private boolean isWhite;
		private Square pos;
		King(boolean isWhite, Square pos) {
			this.isWhite = isWhite;
			this.pos = pos;
		}
		
		@Override
		boolean isWhite() {
			return isWhite;
		}

		@Override
		Square getPosition() {
			return pos;
		}

		@Override
		boolean isMoveLegal(Square dest) {
			int rankDiff = Math.abs(dest.getRank() - pos.getRank());
			int fileDiff = Math.abs(dest.getFile() - pos.getFile());
			if (rankDiff<=1 && fileDiff <=1)
				return true;
			//don't forget to implement castle...
			int castleLn=8;
			if (isWhite)
				castleLn=1;
			if (getPosition().equals(new Square('e', castleLn))) {
				if (dest.equals(new Square('g', castleLn)) && canCastle[isWhite ? 0 : 2]) {
					return true;
				}
				else if (dest.equals(new Square('c', castleLn)) && canCastle[isWhite ? 1 : 3]) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		void move(Square dest) {
			int castleLn = 8;
			if (isWhite)
				castleLn = 1;
			if (getPosition().equals(new Square('e', castleLn)))
			{
				//castle, move only the rook as the king will be moved after this if block
				if (dest.equals(new Square('g', castleLn))) {
					for (Piece p: (isWhite ? white : black)) {
						System.out.println(p.getPosition().toString());
						if (p.getPosition().equals(new Square('h', castleLn))) {
							p.move(new Square('f', castleLn));
							break;
						}
						else if (p.getPosition().equals(new Square('a', castleLn))) {
							p.move(new Square('c', castleLn));
							break;
						}
					}
				}
			}
			this.pos = dest;
			if (isWhite) {
				canCastle[0]=false;
				canCastle[1]=false;
			}
			else {
				canCastle[2]=false;
				canCastle[3]=false;
			}
		}
		
		@Override
		public String toString() {
			String piece="k";
			if (isWhite())
				piece="K";
			return piece+""+pos.toString();
		}
	}
	public long getTimeWhite() {
		return clock.msW;
	}
	public long getTimeBlack() {
		return clock.msB;
	}
	boolean moveLegalWithCheck(Square pos, Square dest) {
		//this move checks if the king is checked, and if so, if the move proposed clears it
		return true;
	}
	boolean isMoveLegal(Square pos, Square dest) {
		/*
		 * A move is legal if and only if:
		 * 1) pos and dest both between a1 and h8 and are not the same
		 * 2) The player to play has a piece in pos
		 * 3) The player to play has no piece in dest
		 * 4) The piece can move from pos to dest
		 * 5) There is no check, or if there is the move resolves it
		*/
		System.out.println("Made it to isMoveLegal()");
		if (!(pos.isFileValid() || pos.isRankValid() || dest.isFileValid() || dest.isRankValid()))
		{
			System.out.println("invalid input");
			return false;
		}
		if (pos.equals(dest))
		{
			System.out.println("both are the same square");
			return false;
		}
		LinkedList<Piece> player;
		if (whitePlays)
			player = white;
		else
			player = black;
		for (Piece p: player) {
			if (p.getPosition().equals(pos)) {
				System.out.println("good! a piece is ready to move!");
				for (Piece p1: player) {
					//check to see no piece stands in the destination
					if (!p.equals(p1))
					{
						if (p1.getPosition().equals(dest))
						{
							System.out.println("another piece already there");
							return false;
						}
					}
				}
				//now check if the piece can make this move
				if (!p.isMoveLegal(dest))
				{
					System.out.println("The piece cannot go there");
					return false;
				}
				//now check if there is a check and if so, does this move resolve it
				if (!moveLegalWithCheck(pos, dest))
				{
					System.out.println("check!");
					return false;
				}
				return true;
			}
		}
		System.out.println("no piece exists there!!");
		return false;
	}
	void makeMove(Square pos, Square dest) {
		LinkedList<Piece> player;
		if (whitePlays)
		{
			player=white;
		}
		else
		{
			player=black;
		}
		for (Piece p: player) {
			if (p.getPosition().equals(pos))
			{
				LinkedList<Piece> opponent;
				if (whitePlays)
					opponent = black;
				else
					opponent = white;
				int index=0;
				for (Piece p1: opponent) {
					if (p1.getPosition().equals(dest))
					{
						opponent.remove(index);
						break;
					}
					index++;
				}
				p.move(dest); // the piece itself is moved
				whitePlays = whitePlays ^ true;
			}
		}
		System.out.println("wake the clock up!");
		synchronized(this.clock) {
			this.clock.playMove();
		}
		System.out.println(this.toString());
	}
	boolean hasLegalMove() {
		return true;
	}
	boolean isChecked() {
		return true;
	}
	public boolean playMove(String move) throws Exception {
		if (move.length()==5 && move.charAt(2)=='-')
		{
			System.out.println("Proper input");
			Square pos = new Square(move.charAt(0), Character.getNumericValue(move.charAt(1)));
			Square dest = new Square(move.charAt(3), Character.getNumericValue(move.charAt(4)));
			System.out.print(pos.getFile()+""+pos.getRank());
			System.out.println("-"+dest.getFile()+""+dest.getRank());
			if (!isMoveLegal(pos, dest))
				return false;
			makeMove(pos, dest);
			drawOffered = false;
			return true;
		}
		return false;
	}
	public String checkOutcome() {
		if (hasLegalMove())
			return "OK";
		if (isChecked()) {
			//checkmate
			return "CHECKMATE";
		}
		else {
			//stalemate
			return "STALEMATE";
		}
	}
	public void endGame(String code) {
		if (code.equals("time-white")) {
			System.out.println("White ran out of time! 0-1");
		} else if (code.equals("time-black")) {
			System.out.println("Black ran out of time! 1-0");
		} else if (code.equals("resign-white")) {
			System.out.println("White resigns! 0-1");
		} else if (code.equals("resign-black")) {
			System.out.println("Black resigns! 1-0");
		} else if (code.equals("checkmate-white")) {
			System.out.println("Checkmate! 0-1");
		} else if (code.equals("checkmate-black")) {
			System.out.println("Checkmate! 1-0");
		} else if (code.equals("stalemate")) {
			System.out.println("Draw by stalemate!");
		} else if (code.equals("agreed")) {
			System.out.println("Draw by agreement.");
		}
	}
	public String resign() {
		if (whitePlays)
			return "0-1";
		else
			return "1-0";
	}
	public String offerDraw() {
		drawOffered = true;
		return "OFFER";
	}
	public String acceptDraw() {
		return ".5-.5";
	}
	@Override
	public String toString() {
		String str = "White {";
		for (Piece p: white) {
			str += String.format("%s ", p.toString());
		}
		str+="} \nBlack {";
		for (Piece p: black) {
			str += String.format("%s ", p.toString());
		}
		str+="}\n";
		str+= whitePlays ? "white to play" : "black to play";
		str+= "\n" + clock.toString();
		return str;
	}
	
	public String toJson() {
		String str = "{ \"white\": [";
		for (Piece p: white) {
			str += String.format("\"%s\", ", p.toString());
		}
		str = str.substring(0, str.length()-2) + "], \"black\": [";
		for (Piece p: black) {
			str += String.format("\"%s\", ", p.toString());
		}
		str = str.substring(0, str.length()-2) + "], ";
		str += "\"timeWhite\": "+this.getTimeWhite()+", \"timeBlack\": "+this.getTimeBlack();
		//str += "\"lastPlayed: \""+
		//System.out.println(clock.lastPlayedTime+"!!!");
		str += ", \"lastPlayed\": "+clock.lastPlayedTime+", \"whitePlays\": "+this.whitePlays;
		str += "}";
		return str;
	}
}
