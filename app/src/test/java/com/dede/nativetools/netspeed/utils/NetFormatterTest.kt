package com.dede.nativetools.netspeed.utils

import com.dede.nativetools.util.splicing
import org.junit.Assert
import org.junit.Test

/**
 * Created by shhu on 2022/7/14 14:23. Test for [NetFormatter]
 * @since 2022/7/14
 */
internal class NetFormatterTest {

    @Test
    fun format() {
        Assert.assertEquals(
            "512B/s",
            NetFormatter.format(
                    512,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_BYTE
                )
                .splicing()
        )

        Assert.assertEquals(
            "10KB/s",
            NetFormatter.format(
                    10 * 1024,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_BYTE
                )
                .splicing()
        )

        Assert.assertEquals(
            "0.1KB/s",
            NetFormatter.format(
                    102,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_KB
                )
                .splicing()
        )

        Assert.assertEquals(
            "0.5KB/s",
            NetFormatter.format(
                    512,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_KB
                )
                .splicing()
        )

        Assert.assertEquals(
            "10KB/s",
            NetFormatter.format(
                    10 * 1024,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_KB
                )
                .splicing()
        )

        Assert.assertEquals(
            "0MB/s",
            NetFormatter.format(
                    512,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_MB
                )
                .splicing()
        )

        Assert.assertEquals(
            "0.01MB/s",
            NetFormatter.format(
                    10 * 1024,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_MB
                )
                .splicing()
        )

        Assert.assertEquals(
            "10MB/s",
            NetFormatter.format(
                    10 * 1024 * 1024,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_MB
                )
                .splicing()
        )

        Assert.assertEquals(
            "10GB/s",
            NetFormatter.format(
                    10L * 1024 * 1024 * 1024,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT,
                    NetFormatter.MIN_UNIT_MB
                )
                .splicing()
        )
    }
}
