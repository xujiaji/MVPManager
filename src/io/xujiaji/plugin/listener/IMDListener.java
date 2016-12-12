package io.xujiaji.plugin.listener;

import io.xujiaji.plugin.model.MethodEntity;

/**
 * Created by jiana on 12/12/16.
 * InputMethodDialog listener
 * {@link io.xujiaji.plugin.widget.InputMethodDialog}
 */
public interface IMDListener {
    void complete(MethodEntity methodEntity);
}
