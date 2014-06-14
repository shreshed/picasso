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
package it.sephiroth.android.library.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableList;

/** Immutable data about an image and the transformations that will be applied to it. */
public final class Request {
  private static final long TOO_LONG_LOG = TimeUnit.SECONDS.toNanos(5);

  /** A unique ID for the request. */
  int id;
  /** The time that the request was first submitted (in nanos). */
  long started;

  /**
   * The image URI.
   * <p>
   * This is mutually exclusive with {@link #resourceId}.
   */
  public final Uri uri;
  /**
   * The image resource ID.
   * <p>
   * This is mutually exclusive with {@link #uri}.
   */
  public final int resourceId;
  /** List of custom transformations to be applied after the built-in transformations. */
  public final List<Transformation> transformations;
  /** Target image width for resizing. */
  public final int targetWidth;
  /** Target image height for resizing. */
  public final int targetHeight;
  /**
   * If true the bitmap will be resized only if bigger than
   * {@link #targetWidth} or {@link #targetHeight}
   */
  public final boolean resizeOnlyIfBigger;

  /**
   * True if the final image should use the 'centerCrop' scale technique.
   * <p>
   * This is mutually exclusive with {@link #centerInside}.
   */
  public final boolean centerCrop;
  /**
   * True if the final image should use the 'centerInside' scale technique.
   * <p>
   * This is mutually exclusive with {@link #centerCrop}.
   */
  public final boolean centerInside;
  public final boolean resizeByMaxSide;
  /** Amount to rotate the image in degrees. */
  public final float rotationDegrees;
  /** Rotation pivot on the X axis. */
  public final float rotationPivotX;
  /** Rotation pivot on the Y axis. */
  public final float rotationPivotY;
  /** Whether or not {@link #rotationPivotX} and {@link #rotationPivotY} are set. */
  public final boolean hasRotationPivot;
  /** Target image config for decoding. */
  public final Bitmap.Config config;
  /** custom generator */
  public final Generator generator;
  /** cache to be used */
  public final Cache cache;
  public final Cache diskCache;
  /** Use this options instead of generating new ones every time */
  public final BitmapFactory.Options options;

  private Request(Uri uri, int resourceId, List<Transformation> transformations, int targetWidth,
      int targetHeight, boolean resizeOnlyIfBigger, boolean centerCrop, boolean centerInside,
      boolean resizeByMaxSide,
      float rotationDegrees, float rotationPivotX, float rotationPivotY, boolean hasRotationPivot,
      Bitmap.Config config, Generator generator, Cache cache, Cache diskCache, BitmapFactory.Options options) {
    this.uri = uri;
    this.resourceId = resourceId;
    if (transformations == null) {
      this.transformations = null;
    } else {
      this.transformations = unmodifiableList(transformations);
    }
    this.targetWidth = targetWidth;
    this.targetHeight = targetHeight;
    this.centerCrop = centerCrop;
    this.centerInside = centerInside;
    this.rotationDegrees = rotationDegrees;
    this.rotationPivotX = rotationPivotX;
    this.rotationPivotY = rotationPivotY;
    this.hasRotationPivot = hasRotationPivot;
    this.resizeOnlyIfBigger = resizeOnlyIfBigger;
    this.resizeByMaxSide = resizeByMaxSide;
    this.config = config;
    this.generator = generator;
    this.cache = cache;
    this.diskCache = diskCache;
	this.options = options;
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("Request{");
    if (resourceId > 0) {
      sb.append(resourceId);
    } else {
      sb.append(uri);
    }
    if (transformations != null && !transformations.isEmpty()) {
      for (Transformation transformation : transformations) {
        sb.append(' ').append(transformation.key());
      }
    }
    if (targetWidth > 0) {
      sb.append(" resize(").append(targetWidth).append(',').append(targetHeight).append(')');
    }
    if (centerCrop) {
      sb.append(" centerCrop");
    }
    if (centerInside) {
      sb.append(" centerInside");
    }
    if (rotationDegrees != 0) {
      sb.append(" rotation(").append(rotationDegrees);
      if (hasRotationPivot) {
        sb.append(" @ ").append(rotationPivotX).append(',').append(rotationPivotY);
      }
      sb.append(')');
    }
    if (config != null) {
      sb.append(' ').append(config);
    }
    sb.append('}');

    return sb.toString();
  }

