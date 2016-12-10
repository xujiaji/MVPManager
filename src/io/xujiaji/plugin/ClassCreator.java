package io.xujiaji.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.xujiaji.plugin.model.EditEntity;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        importContractClass(project, classContract, editEntity.getViewParent());
        importContractClass(project, classContract, editEntity.getPresenterParent());
        importContractClass(project, classContract, editEntity.getPresenterParent());


        //add parent interface class
        extendsClass(factory, searchScope, viewInterface, editEntity.getViewParent());
        extendsClass(factory, searchScope, presenterInterface, editEntity.getPresenterParent());
        extendsClass(factory, searchScope, modelInterface, editEntity.getModelParent());

        //add method to view,presenter,model interface
        addMethodToClass(project, viewInterface, editEntity.getView(), false);
        addMethodToClass(project, presenterInterface, editEntity.getPresenter(), false);
        addMethodToClass(project, modelInterface, editEntity.getModel(), false);

        //add view,presenter,model Interface to Contract
        classContract.add(viewInterface);
        classContract.add(presenterInterface);
        classContract.add(modelInterface);

        PsiImportStatement importStatement = factory.createImportStatement(classContract);
        ((PsiJavaFile) classPresenter.getContainingFile()).getImportList().add(importStatement);
        ((PsiJavaFile) classModel.getContainingFile()).getImportList().add(importStatement);

        impInterface(factory, searchScope, classPresenter, editEntity.getContractName() + "Contract.Presenter");
        impInterface(factory, searchScope, classModel, editEntity.getContractName() + "Contract.Model");

        addMethodToClass(project, classPresenter, editEntity.getPresenter(), true);
        addMethodToClass(project, classModel, editEntity.getModel(), true);

        openFiles(project, classContract, classPresenter, classModel);
    }


    private static void importContractClass(Project project, PsiClass classContract, String viewParent) {
        String[] strings = viewParent.split("\\.");
        if (strings.length < 1) {
            return;
        }
        searchAndImportClass(strings[0], classContract, project);
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
     *
     * @param psiClass
     */
    private static void addMethodToClass(Project project, PsiClass psiClass, java.util.List<String> methods, boolean over) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
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

            //Example: List<String>
            if (strings[0].matches(".+<.+>")) {
                //List<String>  >>>   List
                Pattern pattern = Pattern.compile("(?<=^)[a-zA-Z1-9_$]+(?=<.+>)");
                Matcher matcher = pattern.matcher(strings[0]);
                if (matcher.find()) {
                    searchAndImportClass(matcher.group(), psiClass, project);
                }
                //List<String>   >>>   String
                Pattern p = Pattern.compile("(?<=<).+(?=>)");
                Matcher m = p.matcher(strings[0]);
                if (m.find()) {
                    String s = m.group();
                    //Not match example : List<List<String>> 、 List<? extend Pet>
                    if (s.matches("^[a-zA-Z1-9_$]+$")) {
                        searchAndImportClass(s, psiClass, project);
                    }
                }

            } else {
                searchAndImportClass(strings[0], psiClass, project);
            }
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
     *
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
     *
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

    /**
     * search and import class
     *
     * @param name
     * @param resClass
     * @param project
     */
    private static void searchAndImportClass(String name, PsiClass resClass, Project project) {
        if ("".equals(name) || "void".equals(name)) return;
        PsiClass importClass = searchClassByName(name, project);
        if (importClass == null) return;
        PsiJavaFile psiJavaFile = ((PsiJavaFile) importClass.getContainingFile());
        String packageName = psiJavaFile.getPackageName();
        if (packageName.contains("java.lang")) return;
        importClass(importClass, resClass, project);
    }

    /**
     * search class by class name.
     *
     * @param name
     * @param project
     * @return
     */
    private static PsiClass searchClassByName(String name, Project project) {
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(name, searchScope);
        if (psiClasses.length == 1) {
            return psiClasses[0];
        }
        if (psiClasses.length > 1) {
            for (PsiClass pc :
                    psiClasses) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) pc.getContainingFile();
                String packageName = psiJavaFile.getPackageName();
                if (List.class.getPackage().getName().equals(packageName)) {
                    return pc;
                }
            }
        }
        return null;
    }

    /**
     * import class
     *
     * @param importClass
     * @param resClass
     * @param project
     */
    private static void importClass(PsiClass importClass, PsiClass resClass, Project project) {
        if (importClass == null || resClass == null) return;
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiImportStatement importStatement = factory.createImportStatement(importClass);
        PsiJavaFile psiJavaFile = ((PsiJavaFile) resClass.getContainingFile());
        PsiImportList psiImportList = psiJavaFile.getImportList();
        if (psiImportList == null) return;
        for (PsiImportStatement pis : psiImportList.getImportStatements()) {
            if (pis.getText().equals(importStatement.getText())) {
                return;
            }
        }
        psiImportList.add(importStatement);
    }

    /**
     * open mvp's java file.
     * @param project
     */
    private static void openFiles(Project project, PsiClass ... psiClasses) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (PsiClass psiClass :
                psiClasses) {
            fileEditorManager.openFile(psiClass.getContainingFile().getVirtualFile(), true, true);
        }
    }

}
