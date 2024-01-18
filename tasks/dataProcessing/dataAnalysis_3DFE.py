''' TODO:
- Iterate through each trial, determine the pointing gesture/s, derive features that describe the gesture.
    - Determine pointing windows based on movement characteristics. We assume a pointing action involves moving a hand from rest, away from the participants body, in the direction of the target.
        - Determine the end point (either come to rest, and return hand towards body, or where hand immediately returns to body)
        - Will need to account for times where a corrective action is taken (e.g. they moved towards the wrong target initially). Do we track the whole gesture? Do we label as corrected for our own-sake to distinguish between one-shot vs multi-shot attempts (will be unbalanced most-likely, but think worth tracking, e.g. which targets prompted most mistakes, did the distractor cause more?)
        - We will want to output the trajectory for each gesture and target for each condition per participant. We can then visually check if any detected gestures look wrong (e.g. is the gesture short, pointing the wrong way, etc), then given the target and condition we can open up that run and and cycle through the trials to find the dodgy one and manually label the start and stop of the gesture.
        - Maybe just setup for manual labelling with the approach described? Maybe just try to detect end-points, and work from them to determine which one was 'the' endpoint and determine when they were ballistic, pin-pointing (if at all), resting, misc, etc... Maybe ML to predict using CFE data?
    - Once we have the pointing windows, we can derive features that describe the pointing gesture.
        - gesture sub-movements? How many movement were performed within the gesture. Will likely need to do some smoothing or have some heuristics that determine what constitutes a sub-movement. From control theory models we can expect ballistic followed by pin-pointing, however this doesn't account for initial mistakes and corrective gestures. Also need to ignore miscellaneous movements, such as scratches, adjusting glasses, fidgeting.
        - Movement Time, how long taken to point at the target (from rest), how long to point from available, how long to point once looking (if they gaze toward target)
        - Eye Closure
        - Hand Usage (dominant or non-dominant, relative to target location)
            - hand shape during pointing termination, hand gesture during ballistic, etc
        - Alignments at point of termination
            - hand to body
            - hand to arm
            - fingers to hand
            - hand to gaze
        - Distractor focus (when applicable)
            - time taken to gaze at the stroop word, after buzzer sounds
            - time spent looking at screen (were they focusing on the screen)
            - time spent looking at target (was it gaze-less)
        - Additional Body-Pose features - normalised by participant height
            - Distance from head and hands
            - Distance of head and torso
            - Distance of elbow and torso
            - Angle of the hand ray and the torso normal (e.g. imagine a plane along the length of the body, and perpendicular to the shoulders, e.g. emitted from the sternum)
            - Angle of the hand ray to the head ray
            - Angle of the hand ray to the plane defined by the shoulders
            - Movement - normalised by participant height
                - distance travelled by hands
                - Head rotation
            - peak velocity and acceleration
            - average velocity and acceleration fro each component of pointing gesture
            - exertion modelling
'''

''' Folder structure:
    GUID
'''

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

