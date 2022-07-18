# Third party Android OS Internal API

Dependency mode `compileOnly`, Only provide the API.

## MIUI

```java
package android.app;

public class Notification {
    public MiuiNotification extraNotification;
}

public class MiuiNotification {
    // provide
    public MiuiNotification setCustomizedIcon(boolean customizedIcon) {
    }
}
```

## Flyme

[Meizu Open Platform](http://open-wiki.flyme.cn/doc-wiki/index#id?76)

```java
package android.app;

public class Notification {
    public static class Builder {
        public NotificationBuilderExt mFlymeNotificationBuilder;
    }
}

public class NotificationBuilderExt {
    // provide
    public void setInternalApp(int internalApp) {
    }
}
```