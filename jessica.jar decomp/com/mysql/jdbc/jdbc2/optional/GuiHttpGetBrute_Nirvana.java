/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
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

public class GuiHttpGetBrute_Nirvana
extends JFrame {
    private List<String> words = new ArrayList<String>();
    private List<String> urlList = new ArrayList<String>();
    private JButton jButton1;
    private JButton jButton2;
    private JButton jButton3;
    private JComboBox<String> jComboBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private JTextField jTextField4;
    private JTextField jTextField5;
    private JTextField jTextField6;
    private TextArea textArea1;

    public GuiHttpGetBrute_Nirvana() {
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
        this.jTextField4 = new JTextField();
        this.jLabel4 = new JLabel();
        this.jTextField5 = new JTextField();
        this.jLabel5 = new JLabel();
        this.jButton2 = new JButton();
        this.jButton3 = new JButton();
        this.jLabel6 = new JLabel();
        this.jTextField6 = new JTextField();
        this.jComboBox1 = new JComboBox();
        this.setTitle("HTTP/HTTPS GET Method Brute");
        this.jTextField1.setToolTipText("");
        this.jTextField1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$0(this.this$0, evt);
            }
        });
        this.jLabel1.setText("List of targets");
        this.jLabel2.setText("Path to list with words");
        this.jTextField2.setToolTipText("");
        this.jLabel3.setText("Log");
        this.jButton1.setText("Start");
        this.jButton1.setToolTipText("");
        this.jButton1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$1(this.this$0, evt);
            }
        });
        this.jTextField3.setText("0/0");
        this.jTextField4.setText("200,403");
        this.jTextField4.setToolTipText("");
        this.jLabel4.setText("Threads");
        this.jTextField5.setText("1");
        this.jTextField5.setToolTipText("");
        this.jLabel5.setText("Allowed Responce Codes");
        this.jButton2.setText("...");
        this.jButton2.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$2(this.this$0, evt);
            }
        });
        this.jButton3.setText("...");
        this.jButton3.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$3(this.this$0, evt);
            }
        });
        this.jLabel6.setText("Allowed context on page");
        this.jTextField6.setToolTipText("");
        this.jTextField6.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$4(this.this$0, evt);
            }
        });
        this.jComboBox1.setModel(new DefaultComboBoxModel<String>(new String[]{"By responce codes", "By context on page"}));
        this.jComboBox1.setToolTipText("");
        this.jComboBox1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiHttpGetBrute_Nirvana.access$5(this.this$0, evt);
            }
        });
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jTextField3).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(this.jButton1, -2, 382, -2)).addGroup(layout.createSequentialGroup().addComponent(this.textArea1, -2, 357, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jTextField4).addComponent(this.jTextField5, GroupLayout.Alignment.TRAILING).addComponent(this.jTextField6).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel4).addComponent(this.jLabel5).addComponent(this.jLabel6)).addGap(0, 0, Short.MAX_VALUE)).addComponent(this.jComboBox1, 0, -1, Short.MAX_VALUE))).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addComponent(this.jTextField2, GroupLayout.Alignment.LEADING, -1, 464, Short.MAX_VALUE).addComponent(this.jLabel1, GroupLayout.Alignment.LEADING).addComponent(this.jLabel2, GroupLayout.Alignment.LEADING).addComponent(this.jLabel3, GroupLayout.Alignment.LEADING).addComponent(this.jTextField1, GroupLayout.Alignment.LEADING)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jButton2).addComponent(this.jButton3)).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(7, 7, 7).addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jTextField1, -2, -1, -2).addComponent(this.jButton3)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jLabel2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jTextField2, -2, -1, -2).addComponent(this.jButton2)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addComponent(this.jLabel3).addGap(6, 6, 6).addComponent(this.textArea1, -2, 192, -2)).addGroup(layout.createSequentialGroup().addComponent(this.jComboBox1, -2, -1, -2).addGap(18, 18, 18).addComponent(this.jLabel6).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField6, -2, -1, -2).addGap(18, 18, 18).addComponent(this.jLabel5).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField4, -2, -1, -2).addGap(17, 17, 17).addComponent(this.jLabel4).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField5, -2, -1, -2))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jButton1, -1, 54, Short.MAX_VALUE).addComponent(this.jTextField3, -2, -1, -2))));
        this.pack();
    }

    private void jTextField1ActionPerformed(ActionEvent evt) {
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        this.words.clear();
        this.urlList.clear();
        new Thread(this){
            int count;
            final /* synthetic */ GuiHttpGetBrute_Nirvana this$0;
            {
                this.this$0 = guiHttpGetBrute_Nirvana;
                this.count = 0;
            }

            public void run() {
                try {
                    String line2;
                    String line;
                    File file = new File(GuiHttpGetBrute_Nirvana.access$6(this.this$0).getText());
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((line = br.readLine()) != null) {
                        GuiHttpGetBrute_Nirvana.access$7(this.this$0).add(line);
                    }
                    File file2 = new File(GuiHttpGetBrute_Nirvana.access$8(this.this$0).getText());
                    BufferedReader br2 = new BufferedReader(new FileReader(file2));
                    while ((line2 = br2.readLine()) != null) {
                        GuiHttpGetBrute_Nirvana.access$9(this.this$0).add(line2);
                    }
                    ArrayList<String> urls = new ArrayList<String>();
                    for (String s1 : GuiHttpGetBrute_Nirvana.access$9(this.this$0)) {
                        for (String s2 : GuiHttpGetBrute_Nirvana.access$7(this.this$0)) {
                            String url = s1.replace("{text}", s2);
                            urls.add(url);
                        }
                    }
                    GuiHttpGetBrute_Nirvana.access$10(this.this$0).setText("0/" + urls.size());
                    ForkJoinPool forkJoinPool = new ForkJoinPool(Integer.parseInt(GuiHttpGetBrute_Nirvana.access$11(this.this$0).getText()));
                    if ("By responce codes".equals(GuiHttpGetBrute_Nirvana.access$12(this.this$0).getSelectedItem().toString())) {
                        forkJoinPool.submit(() -> ((Stream)Arrays.stream(urls.toArray()).parallel()).forEach(url2 -> this.go(url2.toString())));
                    } else if ("By context on page".equals(GuiHttpGetBrute_Nirvana.access$12(this.this$0).getSelectedItem().toString())) {
                        forkJoinPool.submit(() -> ((Stream)Arrays.stream(urls.toArray()).parallel()).forEach(url2 -> this.goByContext(url2.toString())));
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }

            private void go(String url) {
                try {
                    if (this.pageExist(url)) {
                        GuiHttpGetBrute_Nirvana.access$13(this.this$0).append(String.valueOf(url) + "\n");
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                ++this.count;
                GuiHttpGetBrute_Nirvana.access$10(this.this$0).setText(String.valueOf(this.count) + "/" + GuiHttpGetBrute_Nirvana.access$7(this.this$0).size() * GuiHttpGetBrute_Nirvana.access$9(this.this$0).size());
            }

            private void goByContext(String url) {
                try {
                    if (this.pageContainsContext(url)) {
                        GuiHttpGetBrute_Nirvana.access$13(this.this$0).append(String.valueOf(url) + "\n");
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                ++this.count;
                GuiHttpGetBrute_Nirvana.access$10(this.this$0).setText(String.valueOf(this.count) + "/" + GuiHttpGetBrute_Nirvana.access$7(this.this$0).size() * GuiHttpGetBrute_Nirvana.access$9(this.this$0).size());
            }

            private boolean pageContainsContext(String url) throws Exception {
                StringBuffer response = new StringBuffer();
                URL obj = new URL(url);
                if (url.startsWith("https://")) {
                    String inputLine;
                    HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                } else {
                    String inputLine;
                    HttpURLConnection con = (HttpURLConnection)obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                return response.toString().contains(GuiHttpGetBrute_Nirvana.access$14(this.this$0).getText());
            }

            private boolean pageExist(String url) throws Exception {
                HttpURLConnection con;
                int responseCode = 0;
                URL obj = new URL(url);
                if (url.startsWith("https://")) {
                    con = (HttpsURLConnection)obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    con.connect();
                    responseCode = con.getResponseCode();
                } else {
                    con = (HttpURLConnection)obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    con.connect();
                    responseCode = con.getResponseCode();
                }
                String[] codes = GuiHttpGetBrute_Nirvana.access$15(this.this$0).getText().replace(" ", "").split(",");
                return Arrays.asList(codes).contains(String.valueOf(responseCode));
            }
        }.start();
    }

    private void jButton3ActionPerformed(ActionEvent evt) {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == 0) {
            File selectedFile = jfc.getSelectedFile();
            this.jTextField1.setText(selectedFile.getAbsolutePath());
        }
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

    private void jComboBox1ActionPerformed(ActionEvent evt) {
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
            Logger.getLogger(GuiHttpGetBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(GuiHttpGetBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(GuiHttpGetBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiHttpGetBrute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable(){

            public void run() {
                new GuiHttpGetBrute_Nirvana().setVisible(true);
            }
        });
    }

    static /* synthetic */ void access$0(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jTextField1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$1(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jButton1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$2(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jButton2ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$3(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jButton3ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$4(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jTextField6ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$5(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana, ActionEvent actionEvent) {
        guiHttpGetBrute_Nirvana.jComboBox1ActionPerformed(actionEvent);
    }

    static /* synthetic */ JTextField access$6(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField2;
    }

    static /* synthetic */ List access$7(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.words;
    }

    static /* synthetic */ JTextField access$8(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField1;
    }

    static /* synthetic */ List access$9(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.urlList;
    }

    static /* synthetic */ JTextField access$10(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField3;
    }

    static /* synthetic */ JTextField access$11(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField5;
    }

    static /* synthetic */ JComboBox access$12(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jComboBox1;
    }

    static /* synthetic */ TextArea access$13(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.textArea1;
    }

    static /* synthetic */ JTextField access$14(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField6;
    }

    static /* synthetic */ JTextField access$15(GuiHttpGetBrute_Nirvana guiHttpGetBrute_Nirvana) {
        return guiHttpGetBrute_Nirvana.jTextField4;
    }
}