  String logId() {
    long delta = System.nanoTime() - started;
    if (delta > TOO_LONG_LOG) {
      return plainId() + '+' + TimeUnit.NANOSECONDS.toSeconds(delta) + 's';
    }
    return plainId() + '+' + TimeUnit.NANOSECONDS.toMillis(delta) + "ms";
  }

  String plainId() {
    return "[R" + id + ']';
  }

  String getName() {
    if (uri != null) {
      return uri.getPath();
    }
    return Integer.toHexString(resourceId);
  }

  public boolean hasSize() {
    return targetWidth != 0;
  }

  public boolean hasGenerator() {
    return null != generator;
  }

  boolean needsTransformation() {
    return needsMatrixTransform() || hasCustomTransformations();
  }

  boolean needsMatrixTransform() {
    return targetWidth != 0 || rotationDegrees != 0;
  }

  boolean hasCustomTransformations() {
    return transformations != null;
  }

  public Builder buildUpon() {
    return new Builder(this);
  }

  /** Builder for creating {@link Request} instances. */
  public static final class Builder {
    private Uri uri;
    private int resourceId;
    private int targetWidth;
    private int targetHeight;
    private boolean centerCrop;
    private boolean centerInside;
    private boolean resizeByMaxSide;
    private float rotationDegrees;
    private float rotationPivotX;
    private float rotationPivotY;
    private boolean hasRotationPivot;
    private List<Transformation> transformations;
    private Bitmap.Config config;
    private boolean resizeOnlyIfBigger;
    private Generator generator;
    private Cache cache;
    private Cache diskCache;
	private BitmapFactory.Options options;

    /** Start building a request using the specified {@link Uri}. */
    public Builder(Uri uri) {
      setUri(uri);
    }

    /** Start building a request using the specified resource ID. */
    public Builder(int resourceId) {
      setResourceId(resourceId);
    }

    Builder(Uri uri, int resourceId) {
      this.uri = uri;
      this.resourceId = resourceId;
    }

    private Builder(Request request) {
      uri = request.uri;
      resourceId = request.resourceId;
      targetWidth = request.targetWidth;
      targetHeight = request.targetHeight;
      centerCrop = request.centerCrop;
      centerInside = request.centerInside;
      rotationDegrees = request.rotationDegrees;
      rotationPivotX = request.rotationPivotX;
      rotationPivotY = request.rotationPivotY;
      hasRotationPivot = request.hasRotationPivot;
      resizeOnlyIfBigger = request.resizeOnlyIfBigger;
      if (request.transformations != null) {
        transformations = new ArrayList<Transformation>(request.transformations);
      }
      config = request.config;
      cache = request.cache;
      diskCache = request.diskCache;
	  options = request.options;
    }

    boolean hasImage() {
      return uri != null || resourceId != 0;
    }

    boolean hasSize() {
      return targetWidth != 0;
    }

    /**
     * Set the target image Uri.
     * <p>
     * This will clear an image resource ID if one is set.
     */
    public Builder setUri(Uri uri) {
      if (uri == null) {
        throw new IllegalArgumentException("Image URI may not be null.");
      }
      this.uri = uri;
      this.resourceId = 0;
      return this;
    }

    /**
     * Set the target image resource ID.
     * <p>
     * This will clear an image Uri if one is set.
     */
    public Builder setResourceId(int resourceId) {
      if (resourceId == 0) {
        throw new IllegalArgumentException("Image resource ID may not be 0.");
      }
      this.resourceId = resourceId;
      this.uri = null;
      return this;
    }

    /** Resize the image to the specified size in pixels. */
    public Builder resize(int targetWidth, int targetHeight) {
      return resize(targetWidth, targetHeight, false);
    }

    /** Resize the image to the specified size in pixels. */
    public Builder resize(int targetWidth, int targetHeight, boolean onlyIfBigger) {
      if (targetWidth <= 0) {
        throw new IllegalArgumentException("Width must be positive number.");
      }
      if (targetHeight <= 0) {
        throw new IllegalArgumentException("Height must be positive number.");
      }
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
      this.resizeOnlyIfBigger = onlyIfBigger;
      return this;
    }

    /** Resizes the image by its max side */
    public Builder resizeByMaxSide() {
      if (centerInside || centerCrop) {
        throw new IllegalStateException("Resize by max side cannot be used with centerCrop or " +
            "centerInside");
      }
      this.resizeByMaxSide = true;
      return this;
    }

