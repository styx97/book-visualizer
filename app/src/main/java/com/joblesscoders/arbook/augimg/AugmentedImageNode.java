package com.joblesscoders.arbook.augimg;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.joblesscoders.arbook.pojo.Contents;

import java.util.concurrent.CompletableFuture;

public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;
  private String modelname;
  private Context context;
  private ArFragment arFragment;
  private Contents contents;

  private CompletableFuture<ModelRenderable> model;

  public AugmentedImageNode(Context context, ArFragment arFragment, String modelname, Contents contents) {
    this.modelname = modelname;
    this.context = context;
    this.contents = contents;
    this.arFragment = arFragment;
    // Upon construction, start loading the models for the corners of the frame.
    if (model == null) {
      model =
          ModelRenderable.builder()
              .setSource(context, Uri.parse(modelname))
              .build();
    }
  }

  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    //Toast.makeText(context, "loading", Toast.LENGTH_SHORT).show();
    this.image = image;

    // If any of the models are not loaded, then recurse when all are loaded.
    if (!model.isDone()) {
      CompletableFuture.allOf(model)
          .thenAccept((Void aVoid) -> setImage(image))
          .exceptionally(
              throwable -> {
                Log.e(TAG, "Exception loading", throwable);
                return null;
              });
    }

    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    // Make the 4 corner nodes.
    Vector3 localPosition = new Vector3();
    TransformableNode cornerNode;

    // Upper left corner.
    localPosition.set(0, 0.01f, 0);

    cornerNode = new TransformableNode(arFragment.getTransformationSystem());

      float percentage[] = contents.getPercentage();
      cornerNode.getScaleController().setMaxScale(percentage[1]*contents.getScale()[1]);
      cornerNode.getScaleController().setMinScale(percentage[1]*contents.getScale()[0]);


    cornerNode.setParent(this);
    cornerNode.setLocalPosition(localPosition);
    cornerNode.setRenderable(model.getNow(null));
    cornerNode.select();
  }

  public AugmentedImage getImage() {
    return image;
  }
}
