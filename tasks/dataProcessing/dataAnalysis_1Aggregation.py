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
import datetime as dt

# read through a tab separated file, storing the lines in a list, until a line starting with the word 'Frame' (matching case) is found. Once found add the line number to a 'metadata' dictionary with the key 'headerLineNumber', then add the increment of the line number to the same dictionary with the key 'dataStartLineNumber'. Use the 'dataStartLineNumber' to then import the remainder of the tab separated file into a pandas dataframe
def read_tab_separated_file(file_path, header_line_str, header_metadata_extractors):
    metadata = {
        "filePath": file_path
    }
    metadata_lines = []

    with open(file_path, 'r') as tsv_file:
        line_num = 0
        while (True):
            line = tsv_file.readline()
            if line == "":
                print(f"Unable to match header start in file: {file_path}")
                break
            line = line[:-1].split("\t")
            if line[0] == header_line_str:
                metadata['headerLineNumber'] = line_num
                metadata['dataStartLineNumber'] = line_num + 1
                break
            else:
                line_num += 1
                metadata_lines.append(line)
    
    for extractor in header_metadata_extractors:
        key, extracted_metadata = extractor(metadata_lines)
        metadata[key] = extracted_metadata

    # data = pd.read_csv(file_path, sep="\t", header=0, skiprows=metadata['headerLineNumber'])
    return metadata

def fetch_metadata(lines, match):
    for line in lines:
        if line[0] == match:
            return line[1:]

marker_processing = {
    "FileName": lambda fileStart: fileStart + ".tsv",
    "Header": "Frame",
    "HeaderExtractors": [
        lambda lines: ("StartTimeStamp", dt.datetime.fromisoformat(fetch_metadata(lines, "TIME_STAMP")[0].replace(", ", "T"))),
        lambda lines: ("StartEvent", tuple(fetch_metadata(lines, "EVENT")[-2:])),
        lambda lines: ("Frequency", int(fetch_metadata(lines, "FREQUENCY")[0])),
        lambda lines: ("FrameCount", int(fetch_metadata(lines, "NO_OF_FRAMES")[0]))
    ],
    "ColumnFilters": []
}
rigidbody_processing = {
    "FileName": lambda fileStart: fileStart + "_6D.tsv",
    "Header": "Frame",
    "HeaderExtractors": [],
    "ColumnFilters": []
}
left_hand_skeleton_processing = {
    "FileName": lambda fileStart: fileStart + "_s_LH.tsv",
    "Header": "Frame",
    "HeaderExtractors": [],
    "ColumnFilters": []
}
right_hand_skeleton_processing = {
    "FileName": lambda fileStart: fileStart + "_s_RH.tsv",
    "Header": "Frame",
    "HeaderExtractors": [],
    "ColumnFilters": []
}
raw_gaze_processing = {
    "FileName": lambda fileStart: fileStart + "_a.tsv",
    "Header": "SAMPLE",
    "HeaderExtractors": [
        lambda lines: ("TimeStamp", tuple(fetch_metadata(lines, "TIME_STAMP"))),
        lambda lines: ("Frequency", int(fetch_metadata(lines, "FREQUENCY")[0].split(".")[0])),
        lambda lines: ("FrameCount", int(fetch_metadata(lines, "NO_OF_SAMPLES")[0]))
    ],
    "ColumnFilters": []
}

gaze_vector_processing = {
    "FileName": lambda fileStart, idx: f"{fileStart}_g_{idx}.tsv",
    "Header": "SAMPLE",
    "HeaderExtractors": [
        lambda lines: ("Side", fetch_metadata(lines, "GAZE_VECTOR_NAME")[0][-2])
    ],
    "ColumnFilters": []
}

######################################################################################
#                                    Testing                                         #
######################################################################################

trial_file_paths = ["./test/Task 1","./test/Task 2"]
trials_metadata = []

