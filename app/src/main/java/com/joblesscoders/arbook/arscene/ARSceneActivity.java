package com.joblesscoders.arbook.arscene;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.joblesscoders.arbook.R;
import com.joblesscoders.arbook.pojo.Contents;

public class ARSceneActivity extends AppCompatActivity {

    private static final String TAG = ARSceneActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private Contents content;
    private CustomARFragment arFragment;
    private ModelRenderable andyRenderable;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_arscene);
        content = getIntent().getParcelableExtra("content");

        arFragment = (CustomARFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/"+content.getLink().toLowerCase()+".sfb"))
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                   /* switch (content.getTitle().toLowerCase())
                    {
                        case "skull":
                            andy.getScaleController().setMaxScale(0.3f);
                            andy.getScaleController().setMinScale(0.1f);
                            break;
                        case "beagle":
                            andy.getScaleController().setMaxScale(0.4f);
                            andy.getScaleController().setMinScale(0.3f);
                            break;
                        case "crocodile":
                            andy.getScaleController().setMaxScale(1.0f);
                            andy.getScaleController().setMinScale(0.7f);
                            break;
                        case "brain":
                            andy.getScaleController().setMaxScale(0.3f);
                            andy.getScaleController().setMinScale(0.1f);
                            break;
                        case "horse":
                            andy.getScaleController().setMaxScale(0.8f);
                            andy.getScaleController().setMinScale(0.6f);
                            break;
                        case "heart":
                            andy.getScaleController().setMaxScale(0.3f);
                            andy.getScaleController().setMinScale(0.1f);
                            break;
                    }*/
                    float percentage[] = content.getPercentage();
                    andy.getScaleController().setMaxScale(percentage[2]*content.getScale()[1]);
                    andy.getScaleController().setMinScale(percentage[2]*content.getScale()[0]);

                    //andy.setLocalScale(new Vector3(0.02f,0.02f,0.02f));
                    //andy.setLocalScale(new Vector3(content.getScale()[0],content.getScale()[1],content.getScale()[3]));
                    //andy.setLocalPosition(new Vector3(0,0.25f,0));
                    //andy.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 180f));
                    andy.select();
                });
    }
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
