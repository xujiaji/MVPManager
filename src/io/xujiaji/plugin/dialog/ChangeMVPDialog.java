package io.xujiaji.plugin.dialog;

import com.intellij.openapi.ui.Messages;
import io.xujiaji.plugin.listener.ChangeListener;
import io.xujiaji.plugin.listener.IMDListener;
import io.xujiaji.plugin.model.MethodEntity;
import io.xujiaji.plugin.util.ClassHelper;
import io.xujiaji.plugin.util.GenericHelper;
import io.xujiaji.plugin.widget.InputMethodDialog;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class ChangeMVPDialog extends JDialog {
    public static final int VIEW = 0;
    public static final int PRESENTER = 1;
    public static final int MODEL = 2;
    private JPanel contentPane;
    private JButton btnAddView;
    private JTable tableView;
    private JButton btnDelView;
    private JButton btnAddPresenter;
    private JTable tablePresenter;
    private JButton btnDelPresenter;
    private JButton btnAddModel;
    private JTable tableModel;
    private JButton btnDelModel;
    private JButton[] btnAddArr = new JButton[3];
    private JButton[] btnDelArr = new JButton[3];
    private JTable[] tableArr = new JTable[3];
    private Map<String, Object[][]> objects;
    private ChangeListener listener;

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public ChangeMVPDialog(Map<String, Object[][]> objects) {
        this.objects = objects;
        setContentPane(contentPane);
        setModal(true);
        fillArr();
        addListener();
    }


    /**
     * fill view array
     */
    private void fillArr() {
        btnAddArr[0] = btnAddView;
        btnAddArr[1] = btnAddPresenter;
        btnAddArr[2] = btnAddModel;

        btnDelArr[0] = btnDelView;
        btnDelArr[1] = btnDelPresenter;
        btnDelArr[2] = btnDelModel;

        tableArr[0] = tableView;
        tableArr[1] = tablePresenter;
        tableArr[2] = tableModel;

    }

    private void addListener() {

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        for (int i = 0; i < btnAddArr.length; i++) {
            JButton btn = btnAddArr[i];
            int finalI = i;
            btn.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputMethodDialog.input(new IMDListener() {
                        @Override
                        public void complete(MethodEntity methodEntity) {
                            methodEntity = GenericHelper.addAMethod((DefaultTableModel) tableArr[finalI].getModel(), methodEntity);
                            if (listener != null) listener.add(finalI, methodEntity);
                        }
                    });
                }
            });
        }

        for (int i = 0; i < btnDelArr.length; i++) {
            JButton btn = btnDelArr[i];
            int finalI = i;
            btn.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultTableModel model = (DefaultTableModel) tableArr[finalI].getModel();
                    int totalRow = 0;
                    for (int row : tableArr[finalI].getSelectedRows()) {
                        if (listener != null) {
                            String returnStr = (String) model.getValueAt(row, 0);
                            String methodStr = (String) model.getValueAt(row, 1);
                            listener.del(finalI, new MethodEntity(returnStr, methodStr));
                        }
                        model.removeRow(row - totalRow);
                        totalRow++;
                    }
                }
            });
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ChangeMVPDialog dialog = new ChangeMVPDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


    private void createUIComponents() {

        initJTable(tableView = newTableInstance(objects.get(ClassHelper.VIEW)));
        initJTable(tablePresenter = newTableInstance(objects.get(ClassHelper.PRESENTER)));
        initJTable(tableModel = newTableInstance(objects.get(ClassHelper.MODEL)));
    }


    private void initJTable(JTable mJtable) {
        mJtable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (e.getLastRow() == -1) {
                        return;
                    }
                    String value = mJtable.getValueAt(e.getLastRow(), e.getColumn()).toString();
                    if (value.trim().equals("")) {
                        Messages.showMessageDialog("can't be empty", "Information", Messages.getInformationIcon());
                    }
                    for (int i = 0; i < mJtable.getModel().getRowCount(); i++) {
                        if (mJtable.getValueAt(i, 1).toString().equals(value) && e.getLastRow() != i) {
                            Messages.showMessageDialog("This method has been added", "Information", Messages.getInformationIcon());
                        }
                    }
                }
            }
        });

        mJtable.getTableHeader().setPreferredSize(new Dimension(tableView.getTableHeader().getWidth(), 20));
        mJtable.getColumnModel().getColumn(0).setPreferredWidth(15);
        mJtable.getColumnModel().getColumn(1).setPreferredWidth(155);
        mJtable.setRowHeight(25);
    }

    /**
     * create a JTable instance
     *
     * @return
     */
    private JTable newTableInstance(Object[][] objs) {
        DefaultTableModel mDefaultTableMoadel = new DefaultTableModel();
        if (objs == null) {
            objs = new Object[1][2];
            objs[0][0] = "void";
            objs[0][1] = "method()";
        }
        mDefaultTableMoadel.setDataVector(objs, new Object[]{"return", "method"});
        JTable mJtable = new JTable(mDefaultTableMoadel) {
            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        return mJtable;
    }
}
