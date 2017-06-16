package io.xujiaji.plugin.dialog;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import io.xujiaji.plugin.Constant;
import io.xujiaji.plugin.listener.EditorListener;
import io.xujiaji.plugin.listener.IMDListener;
import io.xujiaji.plugin.model.EditEntity;
import io.xujiaji.plugin.model.InitEntity;
import io.xujiaji.plugin.model.MethodEntity;
import io.xujiaji.plugin.util.GenericHelper;
import io.xujiaji.plugin.widget.InputMethodDialog;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class EditorMVPDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton btnAddView;
    private JButton btnAddPresenter;
    private JButton btnAddModel;
    private JTable tableView;
    private JButton btnDelView;
    private JButton btnDelPresenter;
    private JButton btnDelModel;
    private JTable tablePresenter;
    private JTable tableModel;
    private JTextField viewParent;
    private JTextField contractName;
    private JTextField presenterParent;
    private JTextField modelParent;
    private JComboBox viewPackageName;
    private JTextField viewImpName;
    private JLabel labelUrl;
    private JCheckBox xmvpCheckBox;
    private JButton[] btnAddArr = new JButton[3];
    private JButton[] btnDelArr = new JButton[3];
    private JTable[] tableArr = new JTable[3];
    private EditorListener listener;
    private InitEntity initEntity;

    public void setEditorListener(EditorListener listener) {
        this.listener = listener;
    }

    public EditorMVPDialog(InitEntity initEntity) {
        this.initEntity = initEntity;
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        changeXMVP(PropertiesComponent.getInstance().getBoolean(Constant.IS_XMVP));
        fillArr();
        initView();
        addListener();
    }

    private void changeXMVP(boolean isXMVP) {
        if (isXMVP) {
            viewParent.setText(Constant.X_VIEW);
            presenterParent.setText(Constant.X_PRESENTER);
            modelParent.setText(Constant.X_MODEL);
        } else {
            viewParent.setText(Constant.NULL);
            presenterParent.setText(Constant.NULL);
            modelParent.setText(Constant.NULL);
        }
    }

    private void initView() {
        labelUrl.setForeground(JBColor.BLUE);
        xmvpCheckBox.setSelected(PropertiesComponent.getInstance().getBoolean(Constant.IS_XMVP));
        if (initEntity == null) return;
        String[] datasPackage = new String[initEntity.getPsiDirectories().length];
        for (int i = 0; i < datasPackage.length; i++) {
            datasPackage[i] = initEntity.getPsiDirectories()[i].getName();
        }
        ComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(datasPackage);
        viewPackageName.setModel(comboBoxModel);
    }

    private void addListener() {
        xmvpCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean b = xmvpCheckBox.isSelected();
                changeXMVP(b);
                PropertiesComponent.getInstance().setValue(Constant.IS_XMVP, b);
            }
        });

        labelUrl.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(Constant.XMVP_URL));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                labelUrl.setForeground(JBColor.RED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                labelUrl.setForeground(JBColor.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                labelUrl.setForeground(JBColor.BLUE);
            }
        });

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

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        for (int i = 0; i < btnAddArr.length; i++) {
            JButton btn = btnAddArr[i];
            int finalI = i;
            btn.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputMethodDialog.input(new IMDListener() {
                        @Override
                        public void complete(MethodEntity methodEntity) {
                            GenericHelper.addAMethod((DefaultTableModel) tableArr[finalI].getModel(), methodEntity);
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
                        model.removeRow(row - totalRow);
                        totalRow++;
                    }
                }
            });
        }
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

    private void onOK() {
        if (listener != null) {
            ArrayList<String> viewData = getData(tableView);
            ArrayList<String> presenterData = getData(tablePresenter);
            ArrayList<String> modelData = getData(tableModel);
            if (viewData == null || presenterData == null || modelData == null) {
                Messages.showMessageDialog("Incomplete information, please check", "Information", Messages.getInformationIcon());
                return;
            }

            String name = contractName.getText();
            if (name == null || name.equals("")) {
                Messages.showMessageDialog("Please input contract name!", "Information", Messages.getInformationIcon());
                return;
            }

            EditEntity ee = new EditEntity(viewData, presenterData, modelData);
            ee.setContractName(name.trim());
            ee.setViewParent(viewParent.getText().trim());
            ee.setPresenterParent(presenterParent.getText().trim());
            ee.setModelParent(modelParent.getText().trim());
            String packageName = (String) viewPackageName.getModel().getSelectedItem();
            if (packageName != null && !packageName.equals("") && initEntity != null && initEntity.getPsiDirectories() != null) {
                for (int i = 0; i < initEntity.getPsiDirectories().length; i++) {
                    if (initEntity.getPsiDirectories()[i].getName().equals(packageName)) {
                        ee.setViewDir(initEntity.getPsiDirectories()[i]);
                        break;
                    }
                }
            }
            if ("".equals(viewImpName.getText().trim())) {
                Messages.showMessageDialog("Please input implement view!", "Information", Messages.getInformationIcon());
                return;
            }
            ee.setViewName(viewImpName.getText().trim());
            listener.editOver(ee);
        }
    }

    /**
     * Get data in JTable
     *
     * @param jTable
     * @return
     */
    private ArrayList<String> getData(JTable jTable) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < jTable.getModel().getRowCount(); i++) {
            TableModel model = jTable.getModel();
            String returnStr = (String) model.getValueAt(i, 0);
            String methodStr = (String) model.getValueAt(i, 1);
            returnStr = returnStr.trim();
            methodStr = methodStr.trim();
            if (TextUtils.isEmpty(returnStr) || TextUtils.isEmpty(methodStr)) {
                return null;
            }
            list.add(returnStr + "##" + methodStr);
        }

        return list;
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        EditorMVPDialog dialog = new EditorMVPDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        initJTable(tableView = newTableInstance());
        initJTable(tablePresenter = newTableInstance());
        initJTable(tableModel = newTableInstance());
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
    private JTable newTableInstance() {
        String[] defaultValue = {"void", "method()"};
        DefaultTableModel mDefaultTableMoadel = new DefaultTableModel();
        Object[][] object = new Object[1][2];
        object[0][0] = "void";
        object[0][1] = "method()";
        mDefaultTableMoadel.setDataVector(object, new Object[]{"return", "method"});
        JTable mJtable = new JTable(mDefaultTableMoadel) {
            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        return mJtable;
    }
}
