/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.tests;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.BlobbyTransition;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.state.transition.HorizontalSplitTransition;
import org.newdawn.slick.state.transition.RotateTransition;
import org.newdawn.slick.state.transition.SelectTransition;
import org.newdawn.slick.state.transition.Transition;
import org.newdawn.slick.state.transition.VerticalSplitTransition;
import org.newdawn.slick.util.Log;

public class TransitionTest
extends StateBasedGame {
    private Class[][] transitions;
    private int index;

    public TransitionTest() {
        super("Transition Test - Hit Space To Transition");
        Class[][] classArrayArray = new Class[6][];
        Class[] classArray = new Class[2];
        classArray[1] = VerticalSplitTransition.class;
        classArrayArray[0] = classArray;
        classArrayArray[1] = new Class[]{FadeOutTransition.class, FadeInTransition.class};
        Class[] classArray2 = new Class[2];
        classArray2[1] = RotateTransition.class;
        classArrayArray[2] = classArray2;
        Class[] classArray3 = new Class[2];
        classArray3[1] = HorizontalSplitTransition.class;
        classArrayArray[3] = classArray3;
        Class[] classArray4 = new Class[2];
        classArray4[1] = BlobbyTransition.class;
        classArrayArray[4] = classArray4;
        Class[] classArray5 = new Class[2];
        classArray5[1] = SelectTransition.class;
        classArrayArray[5] = classArray5;
        this.transitions = classArrayArray;
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        this.addState(new ImageState(0, "testdata/wallpaper/paper1.png", 1));
        this.addState(new ImageState(1, "testdata/wallpaper/paper2.png", 2));
        this.addState(new ImageState(2, "testdata/bigimage.tga", 0));
    }

    public Transition[] getNextTransitionPair() {
        Transition[] pair = new Transition[2];
        try {
            if (this.transitions[this.index][0] != null) {
                pair[0] = (Transition)this.transitions[this.index][0].newInstance();
            }
            if (this.transitions[this.index][1] != null) {
                pair[1] = (Transition)this.transitions[this.index][1].newInstance();
            }
        }
        catch (Throwable e) {
            Log.error(e);
        }
        ++this.index;
        if (this.index >= this.transitions.length) {
            this.index = 0;
        }
        return pair;
    }

    public static void main(String[] argv) {
        try {
            AppGameContainer container = new AppGameContainer(new TransitionTest());
            container.setDisplayMode(800, 600, false);
            container.start();
        }
        catch (SlickException e) {
            e.printStackTrace();
        }
    }

    private class ImageState
    extends BasicGameState {
        private int id;
        private int next;
        private String ref;
        private Image image;

        public ImageState(int id, String ref, int next) {
            this.ref = ref;
            this.id = id;
            this.next = next;
        }

        @Override
        public int getID() {
            return this.id;
        }

        @Override
        public void init(GameContainer container, StateBasedGame game) throws SlickException {
            this.image = new Image(this.ref);
        }

        @Override
        public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
            this.image.draw(0.0f, 0.0f, 800.0f, 600.0f);
            g.setColor(Color.red);
            g.fillRect(-50.0f, 200.0f, 50.0f, 50.0f);
        }

        @Override
        public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
            if (container.getInput().isKeyPressed(57)) {
                Transition[] pair = TransitionTest.this.getNextTransitionPair();
                game.enterState(this.next, pair[0], pair[1]);
            }
        }
    }
}

