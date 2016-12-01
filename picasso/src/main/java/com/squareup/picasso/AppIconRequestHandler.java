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

package com.squareup.picasso;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.IOException;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;
import static com.squareup.picasso.Picasso.SCHEME_APK;
import static com.squareup.picasso.Picasso.SCHEME_COMPONENT;
import static com.squareup.picasso.Picasso.SCHEME_PACKAGE;

class AppIconRequestHandler extends RequestHandler {

  private final AppIconLoader iconLoader;

  AppIconRequestHandler(Context context) {
    iconLoader = AppIconLoader.with(context);
  }

  @Override public boolean canHandleRequest(Request data) {
    String scheme = data.uri.getScheme();
    return SCHEME_PACKAGE.equals(scheme) || SCHEME_COMPONENT.equals(scheme) || SCHEME_APK.equals(scheme);
  }

  @Nullable @Override public Result load(Request request, int networkPolicy) throws IOException {
    switch (request.uri.getScheme()) {
      case SCHEME_PACKAGE:
        String packageName = request.uri.getEncodedSchemeSpecificPart();
        try {
          Bitmap bitmap = iconLoader.getFullResIcon(packageName);
          if (bitmap != null) {
            return new Result(bitmap, DISK);
          }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        break;
      case SCHEME_COMPONENT:
        String[] parts = request.uri.getEncodedSchemeSpecificPart().split("/");
        if (parts.length == 2) {
          ComponentName componentName = new ComponentName(parts[0], parts[1]);
          Bitmap bitmap = iconLoader.getFullResIcon(componentName);
          if (bitmap != null) {
            return new Result(bitmap, DISK);
          }
        }
        break;
      case SCHEME_APK:
        String path = request.uri.getEncodedSchemeSpecificPart();
        Bitmap bitmap = iconLoader.getApkIcon(path);
        if (bitmap != null) {
          return new Result(bitmap, DISK);
        }
        break;
    }
    return null;
  }

}
