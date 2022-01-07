// INetSpeedInterface.aidl
package com.dede.nativetools.netspeed;

import com.dede.nativetools.netspeed.NetSpeedConfiguration;

// Declare any non-default types here with import statements

interface INetSpeedInterface {

    /**
     * 更新配置
     */
    void updateConfiguration(in NetSpeedConfiguration configuration);

}