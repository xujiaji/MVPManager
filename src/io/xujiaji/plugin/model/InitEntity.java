package io.xujiaji.plugin.model;

import com.intellij.psi.PsiDirectory;

/**
 * Created by jiana on 11/12/16.
 */
public class InitEntity {
    private PsiDirectory[] psiDirectories;

    public InitEntity(PsiDirectory[] psiDirectories) {
        this.psiDirectories = psiDirectories;
    }

    public PsiDirectory[] getPsiDirectories() {
        return psiDirectories;
    }

    public void setPsiDirectories(PsiDirectory[] psiDirectories) {
        this.psiDirectories = psiDirectories;
    }
}
