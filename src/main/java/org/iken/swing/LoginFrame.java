package org.iken.swing;

import org.iken.wen.simulate.LoginSimulate;
import org.openqa.selenium.Cookie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

public class LoginFrame extends JFrame implements ActionListener {

    JPanel panel;
    JLabel label,label2;
    JButton loginButton,exitButton;
    JTextField jTextField;
    JPasswordField passwordField;

    public LoginFrame () {
        this.setTitle("企查查");
        this.setSize(265,140);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());//设置为流式布局
        label = new JLabel("用户名");
        label2 = new JLabel("密  码 ");
        loginButton = new JButton("登录");
        loginButton.addActionListener(this);//监听事件
        jTextField = new JTextField(16);//设置文本框的长度
        passwordField = new JPasswordField(16);//设置密码框

        panel.add(label);//把组件添加到面板panel
        panel.add(jTextField);
        panel.add(label2);
        panel.add(passwordField);
        panel.add(loginButton);

        this.add(panel);//实现面板panel

        this.setVisible(true);//设置可见
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            if (actionEvent.getSource()==loginButton) {
                if ("".equals(jTextField.getText().trim()) || "".equals(passwordField.getText().trim())){
                    JOptionPane.showMessageDialog(null, "账号或密码不能为空！");
                } else {
                    Set<Cookie> cookieSet = LoginSimulate.login(jTextField.getText(), passwordField.getText());
                    if (null != cookieSet) { // 如果cookies为空，则登录失败
                        JOptionPane.showMessageDialog(null,"登录成功！" );
                        this.setVisible(false);
                        FileChooser fileChooser = new FileChooser(cookieSet);
                    }else {
                        JOptionPane.showMessageDialog(null, "登录失败！");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
