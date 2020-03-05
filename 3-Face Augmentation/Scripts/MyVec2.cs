using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class MyVec2
{
    public float x;
    public float y;

    public MyVec2(Vector2 v)
    {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2 ToVector2()
    {
        return new Vector2(x, y);
    }
}
