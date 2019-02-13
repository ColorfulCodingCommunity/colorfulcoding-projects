package com.colorfulcoding.lowresscanner;

/*
 *https://github.com/claywilkinson/arcore-android-sdk/blob/sceneform-samples/samples/cloud_anchor_java/app/src/main/java/com/google/ar/core/examples/java/cloudanchor/sceneform/PointCloudNode.java
 */

import android.content.Context;

import com.google.ar.core.PointCloud;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;

import java.nio.FloatBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Renders the ARCore point cloud as a Node. */
public class PointCloudNode extends Node {
    private long timestamp;
    private Vertex[] ptbuffer;
    private int[] indexbuffer;

    private CompletableFuture<Material> materialHolder;

    // This is the extent of the point
    private static final float POINT_DELTA = 0.003f;

    public PointCloudNode(Context context) {
        float r = 53 / 255f;
        float g = 174 / 255f;
        float b = 256 / 255f;

        Color color = new Color(r, g, b, 1.0f);
        materialHolder = MaterialFactory.makeOpaqueWithColor(context, color);
    }

    /**
     * Update the renderable for the point cloud. This creates a small quad for each feature point.
     *
     * @param cloud the ARCore point cloud.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void update(PointCloud cloud) {

        if (!isEnabled()) {
            return;
        }
        // If this is the same cloud as last time, skip it.  Also, if the material has not loaded yet,
        // skip this.
        if (this.timestamp != cloud.getTimestamp() && materialHolder.getNow(null) != null) {
            timestamp = cloud.getTimestamp();
            FloatBuffer buf = cloud.getPoints();

            // Point clouds are 4 values x,y,z and a confidence value.
            int numFeatures = buf.limit() / 4;

            // no features in the cloud
            if (numFeatures < 1) {
                setRenderable(null);
                return;
            }

            // Each feature point is drawn as a 4 sided pyramid.
            // 4 points per feature.
            int vertexPerFeature = 4;
            int numFaces = 4;
            int numPoints = numFeatures * vertexPerFeature;
            int vertexPerFace = 3;

            // draw a triangle per face (4 triangles) per feature.
            int indexPerFeature = numFaces * vertexPerFace;

            int numIndices = numFeatures * indexPerFeature;

            // allocate a buffer on the high water mark.
            if (ptbuffer == null || ptbuffer.length < numPoints) {
                ptbuffer = new Vertex[numPoints];
                indexbuffer = new int[numIndices];
            }

            Vector3 feature = new Vector3();
            Vector3[] p = {new Vector3(), new Vector3(), new Vector3(), new Vector3()};
            Vertex.UvCoordinate uv0 = new Vertex.UvCoordinate(0,0);
            Vector3 normal0 = new Vector3(0,0,1);
            Vector3 normal1 = new Vector3(.7f,0,.7f);
            Vector3 normal2 = new Vector3(-.7f,0,.7f);
            Vector3 normal3 = new Vector3(0,1,0);

            // Point cloud data is 4 floats per feature, {x,y,z,confidence}
            for (int i = 0; i < buf.limit() / 4; i++) {
                // feature point
                feature.x = buf.get(i * 4);
                feature.y = buf.get(i * 4 + 1);
                feature.z = buf.get(i * 4 + 2);

                // Top point
                p[0].x = feature.x;
                p[0].y = feature.y + POINT_DELTA;
                p[0].z = feature.z;

                // left pt
                p[1].x = feature.x - POINT_DELTA;
                p[1].y = feature.y;
                p[1].z = feature.z - POINT_DELTA;

                // front point
                p[2].x = feature.x;
                p[2].y = feature.y;
                p[2].z = feature.z + POINT_DELTA;

                // right pt
                p[3].x = feature.x + POINT_DELTA;
                p[3].y = feature.y;
                p[3].z = feature.z - POINT_DELTA;

                int vertexBase = i * vertexPerFeature;

                // Create the vertices.  Set the tangent and UV to quiet warnings about material requirements.
                ptbuffer[vertexBase] = Vertex.builder().setPosition(p[0])
                        .setUvCoordinate(uv0)
                        .setNormal(normal0)
                        .build();
                ptbuffer[vertexBase + 1] = Vertex.builder().setPosition(p[1])
                        .setUvCoordinate(uv0)
                        .setNormal(normal1)
                        .build();

                ptbuffer[vertexBase + 2] = Vertex.builder().setPosition(p[2])
                        .setUvCoordinate(uv0)
                        .setNormal(normal2)
                        .build();

                ptbuffer[vertexBase + 3] = Vertex.builder().setPosition(p[3])
                        .setUvCoordinate(uv0)
                        .setNormal(normal3)
                        .build();

                int featureBase = i * indexPerFeature;

                // The indices of the triangles need to be listed counter clockwise as
                // appears when facing the front side of the face.

                // left 0 1 2
                indexbuffer[featureBase + 2] = vertexBase;
                indexbuffer[featureBase] = vertexBase + 1;
                indexbuffer[featureBase + 1] = vertexBase + 2;

                // right  0 2 3
                indexbuffer[featureBase + 3] = vertexBase;
                indexbuffer[featureBase + 4] = vertexBase + 2;
                indexbuffer[featureBase + 5] = vertexBase + 3;

                // back  0 3 1
                indexbuffer[featureBase + 6] = vertexBase;
                indexbuffer[featureBase + 7] = vertexBase + 3;
                indexbuffer[featureBase + 8] = vertexBase + 1;

                // bottom  1,2,3
                indexbuffer[featureBase + 9] = vertexBase + 1;
                indexbuffer[featureBase + 10] = vertexBase + 2;
                indexbuffer[featureBase + 11] = vertexBase + 3;
            }

            RenderableDefinition.Submesh submesh =
                    RenderableDefinition.Submesh.builder()
                            .setName("pointcloud")
                            .setMaterial(materialHolder.getNow(null))
                            .setTriangleIndices(
                                    IntStream.of(indexbuffer)
                                            .limit(numIndices)
                                            .boxed()
                                            .collect(Collectors.toList()))
                            .build();

            RenderableDefinition def =
                    RenderableDefinition.builder()
                            .setVertices(Stream.of(ptbuffer).limit(numPoints).collect(Collectors.toList()))
                            .setSubmeshes(Stream.of(submesh).collect(Collectors.toList()))
                            .build();

            ModelRenderable.builder().setSource(def).build().thenAccept(renderable -> {
                renderable.setShadowCaster(false);setRenderable(renderable);});
        }
    }
}