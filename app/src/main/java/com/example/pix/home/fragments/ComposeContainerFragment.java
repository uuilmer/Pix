package com.example.pix.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pix.R;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.util.Collection;

public class ComposeContainerFragment extends Fragment {

    private ModelRenderable modelRenderable;
    private Texture texture;
    private boolean isAdded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ComposeFragment composeFrag = (ComposeFragment) this.getChildFragmentManager().findFragmentById(R.id.arFragment);

//        // Define the fox mask
//        ModelRenderable.builder()
//                .setSource(getContext(), R.raw.gas_mask)
//                .build()
//                .thenAccept(renderable ->
//                {
//                    this.modelRenderable = renderable;
//                    modelRenderable.setShadowCaster(false);
//                    modelRenderable.setShadowReceiver(false);
//                });
//        // Define the face mesh
//        Texture.builder()
//                .setSource(getContext(), R.drawable.fox_face_mesh_texture)
//                .build()
//                .thenAccept(texture1 -> this.texture = texture1);
//
//        composeFrag.getArSceneView().setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
//
//        composeFrag.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
//
//            if(modelRenderable == null || texture == null){
//                return;
//            }
//            Frame frame = composeFrag.getArSceneView().getArFrame();
//
//            Collection<AugmentedFace> faces = frame.getUpdatedTrackables(AugmentedFace.class);
//            for(AugmentedFace face : faces){
//                if(isAdded)
//                    return;
//                AugmentedFaceNode augmentedFaceNode = new AugmentedFaceNode(face);
//                augmentedFaceNode.setParent(composeFrag.getArSceneView().getScene());
//                augmentedFaceNode.setFaceRegionsRenderable(modelRenderable);
//                augmentedFaceNode.setFaceMeshTexture(texture);
//
//                isAdded = true;
//            }
//        });
    }
}
