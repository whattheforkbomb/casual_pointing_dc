''' TODO:
- Iterate through participants in processing directory
    - Each participant directory will contain 4 sub-directories (one for each recording, maybe couple exploration ones also)
    - For each recording, read in the files
    - Merge into single data frame for each data type (e.g. gaze, hand, body, task start/stop, Stroop start/stop)
    - Aggregate into different tasks, maybe retaining time prior to task (if exists), so each aggregation is time before task (which will contain any preceding Stroop and rest/gaze?
    - Save to file as json?
'''

''' File notes
- Markers 3D data (not 6DoF stuff)
    - Lines 0-10 are meta data, 11 is Headings - THIS WILL CHANGE BASED ON IF EVENTS ARE LOGGED WILL BE UNDER EVENTS HEADER
        - Timestamp is line 7(8) (column 1(2) is Date, ISO-like formatted down to ms (3dp), column 2(3) is Unix timestamp?)
        - Data does not contain timestamp on each line, however timestamp for 1st line is provided (as above), and frequency is given (100Hz)
    - Data contains frame number (column 0(1)), Time (seconds from start, column 1(2)), then data for each marker.
- 6D (Rigid Bodies)
    - Lines 0-12 are meta data, 13 is Headings
    - Timestamp is line 7(8) (column 1(2) is Date, ISO-like formatted down to ms (3dp), column 2(3) is Unix timestamp?)
    - Data contains X, Y, Z, Roll, Pitch, and Yaw for Tobii rigid body. Assume for the centre of mass??? Also contains Rot[0-9], no idea what this is...
- Hand Skeletons (2 files, one for each hand)
    - Lines 0-6 are meta data, 7 is Headings
    - Timestamp is line 3(4) (column 1(2) is Date, ISO-like formatted down to ms (3dp), column 2(3) is Unix timestamp?)
    - Data has X, Y, Z, and Quaternion for the hand as a whole, and also for each bone, or points?
- Raw Gaze data (_a file)
    - Lines 0-12 are meta data, 8 is Headings 
    - Timestamp is line 4(5) (column 1(2) is Date, ISO-like formatted down to ms (3dp), column 2(3) is Unix timestamp?)
    - No frame number or time elapsed given, but frequency is.
    - Headers are weird, not directly over first line of data, they are stored in the Channel_Names row, so also offset by 1 column...
    - Might be different if H/W synced at start?
- Gaze Vector Data (2 files, one for each eye, _g_[1|2])
    - Lines 0-6 are meta data
    - Timestamp is line 3(4) (column 1(2) is Date, ISO-like formatted down to ms (3dp), column 2(3) is Unix timestamp?)
    - No frame number or time elapsed given, but frequency is.
    - No headers???
        - Data format needs to be confirmed, but looks like X, Y, Z, Roll, Pitch, & Yaw
        - Presumably X, Y, Z is vector origin in global reference frame, e.g. the pupil location?
    - Data will be missing during blinks, or just sporadic if tracking is lost.
    - No analysis of saccades or fixation, may need to manually detect.
        - Might also want to detect if eye closed (check missing data for x number of frames?), maybe if in between a pointing tasks start/stop?
'''

''' Data Sync
- Task Events
    - There is no shared data between the tasks start-stop events that we save to disk.
    - However these aren't used to highlight a specific frame, but rather a range of frames.
    - As such we should be okay to just use timestamps (selecting the nearest frame to the timestamp for the event), as we just need the 'window' defined by the task events (and distance from Stroop)
    - Can still look to start recording and run session at the same time?
        - Issue is if using gaze HW sync, need to start capturing before unplugging, as such can't start session at the same time as capture, without adding extended delay, maybe 30s?
- Gaze
    - If not using HW sync (delay in running study?), then can have participant stare at point and slightly rotate head, can then match trajectories of 6DoF-RB and gaze to determine latency.
        - Ideally though we should have ability to link via cable to sync box, at start of recording
    - Gaze may be at 50Hz, so will be missing gaze for every other sample of skeleton data.
        - Should be fine for our use, e.g. just want to know where someone is looking during pointing gesture, not specifically saccades or fixations, or smooth-pursuits.
'''

