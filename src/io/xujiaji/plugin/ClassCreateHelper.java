package io.xujiaji.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import io.xujiaji.plugin.model.EditEntity;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by jiana on 05/12/16.
 * class create
 */
public class ClassCreateHelper {

    public static void create(AnActionEvent e, EditEntity editEntity) throws FileNotFoundException, UnsupportedEncodingException {
        String path = createContract(e);
        String contractFolder = path + "/contract";

        Project project = e.getProject();

//        PsiDirectory baseDir = PsiDirectoryFactory.getInstance(project).createDirectory(project.getBaseDir());
        PsiDirectory moduleDir = PsiDirectoryFactory.getInstance(project).createDirectory(e.getData(PlatformDataKeys.VIRTUAL_FILE));
//        PsiDirectory[] pfs = baseDir.getSubdirectories();
//        for (PsiDirectory p : pfs) {
//            System.out.println(p.getName());
//        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                new WriteCommandAction(project) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        //writing to file
                        PsiDirectory subDir = moduleDir.findSubdirectory("contract");
                        if (subDir == null) {
                            subDir = moduleDir.createSubdirectory("contract");
                        }
                        PsiClass clazz = JavaDirectoryService.getInstance().createInterface(subDir, "Contract");
                        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//                        PsiField field = factory.createFieldFromText("public int a = 0;", clazz);
//                        PsiClass viewInterface = factory.createClassFromText("interface View {}", null);
                        PsiClass viewInterface = factory.createInterface("View");
                        PsiClass presenterInterface = factory.createInterface("Presenter");
                        PsiClass modelInterface = factory.createInterface("Model");
                        clazz.add(viewInterface);
                        clazz.add(presenterInterface);
                        clazz.add(modelInterface);
                    }
                }.execute();
            }
        });
    }

    /**
     * get current folder
     * @param e
     * @return
     */
    public static String createContract(AnActionEvent e) {
//        VirtualFile currentFile = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        VirtualFile currentFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String path = currentFile.getPath();
        return path;
    }

}
