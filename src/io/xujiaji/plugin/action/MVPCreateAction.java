package io.xujiaji.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.xujiaji.plugin.util.ClassHelper;
import io.xujiaji.plugin.dialog.EditorMVPDialog;
import io.xujiaji.plugin.listener.EditorListener;
import io.xujiaji.plugin.model.EditEntity;
import io.xujiaji.plugin.model.InitEntity;
import io.xujiaji.plugin.util.MsgUtil;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by jiana on 05/12/16.
 */
public class MVPCreateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        InitEntity initEntity = new InitEntity(ClassHelper.dirList(e));
        EditorMVPDialog mEditorMVPDialog = new EditorMVPDialog(initEntity);
        mEditorMVPDialog.setEditorListener(new EditorListener() {
            @Override
            public void editOver(EditEntity editEntity) {
                if (ClassHelper.isContractExists(e, editEntity.getContractName())) {
                    MsgUtil.showInfo("Please enter again contract name,\n " + editEntity.getContractName() + " already exist.");
                    return;
                }
                try {
                    ClassHelper.create(e, editEntity);
                } catch (FileNotFoundException | UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                mEditorMVPDialog.dispose();
            }
        });
        mEditorMVPDialog.pack();
        mEditorMVPDialog.setVisible(true);

    }
}
