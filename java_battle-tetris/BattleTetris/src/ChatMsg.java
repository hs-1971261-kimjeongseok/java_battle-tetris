//1971261 KimJeongSeok

import Tetris.Board;
import Tetris.Shape;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatMsg implements Serializable{
	String userID;
	int mode;
	int attack;
	String message;
	List<String> players = new ArrayList();
	List<Boolean> playerReady = new ArrayList();
	Shape.Tetrominoe[] board;
	Shape.Tetrominoe[] list;
	
	public static final int MODE_LOGIN = 0x1;
	public static final int MODE_LOGOUT = 0x2;
	public static final int MODE_READY = 0x3;
	public static final int MODE_START = 0x4;
	public static final int MODE_LIST = 0x8;
	public static final int MODE_TX_STRING = 0x16;
	
	//미구현
	public static final int MODE_BOARD_DATA=0x17;
	public static final int MODE_BLOCK_ATTACK=0x18;
	public static final int MODE_PLAYER_OUT=0x19;
	public static final int MODE_GAME_OVER=0x20;
	

	public ChatMsg(String userID, int code, String msg) {
		this.userID=userID;
		this.mode=code;
		message=msg;
	}

	public ChatMsg(String userID, int code) {
		this(userID,code,null);
	}
}
