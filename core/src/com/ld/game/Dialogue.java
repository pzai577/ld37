package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

public class Dialogue {
	
	public String[] text;
	public int[] speakerList;
	public String speaker0; // right now each speaker can be one of three things: "startSage", "endSage", "player"
	public String speaker1;
	public int currentSentence;
	public boolean active;
	
	public Dialogue(String[] text, int[] speakerList, String speaker0, String speaker1) {
	    assert text.length==speakerList.length;
		this.text = text;
		this.speakerList = speakerList;
		this.speaker0 = speaker0;
		this.speaker1 = speaker1;
		currentSentence = -1;
		active = false;
	}
	
	public void activate() {
	    active = true;
	    currentSentence = 0;
	}
	
	public void advance() {
	    currentSentence++;
	    if (currentSentence==speakerList.length) {
	        active = false;
	    }
	}
	
}
