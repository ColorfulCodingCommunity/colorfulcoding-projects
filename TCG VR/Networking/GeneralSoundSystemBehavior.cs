using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GeneralSoundSystemBehavior : MonoBehaviour
{
    #region SINGLETON
    private static GeneralSoundSystemBehavior _instance;
    public static GeneralSoundSystemBehavior Instance
    {
        get
        {
            if (_instance == null)
            {
                _instance = GameObject.FindObjectOfType<GeneralSoundSystemBehavior>();

                if (_instance == null)
                {
                    Debug.LogError("No GeneralSoundSystem found in scene!");
                }
            }

            return _instance;
        }
    }
    #endregion
    void Update()
    {
        transform.position = Camera.main.transform.position;
    }

    public void PlaySound(AudioClip clip)
    {
        var audioSource = GetComponent<AudioSource>();
        audioSource.clip = clip;
        audioSource.Play();
    }
}
