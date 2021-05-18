// INetSpeedInterface.aidl
package com.dede.nativetools.netspeed;

// Declare any non-default types here with import statements

interface INetSpeedInterface {
    /**
     * 设置刷新间隔
     */
    void setInterval(int interval);

    /**
     * 通知是否可以被点击
     */
    void setNotifyClickable(boolean clickable);

    /**
     * 兼容模式
     */
    void setLockHide(boolean hide);

    /**
     * 显示模式
     */
    void setMode(String mode);

    /**
     * 图标缩放
     */
    void setScale(float scale);
}