''' Folder structure:
    GUID
     |-events          - The server logs indicating pointing and stroop task start and stop
     |  |-*_stroop...  - stroop command start/stop
     |  \-*            - pointing command start/stop
     |-gaze[?]         - Optional folder, present if unable to tack gaze with QTM.
     |  |-eventdata.gz
     |  |-gazedata.gz  - This is the one we want, a gz file, which when unzipped is a txt file containing lines of json objects, it is NOT a json object/array for the full file...
     |  \-imudata.gz
     \-mocap
        |-*.fbx        - fbx of points & skeleton (if output from theia)
        |-*.tsv        - Tracking of labelled points (e.g. hand markers and LED target markers)
        |-*_6D.tsv     - Tracking of rigid bodies (e.g. Tobii glasses, maybe LED target markers)
        |-*_s_LH.tsv   - Estimated pose for left hand skeleton, derived from respective marker positions
        |-*_s_RH.tsv   - Estimated pose for right hand skeleton, derived from respective marker positions
        |-*_a.tsv[?]   - Optional Gaze data (unsure exactly which data, believe raw gaze data, e.g. where pupil is, dilation, etc)
        |-*_g_1.tsv[?] - Optional Gaze data for left? eye, namely the gaze vector origin and rotation
        |-*_g_2.tsv[?] - Optional Gaze data for right? eye, namely the gaze vector origin and rotation
'''

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv('/mnt/c/temp/pilot_data/CC/mocap/Task 2.tsv', sep="\t", skiprows=11)

rays = {
    "FRC": [["LH_LIndexTip", "LH_LHandIn"], ["RH_RIndexTip", "RH_RHandIn"]], # Check for straightness via Index2? (e.g. that it is near the line formed by the other two)
    "EFRC": [["LH_LIndexTip", ["Tobii3-Set-L2-R2 - 1 X", "Tobii3-Set-L2-R2 - 1 Y", "Tobii3-Set-L2-R2 - 1 Z", "Tobii3-Set-L2-R2 - 2 X", "Tobii3-Set-L2-R2 - 2 Y", "Tobii3-Set-L2-R2 - 2 Z", "Tobii3-Set-L2-R2 - 3 X", "Tobii3-Set-L2-R2 - 3 Y", "Tobii3-Set-L2-R2 - 3 Z", "Tobii3-Set-L2-R2 - 4 X", "Tobii3-Set-L2-R2 - 4 Y", "Tobii3-Set-L2-R2 - 4 Z", "Tobii3-Set-L2-R2 - 5 X", "Tobii3-Set-L2-R2 - 5 Y", "Tobii3-Set-L2-R2 - 5 Z", "Tobii3-Set-L2-R2 - 6 X", "Tobii3-Set-L2-R2 - 6 Y", "Tobii3-Set-L2-R2 - 6 Z"]], ["RH_RIndexTip", ["Tobii3-Set-L2-R2 - 1 X", "Tobii3-Set-L2-R2 - 1 Y", "Tobii3-Set-L2-R2 - 1 Z", "Tobii3-Set-L2-R2 - 2 X", "Tobii3-Set-L2-R2 - 2 Y", "Tobii3-Set-L2-R2 - 2 Z", "Tobii3-Set-L2-R2 - 3 X", "Tobii3-Set-L2-R2 - 3 Y", "Tobii3-Set-L2-R2 - 3 Z", "Tobii3-Set-L2-R2 - 4 X", "Tobii3-Set-L2-R2 - 4 Y", "Tobii3-Set-L2-R2 - 4 Z", "Tobii3-Set-L2-R2 - 5 X", "Tobii3-Set-L2-R2 - 5 Y", "Tobii3-Set-L2-R2 - 5 Z", "Tobii3-Set-L2-R2 - 6 X", "Tobii3-Set-L2-R2 - 6 Y", "Tobii3-Set-L2-R2 - 6 Z"]]]
}

