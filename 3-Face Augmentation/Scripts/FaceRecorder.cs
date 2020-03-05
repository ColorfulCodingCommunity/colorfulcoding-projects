using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization.Formatters.Binary;
using UnityEngine;
using UnityEngine.Android;
using UnityEngine.UI;
using UnityEngine.XR.ARFoundation;
using UnityEngine.XR.ARSubsystems;

[RequireComponent(typeof(ARFaceManager))]
public class FaceRecorder : MonoBehaviour
{
    [SerializeField]
    private Text logText;
    [SerializeField]
    private GameObject eyePrefab;
    [SerializeField]
    private Button recordingButton;

    private ARFaceManager m_ARFaceManager;
    private AudioSource m_audioSource;
    private AudioListener m_audioListener;
    private Dictionary<TrackableId, FaceAnimExportEntity> faceAnimExports;
    private TrackableId mainId;

    private bool isRecording = false;

    private GameObject dialog = null;
    void Start()
    {
        m_ARFaceManager = GetComponent<ARFaceManager>();
        m_audioListener = GetComponent<AudioListener>();
        m_audioSource = GetComponent<AudioSource>();

        faceAnimExports = new Dictionary<TrackableId, FaceAnimExportEntity>();

#if PLATFORM_ANDROID
        if (!Permission.HasUserAuthorizedPermission(Permission.Microphone))
        {
            Permission.RequestUserPermission(Permission.Microphone);
            dialog = new GameObject();
        }
#endif
    }

    private void OnEnable()
    {
        recordingButton.onClick.AddListener(OnSwitchRecord);
    }
    private void OnDisable()
    {
        recordingButton.onClick.RemoveListener(OnSwitchRecord);
    }

    private void OnGUI()
    {
#if PLATFORM_ANDROID
        if (!Permission.HasUserAuthorizedPermission(Permission.Microphone))
        {
            return;
        }
        else if(dialog != null)
        {
            Destroy(dialog);
        }
#endif
    }

    private void OnFacesChanged(ARFacesChangedEventArgs obj)
    {

        logText.text = "# of faces: " + obj.updated.Count + "\n";

        foreach (ARFace face in obj.updated)
        {
            mainId = face.trackableId;

            if (!faceAnimExports.ContainsKey(face.trackableId))
            {
                var newFaceAnimExport = new FaceAnimExportEntity();
                newFaceAnimExport.uvs = face.uvs.Select(uv => new MyVec2(uv)).ToList();
                newFaceAnimExport.indices = face.indices.ToList();
                newFaceAnimExport.keyframes = new List<AnimKeyframe>();

                faceAnimExports.Add(face.trackableId, newFaceAnimExport);
            }

            var faceAnimExport = faceAnimExports[face.trackableId];
            var keyframe = new AnimKeyframe
            {
                position = new float[] { face.transform.position.x, face.transform.position.y, face.transform.position.z },
                rotation = new float[] { face.transform.rotation.x, face.transform.rotation.y, face.transform.rotation.z, face.transform.rotation.w },
                vertices = face.vertices.Select(vert => new MyVec3(vert)).ToList()
            };
            faceAnimExport.keyframes.Add(keyframe);
            //If recording record voice
        }

    }

    public void OnSwitchRecord()
    {
        isRecording = !isRecording;
        if (isRecording)
        {
            ColorBlock cb = recordingButton.colors;
            cb.normalColor = cb.selectedColor = Color.green;
            recordingButton.colors = cb;

            m_ARFaceManager.facesChanged += OnFacesChanged;

            m_audioSource.clip = Microphone.Start(null, false, 100, 44100);
            Debug.Log("Start recording...");
        }
        if (!isRecording)
        {
            ColorBlock cb = recordingButton.colors;
            cb.normalColor = cb.selectedColor = Color.white;
            recordingButton.colors = cb;

            m_ARFaceManager.facesChanged -= OnFacesChanged;
            var position = Microphone.GetPosition(null);
            Microphone.End(null);
            Debug.Log("End Recording...");
            var clip = TrimClip(m_audioSource.clip, position);
            SavWav.Save("file.wav", clip);
            Debug.Log("Saved wav at " + position);
            SaveToFile();
            Debug.Log("Saved anim");
        }
    }

    private void SaveToFile()
    {
        string filename = Application.persistentDataPath + "/record.json";
        if (faceAnimExports.ContainsKey(mainId))
        {
            File.WriteAllText(filename, JsonUtility.ToJson(faceAnimExports[mainId]));
            logText.text = "Saving to " + filename;
        }
        else
        {
            logText.text = "Main Key not existent!";
        }
    }
    //Source: https://answers.unity.com/questions/544264/record-dynamic-length-from-microphone.html
    private AudioClip TrimClip(AudioClip clip, int position)
    {
        var soundData = new float[clip.samples * clip.channels];
        clip.GetData(soundData, 0);
        var newData = new float[position * clip.channels];
        for(int i=0; i<newData.Length; i++)
        {
            newData[i] = soundData[i];
        }

        var newClip = AudioClip.Create(clip.name, position, clip.channels, clip.frequency, false, false);
        newClip.SetData(newData, 0);
        AudioClip.Destroy(clip);
        clip = newClip;
        return clip;
    }
}
