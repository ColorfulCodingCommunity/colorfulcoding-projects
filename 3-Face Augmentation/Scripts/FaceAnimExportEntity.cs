using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class FaceAnimExportEntity
{
    [SerializeField]
    public List<AnimKeyframe> keyframes;

    [SerializeField]
    public List<MyVec2> uvs;
    [SerializeField]
    public List<int> indices;
}
