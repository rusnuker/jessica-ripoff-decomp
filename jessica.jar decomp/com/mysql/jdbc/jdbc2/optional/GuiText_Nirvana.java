/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import java.awt.TextArea;
import javax.swing.GroupLayout;
import javax.swing.JFrame;

public class GuiText_Nirvana
extends JFrame {
    public TextArea textArea1;

    public GuiText_Nirvana() {
        this.initComponents();
    }

    private void initComponents() {
        this.textArea1 = new TextArea();
        this.setTitle("Result");
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.textArea1, -1, 619, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.textArea1, -1, 330, Short.MAX_VALUE).addContainerGap()));
        this.pack();
    }
}

