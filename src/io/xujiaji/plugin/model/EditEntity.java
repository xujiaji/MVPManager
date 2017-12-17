package io.xujiaji.plugin.model;

import com.intellij.psi.PsiDirectory;

import java.util.List;

/**
 * Created by jiana on 05/12/16.
 */
public class EditEntity {
    private String contractName;
    private String viewParent;
    private String presenterParent;
    private String modelParent;
    private String baseViewParent;
    private List<String> view;
    private List<String> presenter;
    private List<String> model;
    private PsiDirectory viewDir;
    private String viewName;

    private boolean isSinglePackage;

    public EditEntity(String contractName) {
        this.contractName = contractName;
    }

    public EditEntity(List<String> view, List<String> presenter, List<String> model) {
        this.view = view;
        this.presenter = presenter;
        this.model = model;
    }

    public boolean isSinglePackage() {
        return isSinglePackage;
    }

    public void setSinglePackage(boolean singlePackage) {
        isSinglePackage = singlePackage;
    }

    public String getBaseViewParent() {
        return baseViewParent;
    }

    public void setBaseViewParent(String baseViewParent) {
        this.baseViewParent = baseViewParent;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public PsiDirectory getViewDir() {
        return viewDir;
    }

    public void setViewDir(PsiDirectory viewDir) {
        this.viewDir = viewDir;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public List<String> getView() {
        return view;
    }

    public void setView(List<String> view) {
        this.view = view;
    }

    public List<String> getPresenter() {
        return presenter;
    }

    public void setPresenter(List<String> presenter) {
        this.presenter = presenter;
    }

    public List<String> getModel() {
        return model;
    }

    public void setModel(List<String> model) {
        this.model = model;
    }

    public String getViewParent() {
        return viewParent;
    }

    public void setViewParent(String viewParent) {
        this.viewParent = viewParent;
    }

    public String getPresenterParent() {
        return presenterParent;
    }

    public void setPresenterParent(String presenterParent) {
        this.presenterParent = presenterParent;
    }

    public String getModelParent() {
        return modelParent;
    }

    public void setModelParent(String modelParent) {
        this.modelParent = modelParent;
    }
}
