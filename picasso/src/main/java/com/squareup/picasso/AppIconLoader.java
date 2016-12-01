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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;

/**
 * Optimal loading of Android app icons.
 */
public class AppIconLoader {

  private static volatile AppIconLoader singleton;

  public static AppIconLoader with(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("context == null");
    }
    if (singleton == null) {
      synchronized (AppIconLoader.class) {
        if (singleton == null) {
          singleton = new AppIconLoader(context.getApplicationContext());
        }
      }
    }
    return singleton;
  }

  private final PackageManager pm;
  private final int size;
  private final int dpi;
  private Bitmap defaultAppIcon;

  private AppIconLoader(Context context) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    size = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
    dpi = am.getLauncherLargeIconDensity();
    pm = context.getPackageManager();
  }

  /**
   * Get the default app icon
   *
   * @return The full resolution default icon for applications that don't specify an icon.
   * @see android.R.mipmap#sym_def_app_icon
   */
  public Bitmap getFullResDefaultActivityIcon() {
    if (defaultAppIcon == null) {
      Drawable drawable;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        drawable = Resources.getSystem().getDrawableForDensity(android.R.mipmap.sym_def_app_icon, dpi);
      } else {
        drawable = Resources.getSystem().getDrawable(android.R.drawable.sym_def_app_icon);
      }
      defaultAppIcon = drawableToBitmap(drawable);
    }
    return defaultAppIcon;
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link ActivityInfo}. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param info
   *     The {@link ActivityInfo} for the package.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getFullResIcon(ActivityInfo info) {
    try {
      Resources resources = pm.getResourcesForApplication(info.applicationInfo);
      if (resources != null) {
        int iconId = info.icon;
        if (iconId != 0) {
          return getFullResIcon(resources, iconId);
        }
      }
    } catch (PackageManager.NameNotFoundException ignored) {
    }
    return getFullResDefaultActivityIcon();
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link ApplicationInfo}. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param info
   *     The {@link ApplicationInfo} for the package.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getFullResIcon(ApplicationInfo info) {
    try {
      Resources resources = pm.getResourcesForApplication(info.packageName);
      if (resources != null) {
        int iconId = info.icon;
        if (iconId != 0) {
          return getFullResIcon(resources, iconId);
        }
      }
    } catch (PackageManager.NameNotFoundException ignored) {
    }
    return getFullResDefaultActivityIcon();
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link ComponentName}. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param cn
   *     The {@link ResolveInfo} for the package.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getFullResIcon(ComponentName cn) {
    Intent intent = new Intent();
    intent.setComponent(cn);
    ResolveInfo info = pm.resolveActivity(intent, 0);
    Bitmap bitmap = null;
    if (info != null) {
      bitmap = getFullResIcon(info);
    }
    if (bitmap == null || bitmap == defaultAppIcon) {
      // The ComponentName may have a null icon. Revert to the application icon.
      try {
        return getFullResIcon(cn.getPackageName());
      } catch (PackageManager.NameNotFoundException ignored) {
      }
    }
    return bitmap;
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link ResolveInfo}. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param info
   *     The {@link ResolveInfo} for the package.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getFullResIcon(ResolveInfo info) {
    return getFullResIcon(info.activityInfo);
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link Resources} and id.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the resource is not found then the default app icon is returned.</p>
   *
   * @param resources
   *     The {@link Resources} for the application.
   * @param iconId
   *     The icon id obtained from {@link ApplicationInfo#icon}.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see Resources#getDrawableForDensity(int, int)
   */
  public Bitmap getFullResIcon(Resources resources, int iconId) {
    final Drawable drawable;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        drawable = resources.getDrawableForDensity(iconId, dpi, null);
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        drawable = resources.getDrawableForDensity(iconId, dpi);
      } else {
        drawable = resources.getDrawable(iconId);
      }
    } catch (Resources.NotFoundException e) {
      return getFullResDefaultActivityIcon();
    }
    return drawableToBitmap(drawable);
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given package name. This will call
   * back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param packageName
   *     The package-name of the app.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @throws PackageManager.NameNotFoundException
   *     If the package is not installed
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getFullResIcon(String packageName) throws PackageManager.NameNotFoundException {
    return getFullResIcon(pm.getApplicationInfo(packageName, 0));
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given {@link ApplicationInfo}. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param info
   *     The {@link ApplicationInfo} for the package.
   * @return a {@link Bitmap} containing the app's icon. If the app does not have an icon, the
   * system default icon is returned.
   * @see #getFullResIcon(ApplicationInfo)
   * @see #getFullResDefaultActivityIcon()
   */
  public Bitmap getApkIcon(ApplicationInfo info) {
    Drawable icon = info.loadIcon(pm);
    Bitmap bmp = drawableToBitmap(icon);
    if (bmp == null) {
      return getFullResDefaultActivityIcon();
    }
    return Bitmap.createScaledBitmap(bmp, size, size, false);
  }

  /**
   * <p>Retrieve the current graphical icon associated with the given APK. This
   * will call back on the given PackageManager to load the icon from the application.</p>
   *
   * <p>The app icon is scaled to the size specified by {@link android.R.dimen#app_icon_size}.</p>
   *
   * <p>If the icon is {@code null} then the default app icon is returned using
   * {@link #getFullResDefaultActivityIcon()}</p>
   *
   * @param path
   *     The absolute path to an APK file.
   * @return a {@link Bitmap} containing the app's icon or {@code null}
   * if the package could not be parsed.
   */
  public Bitmap getApkIcon(String path) {
    PackageInfo packageInfo = pm.getPackageArchiveInfo(path, 0);
    if (packageInfo != null) {
      ApplicationInfo appInfo = packageInfo.applicationInfo;
      appInfo.sourceDir = path;
      appInfo.publicSourceDir = path;
      return getApkIcon(packageInfo.applicationInfo);
    }
    return null;
  }

  private Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    } else if (drawable instanceof PictureDrawable) {
      PictureDrawable pictureDrawable = (PictureDrawable) drawable;
      Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),
          pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawPicture(pictureDrawable.getPicture());
      return bitmap;
    }
    int width = drawable.getIntrinsicWidth();
    width = width > 0 ? width : 1;
    int height = drawable.getIntrinsicHeight();
    height = height > 0 ? height : 1;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

}
