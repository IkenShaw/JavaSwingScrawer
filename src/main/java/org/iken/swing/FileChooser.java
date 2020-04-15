package org.iken.swing;

import org.iken.untils.ExcelUtil;
import org.iken.crawl.WebCrawl;
import org.openqa.selenium.Cookie;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

public class FileChooser extends JFrame implements ActionListener{

    private JButton open=null;
    private Set<Cookie> cookieSet = null;

    public FileChooser(Set<Cookie> cookieSet){
        if ( null != cookieSet) {
            this.cookieSet = cookieSet;
        }
        open=new JButton("点击选择Excel");
        this.add(open);
        this.setSize(250,125);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        open.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc=new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
        jfc.showDialog(new JLabel(), "选择");
        File file=jfc.getSelectedFile();
        if (file != null && file.isFile() && (file.getName().endsWith(".xlsx") || file.getName().endsWith("xls"))) {
            // 开始读取
            ExcelUtil.dealExlce(file.getAbsolutePath());
            // 开始爬取
            try {
                WebCrawl.doGet(this.cookieSet);
                ExcelUtil.generateExcel();
                // 程序最后终止的地方，查询后直接把窗口关了
                System.exit(0); // 正常退出
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1); // 异常退出
            }
        }
    }

    public static void main(String[] args) {
        new FileChooser(null);
    }

}