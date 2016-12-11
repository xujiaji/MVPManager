package io.xujiaji.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiJavaFile;
import io.xujiaji.plugin.dialog.ChangeMVPDialog;
import io.xujiaji.plugin.util.ClassHelper;
import io.xujiaji.plugin.util.MsgUtil;

import java.util.Map;

/**
 * Created by jiana on 11/12/16.
 */
public class MVPChangeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiJavaFile psiJavaFile = ClassHelper.getJavaFile(e);
        boolean isContract = psiJavaFile.getName().contains("Contract");
        if (!isContract) {
            MsgUtil.showInfo("The java file is not contract!");
            return;
        }
        Map<String, Object[][]> objects = ClassHelper.getMethod(psiJavaFile);
        ChangeMVPDialog dialog = new ChangeMVPDialog(objects);
        dialog.pack();
        dialog.setVisible(true);
    }


}
