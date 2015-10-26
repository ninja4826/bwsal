package me.ninja4826.bwsal;

import bwapi.Mirror;

public class GameHandler {
	
	private static Mirror mirror;

	public static Mirror getMirror() {
		return mirror;
	}

	public static void setMirror(Mirror mirror) {
		GameHandler.mirror = mirror;
	}

}
