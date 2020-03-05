using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Linq;

[RequireComponent(typeof(MeshFilter))]
[RequireComponent(typeof(MeshRenderer))]
public class MeshGenerator : MonoBehaviour
{
    public TextAsset faceData;

    private FaceAnimExportEntity faceAnim;
    private Mesh mesh;

    private int currentIdx = 0;

    private void Start()
    {
        faceAnim = JsonUtility.FromJson<FaceAnimExportEntity>(faceData.text);

        mesh = new Mesh();
        GetComponent<MeshFilter>().mesh = mesh;
        Debug.Log("Got " + faceAnim.keyframes.Count + " frames.");
        Debug.Log("The face has " + faceAnim.keyframes[0].vertices.Count + " vertices");
    }

    [ContextMenu("Create Mesh")]
    public void CreateMesh()
    {
        var faceAnim = JsonUtility.FromJson<FaceAnimExportEntity>(faceData.text);

        this.transform.position = new Vector3(faceAnim.keyframes[0].position[0], faceAnim.keyframes[0].position[1], faceAnim.keyframes[0].position[2]);
        this.transform.rotation = new Quaternion(faceAnim.keyframes[0].rotation[0], faceAnim.keyframes[0].rotation[1], faceAnim.keyframes[0].rotation[2], faceAnim.keyframes[0].rotation[3]);

        mesh.vertices = faceAnim.keyframes[0].vertices.Select(v => v.ToVector3()).ToArray();
        mesh.uv = faceAnim.uvs.Select(uv => uv.ToVector2()).ToArray();
        mesh.triangles = faceAnim.indices.ToArray();

        mesh.RecalculateNormals();
    }

    [ContextMenu("Start Animation")]
    public void StartAnim()
    {
        StartCoroutine(AnimationRoutine());
    }

    private IEnumerator AnimationRoutine()
    {
        while (true)
        {
            NextFrame();
            yield return new WaitForSeconds(1 / 24f);
        }
    }


    public void NextFrame()
    {
        currentIdx++;
        if (currentIdx >= faceAnim.keyframes.Count)
        {
            currentIdx = 0;
        }

        this.transform.position = new Vector3(faceAnim.keyframes[currentIdx].position[0], faceAnim.keyframes[currentIdx].position[1], faceAnim.keyframes[currentIdx].position[2]);
        this.transform.rotation = new Quaternion(faceAnim.keyframes[currentIdx].rotation[0], faceAnim.keyframes[currentIdx].rotation[1], faceAnim.keyframes[currentIdx].rotation[2], faceAnim.keyframes[currentIdx].rotation[3]);

        mesh.vertices = faceAnim.keyframes[currentIdx].vertices.Select(v => v.ToVector3()).ToArray();

    }
}
