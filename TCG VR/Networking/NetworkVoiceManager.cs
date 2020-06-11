using Photon.Pun;
using Photon.Voice.Unity;
using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[RequireComponent(typeof(VoiceConnection))]
public class NetworkVoiceManager : MonoBehaviour
{
    public static event Action<bool> OnMuteSwitch;

    public static bool isMute = false;

    public Transform remoteVoiceParent;
    public AudioClip muteUnmuteSound;

    private VoiceConnection voiceConnection;
    private GeneralSoundSystemBehavior generalSoundSystemBehavior;
    
    void Awake()
    {
        generalSoundSystemBehavior = GeneralSoundSystemBehavior.Instance;
        voiceConnection = GetComponent<VoiceConnection>();

        voiceConnection.Client.NickName = GeneralSettings.myNickname;
    }

    private void OnEnable()
    {
        voiceConnection.SpeakerLinked += this.OnSpeakerCreated;
        ControlsFacade.OnMuteUnmute += this.SwitchMute;
    }

    private void OnDisable()
    {
        voiceConnection.SpeakerLinked -= this.OnSpeakerCreated;
        ControlsFacade.OnMuteUnmute -= this.SwitchMute;
    }

    private void OnSpeakerCreated(Speaker speaker)
    {
        speaker.transform.SetParent(this.remoteVoiceParent);
        speaker.OnRemoteVoiceRemoveAction += OnRemoteVoiceRemove;
    }

    private void OnRemoteVoiceRemove(Speaker speaker)
    {
        if(speaker != null)
        {
            Destroy(speaker.gameObject);
        }
    }

    private void SwitchMute()
    {
        isMute = !isMute;

        GetComponent<Recorder>().IsRecording = !isMute;
        generalSoundSystemBehavior.PlaySound(muteUnmuteSound);
        OnMuteSwitch?.Invoke(isMute);

    }
}
