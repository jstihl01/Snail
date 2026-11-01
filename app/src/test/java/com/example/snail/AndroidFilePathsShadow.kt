package com.example.snail

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/** FileProvider assumes Android's '/' separator. Robolectric uses the host filesystem on Windows.
 * Normalize only that comparison; real provider metadata, URI generation, containment checks and
 * opening the shared file still run unchanged.
 */
@Implements(className = "androidx.core.content.FileProvider\$SimplePathStrategy", isInAndroidSdk = false)
class AndroidFilePathsShadow {
    @Implementation
    protected fun belongsToRoot(filePath: String, rootPath: String): Boolean =
        filePath.replace('\\', '/').trimEnd('/').startsWith(rootPath.replace('\\', '/').trimEnd('/') + '/')
}
