package com.ld.game;

import com.badlogic.gdx.utils.Array;

public class Globals {
    public static String SAGE_TEXT = "Thanks for agreeing to deliver this sword to my brother.\n"
            + "It should be pretty easy, since he just lives at the other end of this room. \n"
            + "I placed some signs along the way to help you. Good luck!";
    
    private static String[] dialogue0Text = {"Hello there! Are you the United Parcel Samurai that I called? \n (press Z to continue)",
                                             "Indeed. United Parcel Samurai is one of the most admired companies in the world. We endeavor for perfect efficiency and the fastest speeds-",
                                             "Okay, okay, I get it. I have this samurai sword that I need delivered to my twin sister. Can you do that?",
                                             "Yes, I'd be happy to. Where does she live?",
                                             "She lives on the other side of this room.",
                                             "...you need me to cross a room to deliver your sword? That sounds fishy.",
                                             "Also, what's with those spikes in the upper-right corner?",
                                             "Huh, I don't see any spikes, I think you're just imagining things. This job is really easy, I just don't want to do it because, um...",
                                             "Because I'm old and my legs don't work! Yeah, that's it!",
                                             "...",
                                             "Fine, here's a beginning tip: press left and right to move.",
                                             "Also, I think there's a tunnel somewhere that'll let you skip part of the level, so look out for that. Good luck!"};
    private static int[] dialogue0SpeakerList = {0,1,0,1,0,1,1,0,0,1,0,0};
    
    private static String[] dialogue1Text = {"Here's your brother's sword.",
                                             "Wow, thank you so much! My brother borrowed that from me a year ago and never returned it.",
                                             "Oh! That reminds me. I actually borrowed my brother's laser gun a year ago and haven't given it back. Silly me!",
                                             "are you serious",
                                             "If you could deliver this back to him, I would be eternally grateful. And I'll pay you double.",
                                             "Hmph, fine, I'll do it. But I really don't appreciate running back and forth like I'm in a video game or something!",
                                             "Anyways, here's some advice: laser beams travel forever until they hit a wall, a checkpoint, or a black orb.",
                                             "As usual, you get your double jump restored whenever a black orb is destroyed.",
                                             "Wait a minute! There's no way I can go back over that bridge area! I refuse to-",
                                             "Have fun!"};
            
    private static int[] dialogue1SpeakerList = {1,0,0,1,0,1,0,0,1,0};
    
    private static String[] dialogue2Text = {"Wow, I can't believe you actually made it back with the laser gun.",
                                             "Thanks, I guess.",
                                             "...",
                                             "Wait a minute. How did you know I had to return with the gun?",
                                             "You beat the game!! Congratulations!!!"};
    private static int[] dialogue2SpeakerList = {0,1,1,1,0};

    public static Array<Dialogue> makeDialogue() {
        Array<Dialogue> dialogues = new Array<Dialogue>();
        dialogues.add(new Dialogue(dialogue0Text, dialogue0SpeakerList, "startSage", "player"));
        dialogues.add(new Dialogue(dialogue1Text, dialogue1SpeakerList, "endSage", "player"));
        dialogues.add(new Dialogue(dialogue2Text, dialogue2SpeakerList, "startSage", "player"));
        return dialogues;
    }
}
