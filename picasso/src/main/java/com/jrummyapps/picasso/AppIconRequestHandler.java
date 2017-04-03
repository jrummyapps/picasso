/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrummyapps.picasso;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;

class AppIconRequestHandler extends RequestHandler {

  private final AppIconLoader iconLoader;

  AppIconRequestHandler(Context context) {
    iconLoader = AppIconLoader.with(context);
  }

  @Override public boolean canHandleRequest(Request data) {
    String scheme = data.uri.getScheme();
    return Picasso.SCHEME_PACKAGE.equals(scheme)
        || Picasso.SCHEME_COMPONENT.equals(scheme)
        || Picasso.SCHEME_APK.equals(scheme);
  }

  @Nullable @Override public Result load(Request request, int networkPolicy) throws IOException {
    switch (request.uri.getScheme()) {
      case Picasso.SCHEME_PACKAGE:
        String packageName = request.uri.getEncodedSchemeSpecificPart();
        try {
          Bitmap bitmap = iconLoader.getFullResIcon(packageName);
          if (bitmap != null) {
            return new Result(bitmap, Picasso.LoadedFrom.DISK);
          }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        break;
      case Picasso.SCHEME_COMPONENT:
        String[] parts = request.uri.getEncodedSchemeSpecificPart().split("/");
        if (parts.length == 2) {
          ComponentName componentName = new ComponentName(parts[0], parts[1]);
          Bitmap bitmap = iconLoader.getFullResIcon(componentName);
          if (bitmap != null) {
            return new Result(bitmap, Picasso.LoadedFrom.DISK);
          }
        }
        break;
      case Picasso.SCHEME_APK:
        String path = request.uri.getEncodedSchemeSpecificPart();
        Bitmap bitmap = iconLoader.getApkIcon(path);
        if (bitmap != null) {
          return new Result(bitmap, Picasso.LoadedFrom.DISK);
        }
        break;
      default:
        break;
    }
    return null;
  }

}
