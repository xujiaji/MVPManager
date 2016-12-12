package io.xujiaji.plugin.listener;

import io.xujiaji.plugin.model.MethodEntity;

/**
 * Created by jiana on 12/12/16.
 */
public interface ChangeListener {
    void add(int type, MethodEntity methodEntity);
    void del(int type, MethodEntity methodEntity);
}
