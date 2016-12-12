package io.xujiaji.plugin.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import io.xujiaji.plugin.dialog.ChangeMVPDialog;
import io.xujiaji.plugin.model.MethodEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiana on 12/12/16.
 */
public class ChangeHelper {
      public static void methodAction(Project project, PsiJavaFile psiJavaFile, int type, MethodEntity methodEntity, boolean isDel) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                new WriteCommandAction(project) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        method(project, psiJavaFile, type, methodEntity, isDel);
                    }
                }.execute();
            }
        });
    }

    public static void method(Project project, PsiJavaFile psiJavaFile, int type, MethodEntity methodEntity, boolean isDel) {
        for (PsiElement psiInterface :
                getClasses(psiJavaFile)) {
            if (psiInterface instanceof PsiClass) {
                switch (type) {
                    case ChangeMVPDialog.VIEW:
                        if (!ClassHelper.VIEW.equals(((PsiClass) psiInterface).getName())) continue;
                        break;
                    case ChangeMVPDialog.PRESENTER:
                        if (!ClassHelper.PRESENTER.equals(((PsiClass) psiInterface).getName())) continue;
                        break;
                    case ChangeMVPDialog.MODEL:
                        if (!ClassHelper.MODEL.equals(((PsiClass) psiInterface).getName())) continue;
                        break;
                }
                if (isDel) {
                    delMethod(project, psiInterface, methodEntity);
                } else {
                    ClassHelper.addMethodToClass(project, (PsiClass) psiInterface, createList(methodEntity), false);
                }
                if (((PsiClass) psiInterface).getExtendsList() == null) return;
//                ClassInheritorsSearch query = ClassInheritorsSearch.search((PsiClass) psiInterface, true);
                Query<PsiClass> query = ClassInheritorsSearch.search((PsiClass) psiInterface);
                for (Iterator<PsiClass> it = query.iterator(); it.hasNext(); ) {
                    PsiClass p = it.next();
                    if (isDel) {
                        delMethod(project, p, methodEntity);
                    } else {
                        ClassHelper.addMethodToClass(project, p, createList(methodEntity), true);
                    }
                }
            }
        }
    }

    private static PsiElement[] getClasses(PsiJavaFile psiJavaFile) {
        for (PsiElement p :
                psiJavaFile.getClasses()) {
            if (p instanceof PsiClass) {
                return p.getChildren();
            }
        }
        return null;
    }

    private static void delMethod(Project project, PsiElement psiElement, MethodEntity methodEntity) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiMethod psiMethod = factory.createMethodFromText(methodEntity.getReturnStr() + " " + methodEntity.getMethodStr() + ";", null);
        for (PsiElement p :
                psiElement.getChildren()) {
            if (p instanceof PsiMethod) {
                if (!psiMethod.getReturnType().equals(((PsiMethod) p).getReturnType())) continue;
                System.out.println(getMethodText(psiMethod.getText()));
                System.out.println(getMethodText(p.getText()));
                if (getMethodText(psiMethod.getText()).equals(getMethodText(p.getText()))) {
                   p.delete();
                   break;
                }
            }
        }
    }

    /**
     * get method text (include param)
     * public void method(List<String> list, String str) { .... }       ==>
     * @param text
     * @return method(List<String> list, String str)
     */
    private static String getMethodText(String text) {
        Pattern pattern = Pattern.compile("(?<=\\s)\\S+\\(.+(?=;|\\{)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return "";
    }

    private static List<String> createList(MethodEntity methodEntity) {
        List<String> list = new ArrayList<>(1);
        list.add(methodEntity.getReturnStr() + "##" + methodEntity.getMethodStr());
        return list;
    }

}