    /** Clear the center crop transformation flag, if set. */
    public Builder clearResizeByMaxSide() {
      resizeByMaxSide = false;
      return this;
    }

    /** Clear the resize transformation, if any. This will also clear center crop/inside if set. */
    public Builder clearResize() {
      targetWidth = 0;
      targetHeight = 0;
      centerCrop = false;
      centerInside = false;
      resizeOnlyIfBigger = false;
      return this;
    }

    /**
     * Crops an image inside of the bounds specified by {@link #resize(int, int, boolean)}
     * rather than distorting the aspect ratio. This cropping technique scales the image so
     * that it fills the requested bounds and then crops the extra.
     */
    public Builder centerCrop() {
      if (centerInside || resizeByMaxSide) {
        throw new IllegalStateException("Center crop can not be used after calling centerInside " +
            "or resizeByMaxSide");
      }
      centerCrop = true;
      return this;
    }

    /** Clear the center crop transformation flag, if set. */
    public Builder clearCenterCrop() {
      centerCrop = false;
      return this;
    }

    /**
     * Centers an image inside of the bounds specified by {@link #resize(int, int, boolean)}.
     * This scales the image so that both dimensions are equal to or less than the requested bounds.
     */
    public Builder centerInside() {
      if (centerCrop || resizeByMaxSide) {
        throw new IllegalStateException("Center inside can not be used after calling centerCrop" +
            " or resizeByMaxSide");
      }
      centerInside = true;
      return this;
    }

    /** Clear the center inside transformation flag, if set. */
    public Builder clearCenterInside() {
      centerInside = false;
      return this;
    }

    /** Rotate the image by the specified degrees. */
    public Builder rotate(float degrees) {
      rotationDegrees = degrees;
      return this;
    }

    /** Rotate the image by the specified degrees around a pivot point. */
    public Builder rotate(float degrees, float pivotX, float pivotY) {
      rotationDegrees = degrees;
      rotationPivotX = pivotX;
      rotationPivotY = pivotY;
      hasRotationPivot = true;
      return this;
    }

    /** Clear the rotation transformation, if any. */
    public Builder clearRotation() {
      rotationDegrees = 0;
      rotationPivotX = 0;
      rotationPivotY = 0;
      hasRotationPivot = false;
      return this;
    }

    /** Decode the image using the specified config. */
    public Builder config(Bitmap.Config config) {
      this.config = config;
      return this;
    }

	public Builder options(BitmapFactory.Options options) {
	  this.options = options;
      return this;
	}

    public Builder setGenerator(Generator generator) {
      this.generator = generator;
      return this;
    }

    public Builder setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

    public Builder setDiskCache(Cache cache) {
      this.diskCache = cache;
      return this;
    }

    public Cache getCache() {
      return this.cache;
    }

    public Cache getDiskCache() {
      return this.diskCache;
    }

    /**
     * Add a custom transformation to be applied to the image.
     * <p>
     * Custom transformations will always be run after the built-in transformations.
     */
    public Builder transform(Transformation transformation) {
      if (transformation == null) {
        throw new IllegalArgumentException("Transformation must not be null.");
      }
      if (transformations == null) {
        transformations = new ArrayList<Transformation>(2);
      }
      transformations.add(transformation);
      return this;
    }

    /** Create the immutable {@link Request} object. */
    public Request build() {
      if (centerInside && centerCrop) {
        throw new IllegalStateException("Center crop and center inside can not be used together.");
      }
      if (centerInside && resizeByMaxSide) {
        throw new IllegalStateException("Center Inside and resize by max side can not be used together.");
      }
      if (centerCrop && resizeByMaxSide) {
        throw new IllegalStateException("Center crop and resize by max side can not be used together.");
      }

      if (centerCrop && targetWidth == 0) {
        throw new IllegalStateException("Center crop requires calling resize.");
      }

      if (centerInside && targetWidth == 0) {
        throw new IllegalStateException("Center inside requires calling resize.");
      }

      if(resizeByMaxSide && targetWidth == 0) {
        throw new IllegalStateException("Resize by max side requires calling resize.");
      }

      return new Request(uri, resourceId, transformations, targetWidth, targetHeight,
          resizeOnlyIfBigger, centerCrop, centerInside,
          resizeByMaxSide,
          rotationDegrees, rotationPivotX, rotationPivotY, hasRotationPivot, config, generator,
          cache, diskCache, options);
    }
  }
}
