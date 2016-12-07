package io.xujiaji.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
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
public class ClassCreator {

    public static void create(AnActionEvent e, EditEntity editEntity) throws FileNotFoundException, UnsupportedEncodingException {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                new WriteCommandAction(e.getProject()) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        createContract(e, editEntity);
                    }
                }.execute();
            }
        });

    }

    public static void createContract(AnActionEvent e, EditEntity editEntity) {
        Project project = e.getProject();
        PsiDirectory moduleDir = PsiDirectoryFactory.getInstance(project).createDirectory(e.getData(PlatformDataKeys.VIRTUAL_FILE));

        //writing to file
        PsiDirectory subDir = moduleDir.findSubdirectory("contract");
        if (subDir == null) {
            subDir = moduleDir.createSubdirectory("contract");
        }
        PsiClass clazz = JavaDirectoryService.getInstance().createInterface(subDir, "Contract");
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//                        PsiField field = factory.createFieldFromText("public int a = 0;", clazz);
//                        PsiClass viewInterface = factory.createClassFromText("interface View {}", null);

        //create view,presenter,model interface
        PsiClass viewInterface = factory.createInterface("View");
        PsiClass presenterInterface = factory.createInterface("Presenter");
        PsiClass modelInterface = factory.createInterface("Model");

        //add method to view,presenter,model interface
        addMethodToClass(factory, viewInterface, editEntity.getView());
        addMethodToClass(factory, presenterInterface, editEntity.getPresenter());
        addMethodToClass(factory, modelInterface, editEntity.getModel());

        //add view,presenter,model Interface to Contract
        clazz.add(viewInterface);
        clazz.add(presenterInterface);
        clazz.add(modelInterface);
    }

    /**
     * add method to Class
     * @param factory
     * @param psiClass

     */
    private static void addMethodToClass(PsiElementFactory factory, PsiClass psiClass, java.util.List<String> methods) {
        for (String method : methods) {
            String[] strings = method.split("##");
            PsiMethod psiMethod = factory.createMethodFromText(strings[0] + " " + strings[1] + ";", psiClass);
            psiClass.add(psiMethod);
        }
    }
}