for trial_file_path in trial_file_paths:
    print(f"Processing {trial_file_path}")
    trial_metadata = {
        "Marker": read_tab_separated_file(marker_processing["FileName"](trial_file_path), marker_processing["Header"], marker_processing["HeaderExtractors"]),
        "Rigid": read_tab_separated_file(rigidbody_processing["FileName"](trial_file_path), rigidbody_processing["Header"], rigidbody_processing["HeaderExtractors"]),
        "LeftHand": read_tab_separated_file(left_hand_skeleton_processing["FileName"](trial_file_path), left_hand_skeleton_processing["Header"], left_hand_skeleton_processing["HeaderExtractors"]),
        "RightHand": read_tab_separated_file(right_hand_skeleton_processing["FileName"](trial_file_path), right_hand_skeleton_processing["Header"], right_hand_skeleton_processing["HeaderExtractors"]),
        "RawGaze": read_tab_separated_file(raw_gaze_processing["FileName"](trial_file_path), raw_gaze_processing["Header"], raw_gaze_processing["HeaderExtractors"]),
        "Gaze1": read_tab_separated_file(gaze_vector_processing["FileName"](trial_file_path, 1), gaze_vector_processing["Header"], gaze_vector_processing["HeaderExtractors"]),
        "Gaze2": read_tab_separated_file(gaze_vector_processing["FileName"](trial_file_path, 2), gaze_vector_processing["Header"], gaze_vector_processing["HeaderExtractors"])
    }

    trial_metadata["Marker"]["EndTimeStamp"] = (trial_metadata["Marker"]["StartTimeStamp"] + dt.timedelta(milliseconds=(1000 / trial_metadata["Marker"]["Frequency"]) * trial_metadata["Marker"]["FrameCount"]))

    trials_metadata.append(trial_metadata)
    # print(trial_metadata)

## zip data

# create a class which will read a csv file. the first element is the timestamp, the second is is either 'ON' or 'OFF', representing a target state, the following element is the 'TargetId', the next element is the 'YPos', following this is the 'XPos', and finally the 'SubId', other fields can be ignored. Data should be stored in a dictionary. Each pair of rows (first containing 'ON', second containing 'OFF') define a 'PointingWindow' this will have a start timestamp that is 0.5 seconds earlier than the 'ON' timestamp, and an end timestamp that is 0.5 seconds after the 'OFF' timestamp
def get_pointing_windows(file_path):
    line_num = 0
    pointing_windows = []
    with open(file_path, 'r') as csv_file:
        while (True):
            line = csv_file.readline()
            if line == "":
                break
            line = line[:-1].split(",") # remove trailing newline and split by comma
            if line[1] == "ON":
                pointing_windows.append(
                    PointingWindow(
                        dt.datetime.fromisoformat(line[0][:19].replace(".", ":") + line[0][19:]) - dt.timedelta(milliseconds=1000),
                        line[2],
                        line[3],
                        line[4],
                        line[5]
                    )
                )
            elif line[1] == "OFF":
                pointing_windows[line_num].set_end_time(dt.datetime.fromisoformat(line[0][:19].replace(".", ":") + line[0][19:]) + dt.timedelta(milliseconds=1000))
                line_num += 1
    return pointing_windows  

class PointingWindow:
    def __init__(self, start_time, target_id, y, x, sub_target_id):
        self.data = {}
        self.start_time = start_time
        # self.end_time = start_time
        self.target_id = target_id
        self.y = y
        self.x = x
        self.sub_target_id = sub_target_id

    def set_end_time(self, end_time):
        self.end_time = end_time

    def set_data(self, data):
        self.data = data

    def __str__(self):
        return f"startTime: {self.start_time}, endTime: {self.end_time}, TargetId: {self.target_id}, YPos: {self.y}, XPos: {self.x}, Data: {self.data}"

windows = get_pointing_windows("./test/CASUAL_INDIVIDUAL_FOCUSSED_2023-12-06T17.30.42.149.txt")

