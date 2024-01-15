/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GuiSubdomainBrute_Nirvana
extends JFrame {
    private List<String> subs = new ArrayList<String>();
    private int count = 0;
    private JButton jButton1;
    private JCheckBox jCheckBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private JTextField jTextField4;
    private TextArea textArea1;

    public GuiSubdomainBrute_Nirvana() {
        this.initComponents();
    }

    private void initComponents() {
        this.jTextField1 = new JTextField();
        this.jLabel1 = new JLabel();
        this.textArea1 = new TextArea();
        this.jButton1 = new JButton();
        this.jLabel2 = new JLabel();
        this.jTextField2 = new JTextField();
        this.jCheckBox1 = new JCheckBox();
        this.jTextField3 = new JTextField();
        this.jLabel3 = new JLabel();
        this.jTextField4 = new JTextField();
        this.setTitle("Subdomains Brute");
        this.jTextField1.setToolTipText("");
        this.jLabel1.setText("Path to list with Subdomains");
        this.jButton1.setText("Start");
        this.jButton1.setToolTipText("");
        this.jButton1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiSubdomainBrute_Nirvana this$0;
            {
                this.this$0 = guiSubdomainBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiSubdomainBrute_Nirvana.access$0(this.this$0, evt);
            }
        });
        this.jLabel2.setText("Target");
        this.jTextField2.setToolTipText("");
        this.jTextField2.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiSubdomainBrute_Nirvana this$0;
            {
                this.this$0 = guiSubdomainBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiSubdomainBrute_Nirvana.access$1(this.this$0, evt);
            }
        });
        this.jCheckBox1.setText("HTTP Responce code");
        this.jCheckBox1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiSubdomainBrute_Nirvana this$0;
            {
                this.this$0 = guiSubdomainBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiSubdomainBrute_Nirvana.access$2(this.this$0, evt);
            }
        });
        this.jTextField3.setToolTipText("");
        this.jLabel3.setText("Threads");
        this.jTextField4.setText("0/0");
        this.jTextField4.setToolTipText("");
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jTextField2).addComponent(this.jButton1, -1, -1, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(this.textArea1, -2, 341, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.jCheckBox1, -1, -1, Short.MAX_VALUE).addComponent(this.jTextField3).addComponent(this.jLabel3, -2, 55, -2).addComponent(this.jTextField4))).addComponent(this.jTextField1).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel2).addComponent(this.jLabel1)).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(13, Short.MAX_VALUE).addComponent(this.jLabel2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField2, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField1, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jButton1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.textArea1, -2, 205, -2).addGroup(layout.createSequentialGroup().addComponent(this.jCheckBox1).addGap(18, 18, 18).addComponent(this.jLabel3).addGap(3, 3, 3).addComponent(this.jTextField3, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, Short.MAX_VALUE).addComponent(this.jTextField4, -2, -1, -2))).addContainerGap()));
        this.pack();
    }

    private boolean isSubExist(String sub) {
        try {
            InetAddress inetAddress = InetAddress.getByName(sub);
        }
        catch (UnknownHostException ex) {
            return false;
        }
        return true;
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        this.subs.clear();
        this.count = 0;
        new Thread(this){
            final /* synthetic */ GuiSubdomainBrute_Nirvana this$0;
            {
                this.this$0 = guiSubdomainBrute_Nirvana;
            }

            public void run() {
                GuiSubdomainBrute_Nirvana.access$3(this.this$0).append("Start!\n");
                try {
                    Scanner txtscan = new Scanner(new File(GuiSubdomainBrute_Nirvana.access$4(this.this$0).getText()));
                    while (txtscan.hasNextLine()) {
                        GuiSubdomainBrute_Nirvana.access$5(this.this$0).add(txtscan.nextLine());
                    }
                    ForkJoinPool forkJoinPool = new ForkJoinPool(Integer.parseInt(GuiSubdomainBrute_Nirvana.access$6(this.this$0).getText()));
                    forkJoinPool.submit(() -> ((Stream)Arrays.stream(GuiSubdomainBrute_Nirvana.access$5(this.this$0).toArray()).parallel()).forEach(sub -> GuiSubdomainBrute_Nirvana.access$8(this.this$0, String.valueOf(sub.toString()) + "." + GuiSubdomainBrute_Nirvana.access$7(this.this$0).getText())));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }.start();
    }

    private void go(String str) {
        if (this.isSubExist(str)) {
            if (this.jCheckBox1.isSelected()) {
                try {
                    this.textArea1.append(String.valueOf(str) + " : " + this.sendGET(str) + "\n");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.textArea1.append(String.valueOf(str) + "\n");
            }
        }
        ++this.count;
        this.jTextField4.setText(String.valueOf(this.count) + "/" + this.subs.size());
    }

    private String sendGET(String dom) throws Exception {
        String url = "http://" + dom;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.connect();
        int responseCode = con.getResponseCode();
        return "Code - " + responseCode;
    }

    private void jTextField2ActionPerformed(ActionEvent evt) {
    }

    private void jCheckBox1ActionPerformed(ActionEvent evt) {
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
            Logger.getLogger(GuiSubdomainBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(GuiSubdomainBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(GuiSubdomainBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiSubdomainBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable(){

            public void run() {
                new GuiSubdomainBrute_Nirvana().setVisible(true);
            }
        });
    }

    static /* synthetic */ void access$0(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana, ActionEvent actionEvent) {
        guiSubdomainBrute_Nirvana.jButton1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$1(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana, ActionEvent actionEvent) {
        guiSubdomainBrute_Nirvana.jTextField2ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$2(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana, ActionEvent actionEvent) {
        guiSubdomainBrute_Nirvana.jCheckBox1ActionPerformed(actionEvent);
    }

    static /* synthetic */ TextArea access$3(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana) {
        return guiSubdomainBrute_Nirvana.textArea1;
    }

    static /* synthetic */ JTextField access$4(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana) {
        return guiSubdomainBrute_Nirvana.jTextField1;
    }

    static /* synthetic */ List access$5(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana) {
        return guiSubdomainBrute_Nirvana.subs;
    }

    static /* synthetic */ JTextField access$6(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana) {
        return guiSubdomainBrute_Nirvana.jTextField3;
    }

    static /* synthetic */ JTextField access$7(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana) {
        return guiSubdomainBrute_Nirvana.jTextField2;
    }

    static /* synthetic */ void access$8(GuiSubdomainBrute_Nirvana guiSubdomainBrute_Nirvana, String string) {
        guiSubdomainBrute_Nirvana.go(string);
    }
}

