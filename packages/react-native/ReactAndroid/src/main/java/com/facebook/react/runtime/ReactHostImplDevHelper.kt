/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.runtime

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.facebook.react.bridge.JSBundleLoader
import com.facebook.react.bridge.JavaScriptExecutorFactory
import com.facebook.react.bridge.ReactContext
import com.facebook.react.common.annotations.FrameworkAPI
import com.facebook.react.common.annotations.UnstableReactNativeAPI
import com.facebook.react.devsupport.ReactInstanceDevHelper
import com.facebook.react.interfaces.TaskInterface
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * Implementation of [ReactInstanceDevHelper] for [ReactHostImpl].
 *
 * This allows [BridgelessDevSupportHelper] and other classes inside the .devsupport package to
 * communicate with the Bridgeless infrastructure without exposing public APIs.
 */
@UnstableReactNativeAPI
@OptIn(FrameworkAPI::class)
internal class ReactHostImplDevHelper(private val delegate: ReactHostImpl) :
    ReactInstanceDevHelper {

  override val currentActivity: Activity?
    get() = delegate.lastUsedActivity

  override val javaScriptExecutorFactory: JavaScriptExecutorFactory
    get() = throw IllegalStateException("Not implemented for bridgeless mode")

  override val currentReactContext: ReactContext?
    get() = delegate.currentReactContext

  override fun onJSBundleLoadedFromServer() {
    // Not implemented, only referenced by BridgeDevSupportManager
  }

  override fun toggleElementInspector() {
    val reactContext = delegate.currentReactContext
    reactContext
        ?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        ?.emit("toggleElementInspector", null)
  }

  override fun createRootView(appKey: String): View? {
    val currentActivity = currentActivity
    if (currentActivity != null && !delegate.isSurfaceWithModuleNameAttached(appKey)) {
      val reactSurface = ReactSurfaceImpl.createWithView(currentActivity, appKey, Bundle())
      reactSurface.attach(delegate)
      reactSurface.start()

      return reactSurface.view
    }
    return null
  }

  override fun destroyRootView(rootView: View) {
    // Not implemented, only referenced by BridgeDevSupportManager
  }

  override fun reload(reason: String) {
    delegate.reload(reason)
  }

  override fun loadBundle(bundleLoader: JSBundleLoader): TaskInterface<Boolean> =
      delegate.loadBundle(bundleLoader)
}
