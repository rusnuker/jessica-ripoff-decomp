/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.state.transition;

import java.util.ArrayList;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.Transition;

public class CombinedTransition
implements Transition {
    private ArrayList transitions = new ArrayList();

    public void addTransition(Transition t) {
        this.transitions.add(t);
    }

    @Override
    public boolean isComplete() {
        int i = 0;
        while (i < this.transitions.size()) {
            if (!((Transition)this.transitions.get(i)).isComplete()) {
                return false;
            }
            ++i;
        }
        return true;
    }

    @Override
    public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
        int i = this.transitions.size() - 1;
        while (i >= 0) {
            ((Transition)this.transitions.get(i)).postRender(game, container, g);
            --i;
        }
    }

    @Override
    public void preRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
        int i = 0;
        while (i < this.transitions.size()) {
            ((Transition)this.transitions.get(i)).postRender(game, container, g);
            ++i;
        }
    }

    @Override
    public void update(StateBasedGame game, GameContainer container, int delta) throws SlickException {
        int i = 0;
        while (i < this.transitions.size()) {
            Transition t = (Transition)this.transitions.get(i);
            if (!t.isComplete()) {
                t.update(game, container, delta);
            }
            ++i;
        }
    }

    @Override
    public void init(GameState firstState, GameState secondState) {
        int i = this.transitions.size() - 1;
        while (i >= 0) {
            ((Transition)this.transitions.get(i)).init(firstState, secondState);
            --i;
        }
    }
}

