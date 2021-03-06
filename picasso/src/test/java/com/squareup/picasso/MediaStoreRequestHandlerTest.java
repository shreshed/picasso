package it.sephiroth.android.library.picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.squareup.picasso.Request;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static it.sephiroth.android.library.picasso.MediaStoreRequestHandler.PicassoKind.FULL;
import static it.sephiroth.android.library.picasso.MediaStoreRequestHandler.PicassoKind.MICRO;
import static it.sephiroth.android.library.picasso.MediaStoreRequestHandler.PicassoKind.MINI;
import static it.sephiroth.android.library.picasso.MediaStoreRequestHandler.getPicassoKind;
import static it.sephiroth.android.library.picasso.TestUtils.mockAction;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE,
    shadows = { Shadows.ShadowVideoThumbnails.class, Shadows.ShadowImageThumbnails.class })
public class MediaStoreRequestHandlerTest {

  @Mock Context context;

  @Before public void setUp() {
    initMocks(this);
  }

  @Test public void decodesVideoThumbnailWithVideoMimeType() throws Exception {
    Request request = new Request.Builder(TestUtils.MEDIA_STORE_CONTENT_1_URL, 0).resize(100, 100).build();
    Action action = TestUtils.mockAction(TestUtils.MEDIA_STORE_CONTENT_KEY_1, request);
    MediaStoreRequestHandler requestHandler = create("video/", action);
    Bitmap result = requestHandler.load(action.getRequest()).getBitmap();
    assertThat(result).isEqualTo(TestUtils.VIDEO_THUMBNAIL_1);
  }

  @Test public void decodesImageThumbnailWithImageMimeType() throws Exception {
    Request request = new Request.Builder(TestUtils.MEDIA_STORE_CONTENT_1_URL, 0).resize(100, 100).build();
    Action action = TestUtils.mockAction(TestUtils.MEDIA_STORE_CONTENT_KEY_1, request);
    MediaStoreRequestHandler requestHandler = create("image/png", action);
    Bitmap result = requestHandler.load(action.getRequest()).getBitmap();
    assertThat(result).isEqualTo(TestUtils.IMAGE_THUMBNAIL_1);
  }

  @Test public void getPicassoKindMicro() throws Exception {
    assertThat(getPicassoKind(96, 96)).isEqualTo(MICRO);
    assertThat(getPicassoKind(95, 95)).isEqualTo(MICRO);
  }

  @Test public void getPicassoKindMini() throws Exception {
    assertThat(getPicassoKind(512, 384)).isEqualTo(MINI);
    assertThat(getPicassoKind(100, 100)).isEqualTo(MINI);
  }

  @Test public void getPicassoKindFull() throws Exception {
    assertThat(getPicassoKind(513, 385)).isEqualTo(FULL);
    assertThat(getPicassoKind(1000, 1000)).isEqualTo(FULL);
    assertThat(getPicassoKind(1000, 384)).isEqualTo(FULL);
    assertThat(getPicassoKind(1000, 96)).isEqualTo(FULL);
    assertThat(getPicassoKind(96, 1000)).isEqualTo(FULL);
  }

  private MediaStoreRequestHandler create(String mimeType, Action action) {
    ContentResolver contentResolver = mock(ContentResolver.class);
    when(contentResolver.getType(any(Uri.class))).thenReturn(mimeType);
    return create(contentResolver, action);
  }

  private MediaStoreRequestHandler create(ContentResolver contentResolver, Action action) {
    when(context.getContentResolver()).thenReturn(contentResolver);
    return new MediaStoreRequestHandler(context);
  }
}
