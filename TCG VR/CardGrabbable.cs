﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;


namespace com.colorfulcoding.customVRLogic
{
    public class CardGrabbable : OVRGrabbable
    {
        private CardController cardController;

        protected override void Start()
        {
            base.Start();
            cardController = GetComponent<CardController>();
        }

        public override void GrabEnd(Vector3 linearVelocity, Vector3 angularVelocity)
        {
            base.GrabEnd(linearVelocity, angularVelocity);
            GetComponent<CardController>().OnGrabEnd();
        }

    }
}