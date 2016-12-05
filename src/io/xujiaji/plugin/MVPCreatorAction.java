package io.xujiaji.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.xujiaji.plugin.dialog.EditorMVPDialog;

/**
 * Created by jiana on 05/12/16.
 */
public class MVPCreatorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        EditorMVPDialog mEditorMVPDialog = new EditorMVPDialog();
        mEditorMVPDialog.pack();
        mEditorMVPDialog.setVisible(true);
    }
}
