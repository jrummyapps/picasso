/*
 * Copyright (C) 2013 Square, Inc.
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

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.jrummyapps.picasso.Picasso.LoadedFrom.MEMORY;
import static com.jrummyapps.picasso.Picasso.RequestTransformer.IDENTITY;
import static com.jrummyapps.picasso.TestUtils.makeBitmap;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ImageViewActionTest {

  @Test(expected = AssertionError.class)
  public void throwsErrorWithNullResult() throws Exception {
    ImageViewAction action =
        new ImageViewAction(mock(Picasso.class), TestUtils.mockImageViewTarget(), null, 0, 0, 0,
            null, TestUtils.URI_KEY_1, null, null, false);
    action.complete(null, MEMORY);
  }

  @Test
  public void returnsIfTargetIsNullOnComplete() throws Exception {
    Bitmap bitmap = TestUtils.makeBitmap();
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, 0, null, TestUtils.URI_KEY_1, null,
            callback, false);
    request.target.clear();
    request.complete(bitmap, MEMORY);
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void returnsIfTargetIsNullOnError() throws Exception {
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, 0, null, TestUtils.URI_KEY_1, null,
            callback, false);
    request.target.clear();
    request.error(new RuntimeException());
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void invokesTargetAndCallbackSuccessIfTargetIsNotNull() throws Exception {
    Bitmap bitmap = TestUtils.makeBitmap();
    Picasso picasso =
        new Picasso(RuntimeEnvironment.application, mock(Dispatcher.class), Cache.NONE, null, IDENTITY,
            null, mock(Stats.class), Bitmap.Config.ARGB_8888, false, false);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, 0, null, TestUtils.URI_KEY_1, null,
            callback, false);
    request.complete(bitmap, MEMORY);
    verify(target).setImageDrawable(any(PicassoDrawable.class));
    verify(callback).onSuccess();
  }

  @Test
  public void invokesTargetAndCallbackErrorIfTargetIsNotNullWithErrorResourceId() throws Exception {
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, 0, TestUtils.RESOURCE_ID_1, null, null, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageResource(TestUtils.RESOURCE_ID_1);
    verify(callback).onError(e);
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorResourceId() throws Exception {
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, 0, TestUtils.RESOURCE_ID_1, null, null, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageResource(TestUtils.RESOURCE_ID_1);
    verify(callback).onError(e);
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorDrawable() throws Exception {
    Drawable errorDrawable = mock(Drawable.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, 0, 0, errorDrawable, TestUtils.URI_KEY_1, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageDrawable(errorDrawable);
    verify(callback).onError(e);
  }

  @Test
  public void clearsCallbackOnCancel() throws Exception {
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, 0, null, TestUtils.URI_KEY_1, null,
            callback, false);
    request.cancel();
    assertThat(request.callback).isNull();
  }

  @Test
  public void stopPlaceholderAnimationOnError() throws Exception {
    Picasso picasso = mock(Picasso.class);
    AnimationDrawable placeholder = mock(AnimationDrawable.class);
    ImageView target = TestUtils.mockImageViewTarget();
    when(target.getDrawable()).thenReturn(placeholder);
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, 0, null, TestUtils.URI_KEY_1, null,
            null, false);
    request.error(new RuntimeException());
    verify(placeholder).stop();
  }
}
