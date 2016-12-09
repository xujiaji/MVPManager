package io.xujiaji.plugin.model;

import java.util.List;

/**
 * Created by jiana on 05/12/16.
 */
public class EditEntity {
    private String contractName;
    private String viewParent;
    private String presenterParent;
    private String modelParent;
    private List<String> view;
    private List<String> presenter;
    private List<String> model;

    public EditEntity(String contractName) {
        this.contractName = contractName;
    }

    public EditEntity(List<String> view, List<String> presenter, List<String> model) {
        this.view = view;
        this.presenter = presenter;
        this.model = model;
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
