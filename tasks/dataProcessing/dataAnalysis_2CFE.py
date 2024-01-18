''' TODO:
- Iterate though trials and process features that can be produced frame-by-frame:
    - Velocity (Linear and Angular)
    - Acceleration (Linear and Angular)
    - Jerk?
    - Hand pose
        - Finger extensions, 0 (curled / near palm) - 1 (Extended / far from palm), derive from distance to hand in / out from both finger mounted markers (if curled, both markers will be similar distance to hand marker) - normalised by tracked finger length?
        - Angle from palm plane (plane made from hand in, out and one of the wrists, can alternate based on which 3 points are available), basically are they pointing aligned with the hand/palm, or at angle from it.
        - Alignment to head vector (based on Tobii glasses Rigid Body)
    - Body Pose (if we have FBX data)
        - Elbow flexion, 0 (curled, hand near shoulder) - 1 (Extended, hand far from shoulder) - normalised by participant height
        - Head Torso alignment (head vector normal from plane based on shoulders and pelvis?)
        - Hand Torso Alignment (hand direction from plane based on shoulders and pelvis?)
        - Hand elevation (above shoulders) - normalised by participant height
    - Gaze Alignments
        - To Target
        - To Screen
        - To Hand / Rays
        - Eye Closure
    - Rays
        - Hand Ray (ray from index hand-in to passing through index finger-tip.)
        - EFRC (ray from point between eyes, through index finger-tip)
            - Possibly perform a ray from the open eye (if only one is closed)
        - Accuracy, distance from where the ray intersects the plane, defined by the target box face, and the led position.
'''

''' Folder structure:
    GUID
     
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


