package io.xujiaji.plugin.util;

import com.intellij.openapi.ui.Messages;

/**
 * Created by jiana on 11/12/16.
 */
public class MsgUtil {
    public static void showInfo(String info) {
        Messages.showMessageDialog(info, "Information", Messages.getInformationIcon());
    }


    public static void msgContractNameNull()
    {
        showInfo("Please input contract name!");
    }
}
