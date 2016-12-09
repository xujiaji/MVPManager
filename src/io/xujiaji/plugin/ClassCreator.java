package io.xujiaji.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.xujiaji.plugin.model.EditEntity;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by jiana on 05/12/16.
 * class create
 */
public class ClassCreator {
    public static final String PACKAGE_MODEL = "model";
    public static final String PACKAGE_PRESENTER = "presenter";
    public static final String PACKAGE_CONTRACT = "contract";

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
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        //create package and class
        PsiClass classContract = createClass(moduleDir, PACKAGE_CONTRACT, editEntity.getContractName() + "Contract");
        PsiClass classPresenter = createClass(moduleDir, PACKAGE_PRESENTER, editEntity.getContractName() + "Presenter");
        PsiClass classModel = createClass(moduleDir, PACKAGE_MODEL, editEntity.getContractName() + "Model");


        //create view,presenter,model interface
        PsiClass viewInterface = factory.createInterface("View");
        PsiClass presenterInterface = factory.createInterface("Presenter");
        PsiClass modelInterface = factory.createInterface("Model");

        importContractClass(project, factory, searchScope, classContract, editEntity.getViewParent());
        importContractClass(project, factory, searchScope, classContract, editEntity.getPresenterParent());
        importContractClass(project, factory, searchScope, classContract, editEntity.getPresenterParent());


        //add parent interface class
        extendsClass(factory, searchScope, viewInterface, editEntity.getViewParent());
        extendsClass(factory, searchScope, presenterInterface, editEntity.getPresenterParent());
        extendsClass(factory, searchScope, modelInterface, editEntity.getModelParent());

        //add method to view,presenter,model interface
        addMethodToClass(factory, viewInterface, editEntity.getView(), false);
        addMethodToClass(factory, presenterInterface, editEntity.getPresenter(), false);
        addMethodToClass(factory, modelInterface, editEntity.getModel(), false);

        //add view,presenter,model Interface to Contract
        classContract.add(viewInterface);
        classContract.add(presenterInterface);
        classContract.add(modelInterface);

        PsiImportStatement importStatement = factory.createImportStatement(classContract);
        ((PsiJavaFile) classPresenter.getContainingFile()).getImportList().add(importStatement);
        ((PsiJavaFile) classModel.getContainingFile()).getImportList().add(importStatement);

        impInterface(factory, searchScope, classPresenter, editEntity.getContractName() + "Contract.Presenter");
        impInterface(factory, searchScope, classModel, editEntity.getContractName() + "Contract.Model");

        addMethodToClass(factory, classPresenter, editEntity.getPresenter(), true);
        addMethodToClass(factory, classModel, editEntity.getModel(), true);

    }


    private static void importContractClass(Project project, PsiElementFactory factory, GlobalSearchScope searchScope,PsiClass classContract, String viewParent) {
        String[] strings = viewParent.split("\\.");
        if (strings.length < 1) {
            return;
        }
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(strings[0], searchScope);
        PsiImportStatement importStatement = factory.createImportStatement(psiClasses[0]);
        PsiJavaFile psiJavaFile = ((PsiJavaFile) classContract.getContainingFile());
        PsiImportList psiImportList = psiJavaFile.getImportList();
        for (PsiImportStatement pis : psiImportList.getImportStatements()) {
            if (pis.getText().equals(importStatement.getText())) {
                return;
            }
        }
        psiImportList.add(importStatement);
    }

    private static PsiClass createClass(PsiDirectory moduleDir, String packageName, String name) {
        //writing to file
        PsiDirectory subDir = moduleDir.findSubdirectory(packageName);
        if (subDir == null) {
            subDir = moduleDir.createSubdirectory(packageName);
        }
        PsiClass clazz = null;
        if (PACKAGE_CONTRACT.equals(packageName)) {
            clazz = JavaDirectoryService.getInstance().createInterface(subDir, name);
        } else {
            clazz = JavaDirectoryService.getInstance().createClass(subDir, name);
        }

        return clazz;
    }

    /**
     * add method to Class
     * @param factory
     * @param psiClass

     */
    private static void addMethodToClass(PsiElementFactory factory, PsiClass psiClass, java.util.List<String> methods, boolean over) {
        for (String method : methods) {
            String[] strings = method.split("##");
            PsiMethod psiMethod = null;
            if (over) {
                psiMethod = factory.createMethodFromText("public " + strings[0] + " " + strings[1] + " {\n\n}", psiClass);
                psiMethod.getModifierList().addAnnotation("Override");
            } else {
                psiMethod = factory.createMethodFromText(strings[0] + " " + strings[1] + ";", psiClass);
            }
            psiClass.add(psiMethod);
        }
    }

    public static boolean isFileExists(AnActionEvent e, String name) {
        Project project = e.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(name + "Contract", searchScope);
        return psiClasses.length > 0;
    }

    /**
     * implements interface
     * @param factory
     * @param searchScope
     * @param psiClass
     * @param className
     */
    private static void impInterface(PsiElementFactory factory, GlobalSearchScope searchScope, PsiClass psiClass, String className) {
        PsiJavaCodeReferenceElement pjcre = factory.createFQClassNameReferenceElement(className, searchScope);
        psiClass.getImplementsList().add(pjcre);
    }

    /**
     * extends class
     * @param factory
     * @param searchScope
     * @param psiClass
     * @param className
     */
    private static void extendsClass(PsiElementFactory factory, GlobalSearchScope searchScope, PsiClass psiClass, String className) {
        if (className == null || "".equals(className)) {
            return;
        }
        PsiJavaCodeReferenceElement pjcre = factory.createFQClassNameReferenceElement(className, searchScope);
        psiClass.getExtendsList().add(pjcre);
    }
}
