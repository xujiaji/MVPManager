package io.xujiaji.plugin.widget;

import io.xujiaji.plugin.listener.IMDListener;
import io.xujiaji.plugin.model.MethodEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by jiana on 12/12/16.
 */
public class InputMethodDialog extends JDialog {
    final JTextField fieldReturn, fieldMethodName;//Define two input boxes.
    private IMDListener listener;

    public InputMethodDialog() {
        setTitle("Add a method.");
        setModal(true);
        setSize(220, 180);//对话框的大小

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);//关闭后销毁对话框
        setLocationRelativeTo(null);
        JLabel jReturn = new JLabel("Input return type:");
        fieldReturn = new JTextField(8);
        fieldReturn.setText("void");
        JLabel jMethod = new JLabel("Input method name/param:");
        fieldMethodName = new JTextField(8);
        JPanel jp = new JPanel(new GridLayout(4, 1));
        jp.add(jReturn);
        jp.add(fieldReturn);
        jp.add(jMethod);
        jp.add(fieldMethodName);
        JButton jb = new JButton("ADD");
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
                if (listener != null) {
                    listener.complete(new MethodEntity(fieldReturn.getText().trim(), fieldMethodName.getText().trim()));
                }
            }
        });

        add(jp);
        add(jb,BorderLayout.SOUTH);

    }


    public void setListener(IMDListener listener) {
        this.listener = listener;
    }

    public static void main(String[] args) {
        new InputMethodDialog().setVisible(true);
    }

    /**
     * show
     * @param listener
     */
    public static void input(IMDListener listener) {
        InputMethodDialog imd = new InputMethodDialog();
        imd.setListener(listener);
        imd.setVisible(true);
    }
}