def get_centre(tobii_array):
    raise NotImplementedError 

def get_hand_rays(row):
    #"LH_LIndexTip", "LH_LHandIn" | "RH_RIndexTip", "RH_RHandIn"
    left_tip = np.array([
        row["LH_LIndexTip X"],
        row["LH_LIndexTip Y"],
        row["LH_LIndexTip Z"]
    ])
    left_base = np.array([
        row["LH_LHandIn X"],
        row["LH_LHandIn Y"],
        row["LH_LHandIn Z"]
    ])

    right_tip = np.array([
        row["RH_RIndexTip X"],
        row["RH_RIndexTip Y"],
        row["RH_RIndexTip Z"]
    ])
    right_base = np.array([
        row["RH_RHandIn X"],
        row["RH_RHandIn Y"],
        row["RH_RHandIn Z"]
    ])

    origin = np.zeros([2])
    left_magnitude = left_tip - left_base # unit
    left_direction = np.array([
        np.arctan2(np.array(0, left_magnitude[1]), np.array(0, left_magnitude[0])) * (180/np.pi),
        np.arctan2(np.array(0, left_magnitude[2]), np.array(0, left_magnitude[0])) * (180/np.pi)
    ])

    right_magnitude = right_tip - right_base
    right_direction = np.array([
        np.arctan2(np.array(0, right_magnitude[1]), np.array(0, right_magnitude[0])) * (180/np.pi),
        np.arctan2(np.array(0, right_magnitude[2]), np.array(0, right_magnitude[0])) * (180/np.pi)
    ])

    print(left_magnitude, left_direction, "|", right_magnitude, right_direction)

    # get points (In and Tip)
    # get unit/magnitude? - needed for rotation calc, as only need origin (base), and rotation, for the line
    #   Realistically we don't need the origin, just the line that passes through two points, and then checking intersection on cluster's plane, closest to the second point (tip)
    #   probably want rotation though to be used in the prediction, and get rotation about body (might impact model for casual pointing, e.g. relative position from eyes.)
    # 

# print(data)
rays = [
    [
        (0,0,0), # Origin
        (1,1,1)  # Dest
    ],
    [
        (0,0,0), # Origin
        (1.5,1,1)  # Dest
    ],
    [
        (0,0,0), # Origin
        (1,1.2,1.1)  # Dest
    ],
    [
        (0,0,0), # Origin
        (-1,1,1)  # Dest
    ],
    [
        (0,0.2,0), # Origin
        (1.1,1,1)  # Dest
    ],
    [
        (0,0,0), # Origin
        (1.4,-1,1)  # Dest
    ]
]
# for i in range(10):
    # rays.push(get_hand_rays(data.loc[i,:]))


from matplotlib.widgets import Button, Slider

fig, ax = plt.subplots(subplot_kw=dict(projection="3d"))
ax.view_init(elev=45, azim=45)

lines = []

for data in rays:
    xData, yData, zData = list(zip(*data))
    lines.append(ax.plot3D(xData, yData, zData, color="black"))

def update(i):
    for idx in range(len(lines)):
        colour = "red" if (idx == i) else "black" 
        # print(colour, idx, lines[idx])
        lines[idx][0].set_color(colour)

fig.subplots_adjust(bottom=0.25)
axfreq = fig.add_axes([0.25, 0.1, 0.8, 0.03])
freq_slider = Slider(
    ax=axfreq,
    label='trial',
    valmin=0,
    valmax=len(lines)-1,
    valstep=range(len(lines)),
    valinit=0,
)

freq_slider.on_changed(update)

plt.show()