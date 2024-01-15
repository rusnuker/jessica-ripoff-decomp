/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GuiOfflineCheckerDB_Nirvana
extends JFrame {
    private JButton jButton1;
    private JCheckBox jCheckBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private JTextPane jTextPane1;
    private JTextPane jTextPane2;
    private TextArea textArea1;

    public GuiOfflineCheckerDB_Nirvana() {
        this.initComponents();
    }

    private void initComponents() {
        this.jTextField1 = new JTextField();
        this.jLabel1 = new JLabel();
        this.jTextField2 = new JTextField();
        this.jLabel2 = new JLabel();
        this.textArea1 = new TextArea();
        this.jButton1 = new JButton();
        this.jLabel3 = new JLabel();
        this.jScrollPane1 = new JScrollPane();
        this.jTextPane1 = new JTextPane();
        this.jTextField3 = new JTextField();
        this.jLabel4 = new JLabel();
        this.jLabel5 = new JLabel();
        this.jCheckBox1 = new JCheckBox();
        this.jLabel6 = new JLabel();
        this.jScrollPane2 = new JScrollPane();
        this.jTextPane2 = new JTextPane();
        this.setTitle("CheckedDB");
        this.jTextField1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiOfflineCheckerDB_Nirvana this$0;
            {
                this.this$0 = guiOfflineCheckerDB_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiOfflineCheckerDB_Nirvana.access$0(this.this$0, evt);
            }
        });
        this.jLabel1.setText("Path to list of databases");
        this.jTextField2.setToolTipText("");
        this.jTextField2.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiOfflineCheckerDB_Nirvana this$0;
            {
                this.this$0 = guiOfflineCheckerDB_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiOfflineCheckerDB_Nirvana.access$1(this.this$0, evt);
            }
        });
        this.jLabel2.setText("What search?");
        this.jButton1.setText("Search");
        this.jButton1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiOfflineCheckerDB_Nirvana this$0;
            {
                this.this$0 = guiOfflineCheckerDB_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiOfflineCheckerDB_Nirvana.access$2(this.this$0, evt);
            }
        });
        this.jLabel3.setText("Current file:");
        this.jScrollPane1.setViewportView(this.jTextPane1);
        this.jTextField3.setToolTipText("");
        this.jTextField3.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiOfflineCheckerDB_Nirvana this$0;
            {
                this.this$0 = guiOfflineCheckerDB_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiOfflineCheckerDB_Nirvana.access$3(this.this$0, evt);
            }
        });
        this.jLabel4.setText("Path to list of words");
        this.jLabel5.setText("for search");
        this.jCheckBox1.setText("Search of list");
        this.jLabel6.setText("Current nick:");
        this.jScrollPane2.setViewportView(this.jTextPane2);
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.textArea1, -1, -1, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(this.jLabel4).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel1).addComponent(this.jTextField1, -2, 142, -2)).addGap(53, 53, 53).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel2).addComponent(this.jTextField2, -2, 132, -2)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(10, 10, 10).addComponent(this.jTextField3, -2, 124, -2)).addGroup(layout.createSequentialGroup().addGap(11, 11, 11).addComponent(this.jLabel5)))).addGroup(layout.createSequentialGroup().addComponent(this.jLabel3).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jScrollPane1, -2, 132, -2).addGap(18, 18, 18).addComponent(this.jLabel6).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jScrollPane2, -2, 132, -2))).addGap(11, 11, 11))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jButton1, -1, -1, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(this.jCheckBox1).addGap(0, 71, Short.MAX_VALUE))))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(15, 15, 15).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel1).addComponent(this.jLabel2).addComponent(this.jLabel5))).addComponent(this.jLabel4)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jTextField1, -2, -1, -2).addComponent(this.jTextField2, -2, -1, -2).addComponent(this.jTextField3, -2, -1, -2))).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(this.jButton1, -2, 49, -2))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jScrollPane1, -2, -1, -2).addComponent(this.jLabel3, -2, 14, -2).addComponent(this.jCheckBox1).addComponent(this.jScrollPane2, -2, -1, -2).addComponent(this.jLabel6, -2, 14, -2)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE).addComponent(this.textArea1, -2, 367, -2)));
        this.pack();
    }

    private void jTextField1ActionPerformed(ActionEvent evt) {
    }

    private void jTextField2ActionPerformed(ActionEvent evt) {
    }

    public void findFile(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            File[] fileArray = list;
            int n = list.length;
            int n2 = 0;
            while (n2 < n) {
                File fil = fileArray[n2];
                if (fil.isDirectory()) {
                    this.findFile(fil);
                } else {
                    try {
                        this.jTextPane1.setText(fil.getName());
                        this.findTextInFile(fil);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.jTextPane1.setText("Finish!");
                    this.jTextPane2.setText("Finish!");
                }
                ++n2;
            }
        }
    }

    void findTextInFile(File file) throws FileNotFoundException, IOException {
        if (this.jCheckBox1.isSelected()) {
            Scanner txtscan = new Scanner(new File(this.jTextField3.getText()));
            while (txtscan.hasNextLine()) {
                String str = txtscan.nextLine();
                this.jTextPane2.setText(str);
                Scanner txtscan2 = new Scanner(file);
                while (txtscan2.hasNextLine()) {
                    String str2 = txtscan2.nextLine();
                    if (!str2.contains(str)) continue;
                    this.textArea1.append(String.valueOf(str) + " ||||||||| " + file.getName() + " ||||||||| " + str2 + "\n");
                }
            }
        } else {
            Scanner txtscan = new Scanner(file);
            String words = this.jTextField2.getText();
            while (txtscan.hasNextLine()) {
                String str = txtscan.nextLine();
                if (!str.contains(words)) continue;
                this.textArea1.append(String.valueOf(this.jTextField2.getText()) + " ||||||||| " + file.getName() + " ||||||||| " + str + "\n");
            }
        }
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        new Thread(this){
            final /* synthetic */ GuiOfflineCheckerDB_Nirvana this$0;
            {
                this.this$0 = guiOfflineCheckerDB_Nirvana;
            }

            public void run() {
                this.this$0.findFile(new File(GuiOfflineCheckerDB_Nirvana.access$4(this.this$0).getText()));
            }
        }.start();
    }

    private void jTextField3ActionPerformed(ActionEvent evt) {
    }

    public static void main(String[] args) {
        try {
            UIManager.LookAndFeelInfo[] lookAndFeelInfoArray = UIManager.getInstalledLookAndFeels();
            int n = lookAndFeelInfoArray.length;
            int n2 = 0;
            while (n2 < n) {
                UIManager.LookAndFeelInfo info = lookAndFeelInfoArray[n2];
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
                ++n2;
            }
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(GuiOfflineCheckerDB_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(GuiOfflineCheckerDB_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(GuiOfflineCheckerDB_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiOfflineCheckerDB_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable(){

            public void run() {
                new GuiOfflineCheckerDB_Nirvana().setVisible(true);
            }
        });
    }

    static /* synthetic */ void access$0(GuiOfflineCheckerDB_Nirvana guiOfflineCheckerDB_Nirvana, ActionEvent actionEvent) {
        guiOfflineCheckerDB_Nirvana.jTextField1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$1(GuiOfflineCheckerDB_Nirvana guiOfflineCheckerDB_Nirvana, ActionEvent actionEvent) {
        guiOfflineCheckerDB_Nirvana.jTextField2ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$2(GuiOfflineCheckerDB_Nirvana guiOfflineCheckerDB_Nirvana, ActionEvent actionEvent) {
        guiOfflineCheckerDB_Nirvana.jButton1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$3(GuiOfflineCheckerDB_Nirvana guiOfflineCheckerDB_Nirvana, ActionEvent actionEvent) {
        guiOfflineCheckerDB_Nirvana.jTextField3ActionPerformed(actionEvent);
    }

    static /* synthetic */ JTextField access$4(GuiOfflineCheckerDB_Nirvana guiOfflineCheckerDB_Nirvana) {
        return guiOfflineCheckerDB_Nirvana.jTextField1;
    }
}

