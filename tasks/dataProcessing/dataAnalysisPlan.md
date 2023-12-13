Data Analysis Plan
===

## Script 1 - Data Aggregation

Need a util package to hold onto classes that can process the files.

### TSV Processors
Could make a unique class per output file 'type' (e.g. gaze, rigid body, marker, etc), or make a generic version, where we pass in the rules / matchers, can then set these rules for specific types.
Will need to verify the files are synced by:
- Initial timestamp, 
- Frequency, 
- Samples,
- Events (if in file)

Also need to verify if:
- 6DoF available (if bones for rigid body change length, will just give 0s), this will also result in no gaze data... Will then need to manually calculate, along with gaze...

Can hopefully have classes return meta data (e.g. timestamp, frequency, number of samples, line for first data, headers, etc), and then a method for getting a numpy array of the data (with headers), though we might want to filter to get only data we want, e.g. remove markers and labels we don't want.

Will need to something to extract the average position of the target markers, from across the samples. Shouldn't move, but could be noise in sensors, data, or from vibrations.

Can drop frames from start to 4.5s after event timestamp (time prior to first stroop/pointing)

### FBX Processor
Want to check same number of frames as samples in TSVs.
Not quite sure how to extract.

### Task Log Processors
Once we have the TSV processors (and optional FBX processor), we need to aggregate the data by time windows derived from the logs spit out from the server. At this stage, each window should take 0.5 seconds before and after the on/off events. if stroop was run, then flag when the stroop was (before or after), tracked maybe as just metadata, then we can determine time from pointing gesture once we've derived that.

### Questionnaire Processors
Processor for each type of question?
Not required to match to each frame, but for each condition, maybe specific targets+condition.
Probably not needed at start, can wait until we have processed and clustered pointing.

---

All this data should be written to a file for each trial per participant, so it can be read-in by another script. Maybe leave FBX data as optional separate file for joining if available.

Data structure should be:
Condition[ACCURATE-FOCUSED|ACCURATE-DISTRACTED|CASUAL-FOCUSED|CASUAL-DISTRACTED]: [
    Trial: 
        MetaData: {
            Timestamp, Stroop Events?
        }
        Target Positions: [
            (Origin, Rotation)
        ]
        Trials: [
            Frame: Hand Skeleton Points (X, Y, Z), Finger-Tip Markers (X, Y, Z), Gaze((Origin, Rotation), (Origin, Rotation)), Head (Origin, Rotation), 
        ]
]

<!-- Once we have the files we need to then do the hand ray calculations -->