using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class AnimKeyframe
{
    [SerializeField]
    public long timestamp;
    [SerializeField]
    public float[] position;
    [SerializeField]
    public float[] rotation;

    [SerializeField]
    public List<MyVec3> vertices;
}