marker_columns = [
    "TargetArray - 1 X", "TargetArray - 1 Y", "TargetArray - 1 Z", "TargetArray - 2 X", "TargetArray - 2 Y", "TargetArray - 2 Z", "TargetArray - 3 X", "TargetArray - 3 Y", "TargetArray - 3 Z", "TargetArray - 4 X", "TargetArray - 4 Y", "TargetArray - 4 Z", "TargetArray - 5 X", "TargetArray - 5 Y", "TargetArray - 5 Z", "TargetArray - 6 X", "TargetArray - 6 Y", "TargetArray - 6 Z", "TargetArray - 7 X", "TargetArray - 7 Y", "TargetArray - 7 Z", "TargetArray - 8 X", "TargetArray - 8 Y", "TargetArray - 8 Z", "TargetArray - 9 X", "TargetArray - 9 Y", "TargetArray - 9 Z", "TargetArray - 10 X", "TargetArray - 10 Y", "TargetArray - 10 Z", "TargetArray - 11 X", "TargetArray - 11 Y", "TargetArray - 11 Z", "TargetArray - 12 X", "TargetArray - 12 Y", "TargetArray - 12 Z", "TargetArray - 13 X", "TargetArray - 13 Y", "TargetArray - 13 Z", "TargetArray - 14 X", "TargetArray - 14 Y", "TargetArray - 14 Z", "TargetArray - 15 X", "TargetArray - 15 Y", "TargetArray - 15 Z"
]

# With the windows we can now extract the data from the metadata objects created previously based on the start and end timestamps.
print("Starting aggregation")
window_idx = 0
for trial in trials_metadata:
    # For each trial we want to load the data into a dataframe for the different marker sets, ignoring columns we don't need.
    # print(trial)
    startTime = trial["Marker"]["StartTimeStamp"]
    endTime = trial["Marker"]["EndTimeStamp"]

    # print(startTime, endTime)
    for key, metadata in trial.items():
        trial[key]["data"] = pd.read_csv(metadata["filePath"], sep="\t", header=0, skiprows=metadata["headerLineNumber"])
    
    # print(trial)

    # print(windows[window_idx].end_time, endTime, windows[window_idx].start_time, startTime)
    while (windows[window_idx].end_time < endTime and windows[window_idx].start_time > startTime):
        data = {}
        for key, value in trial.items():
            df = value["data"]
            time_header = df.columns[1]
            #    print(key, df.columns)
            #    print(df[time_header])
            minSec = (windows[window_idx].start_time - startTime).total_seconds()
            maxSec = minSec + (windows[window_idx].end_time - windows[window_idx].start_time).total_seconds()
            #    print(minSec, maxSec)
            data[key] = df[(df[time_header] > minSec) & (df[time_header] < maxSec)]

        windows[window_idx].set_data(data)
        # print(windows[window_idx])
        # print(windows[window_idx].data`["Marker"]['LH_LIndexTip X'], windows[window_idx].data["Marker"]['LH_LIndexTip Y'], windows[window_idx].data["Marker"]['LH_LIndexTip Z'])
        # print(windows[window_idx].data`["LeftHand"].iloc[:,67], windows[window_idx].data["LeftHand"].iloc[:,68], windows[window_idx].data["LeftHand"].iloc[:,69])
        print(f"start: {windows[window_idx].start_time}, stop: {windows[window_idx].end_time}, frames: {windows[window_idx].data['Marker'].shape[0]}")
        window_idx += 1

# we need to process the average position of the target array markers so that we can determine the position of each array, and add it to the pointing windows, such that we can set the point in space the sub-target should be, and the plane for intersection.
# want to then drop frames that aren't needed (save on some memory and ignore redundant data.) Fallback on markers if skeleton joint not found?
# double check that we're copying data into each window, rather than using a reference? Might be fine to leave as reference, as none should share.
# Should probably save the data somehow. either as a json, containing a reference to csv files with the data we want in data frames, and containing the metadata within the file
