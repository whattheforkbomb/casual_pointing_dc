import numpy as np

''' TODO:
- Iterate through participants in processing directory
    - Each participant directory will contain 4 sub-directories (one for each recording, maybe couple exploration ones also)
    - For each recording, read in the files
    - Merge into single data frame for each data type (e.g. gaze, hand, body, task start/stop, Stroop start/stop)
    - Aggregate into different tasks, maybe retaining time prior to task (if exists), so each aggregation is time before task (which will contain any preceding Stroop and rest/gaze?
    - Save to file as json?
'''

''' File notes
- Hand Markers (AIM MODELS)
    - Lines 0-10 are meta data, 11 is Headings
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
- Gaze Vector Data (2 files, on for each eye, _g_[1|2])
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
