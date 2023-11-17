Data Analysis Plan
===

Below will cover what we aim to learn / extract from the study, what data we can collect from the study, and finally how we will process the data.

What Are We Trying to Learn?
---
Just to reiterate the paper/PhD goals: 
- We want to contribute towards enabling systems to adapt to an individual's preference, behaviour, or circumstances for interaction. 

To this point we are starting with interaction at a distance, specifically pointing. At present systems that support pointing either require:
- A cursor, making pointing a Control Theory problem, e.g. user's are not using their arm to point, they're using it to adjust the position of the cursor
- Others require (or rather were researched with) a specific pointing behaviour/style, typically dominant hand only, fully-extended arm and index finger
- Alternatives require gaze, as such are unable to interact with objects without needing to be facing/looking at them.

As such we are looking to complement these existing techniques with support for alternative pointing gestures/behaviour. We are calling this 'Casual' pointing, which we consider to mean
- Pointing in such a manner that the user prioritises comfort over accuracy, though correct identification of the target is still a requirement.
- Pointing you would feel comfortable doing if your attention was focussed elsewhere (e.g. on your phone, reading a book, watching TV, cooking, etc...)

We have not got a more specific definition as this is what we would like to learn from the study.
However with this definition we do can make some obvious hypothesises:

1. Pointing in this manner will likely be less accurate.
Though this is highly coupled to the performance of any model we could build from the data we collect, we suspect that casual pointing will be less consistent than typical accurate pointing, and as such would expect less precision / greater variance in estimated points.

2. Pointing in this manner will likely require less effort.
As we are asking to prioritise comfort, we would expect this to be understood as exerting less effort, or maintaining poses that utilise less muscles.

3. Pointing casually may have multiple behaviours within participants, while precise pointing will be less variable
People might have different behaviour depending on the target location with regards to their dominant arm, or even just inconsistency between gestures.

3. Pointing casually may have multiple behaviours across participants, while precise pointing will be less variable
Different participants may exhibit casual pointing behaviour different to that of others, while we expect most participants to share a common precise pointing behaviour (as seems to be the case from literature, though this could be impacted by only tracking their dominant arm, or phrasing).

3. Existing ray casting models (EFRC, IFRC, FRC, HRC) may not be suitable for casual pointing prediction.

Confirming the difference in accuracy/precision will help us in defining design guidelines to use when setting-up interfaces to support casual pointing (such as minimum distance between points, minimum size of interactable elements), along with 
We aren't looking to 'prove' the second point, as again it is implied from our description that it should exert less effort, but it would be nice to measure and confirm our assumption.
Points 3 and 4 will be good to confirm to better understand whether the EFRC model is suitable for all people for accurate pointing, along with confirming whether casual pointing can have generic models developed, or if models need to be unique to individuals (or rather adapted/learned to individuals, e.g. reinforcement learning?)
Point 5 would be good to confirm that existing models don't work for all pointing needs of users.

### What Data Do We Need?
Given all of the above, what data do we need?

1. Participant Behaviour for accurate/precise and casual pointing
Need to track participants performing both types of behaviour so that we can learn the difference between them.

2. Pointing behaviour while distracted.
This will be helpful for comparing accurate/precise against casual pointing behaviour while the participant's attention is focussed elsewhere, as this is part of our casual pointing description. Which pointing behaviour is preferred between the two while focussed on another task?

3. Demographics
Who is taking part, what is their experience with XR, what is their dominant hand/eye.
Some of this can be provided by participants, others can be derived from data (e.g. eye and shoulder height).

4. Accuracy & Precision/Consistency
Given valid pointing estimation models we want to confirm participant accuracy, while focussed vs distracted, between different pointing styles, and based on relative target position (existing studies focus on targets ahead of the participant, typically in a 2D plane). This is why we need clusters; if an LED/target was in isolation then there is little risk of mis-identification, having nearby targets should encourage participants to try and ensure accuracy. Alongside this we have participant self-reported accuracy/performance.
Without a model to accurately estimate pointing location we should also review how consistent a participant is with different behaviours (i.e. for a target in a specific relative position, do they always point in the same manner, hold the same pose, use the same arm, etc...). This is why we need repetitions.

5. Comfort / Ease-of-Use
Do participants enjoy each style, When would they feel comfortable using each style, what is more 'natural', what is more performant. Confirm whether casual pointing is worth incorporating into XR systems.


What Data Do We Have?
---
We have the data exported from the marker and marker-less tracking systems in FBX, though marker-based can also do TSV.
Need to ensure data is trimmed and filled / labelled prior to processing of the marker-less data (ensure data timelines are the same correctly, otherwise will need to manually sync)

1. **Hand Skeleton**
    - Hand Pose

2. **Body Skeleton**
    - Arm length
    - body pose during pointing gestures

3. **Pointing Task Timestamps**
    - Use for separating the data into windows containing pointing gestures.

4. **Eye Tracker**
    - Eye Height
    - Gaze

5. **Target Positions**
    - Target enclosure dimensions
    - Known relative marker placement
    - Known relative sub-target positions

6. **Instruction Screen position**
    - Known dimensions
    - Known relative marker placement

6. **Effort**
    - Self-Reported: Borg RPE, and TLX questionnaires

7. **Demographics**
    - From initial survey
    - Eye dominance from test ahead of each session

What Do We Want To Extract From The Data
---
1. **Accuracy / Precision / Consistency**
    Can aggregate by relative target locations, arm usage, distractor vs non-distractor, clusters vs specific LEDs, arm dominance
    1. Ray Casting Models
        Compare distance from closest point on estimated ray to target. 
        - EFRC: Centre of Eyes -> Index-Finger Tip (enhance with model utilising hand and eye dominance to adjust origin of ray)
        - IFRC: Index Finger-Base -> Index Finger-Tip
        - FRC:  Elbow -> Wrist
    
    1. Difference in skeleton point positions (pose)
        Hand pose vs position
        body pose
        body pose based on relative target position

3. **Usability**
    1. Questionnaire answers for each session.
        - Aggregate/average metrics based on hand and eye dominance, target position, pointing behaviour, effort exerted (does it match self reported), compare with measured accuracy.
    2. Effort
        - Derived: Models used in XRgonomics, and others
        - Self-Reported: Borg RPE, and TLX questionnaires
        - Range of motion, compare between techniques
        - Rest poses?

3. **Distractor Task Impact**
    - Are participants focusing on the distractor task?
    Hopefully can use gaze to ensure looking at screen, and use transcript of recording of voice to confirm correctly reading correct colours.
    - Does this impact the amount of time they gaze at targets prior to and during gestures
    - Is pointing behaviour/pose/speed impacted.

1. **Features for Classification**
    Need to be able to differentiate between pointing behaviours, both the difference between accurate and casual, and to know if people exhibit multiple distinguishable behaviours within a given style, and if any common styles between participants.
    1. Features from [Paper](https://trello.com/c/u7qH8630/20-body-pose-features-for-classification) used to classify interaction between 2 people given skeleton data
        - Joint Distance: The euclidean distance between all 2 pairs of points within the skeleton. We might not need this for all points, e.g. foot to hand, but will allow us track whether a hand is extended from the body, just not the direction, for example.
        - Joint Motion: Distance between 1 point at 2 different time-steps. This is to show motion, so whether a pose is being held, or if part of the body is actively moving. Likely want some filtering over this to reduce impact of jitter, either filter based on velocity, rolling average...
        - Plane: A plane defined by some points in the skeleton, and then evaluating the distance from the plane of specific points. Seems to me to be a bit like comparing the distance of one point to several others, but perhaps easier to visualise or conceptualise what is actually being measured, e.g. separation of hands, distance of hand from the body
        - Normal Plane: Similar to plane, but using the normal, e.g. neck to torso would have a plane that would allow you to see the distance above the persons neck, relative to the orientation of their body, not the global reference frame.
        - Velocity: self explanatory, useful for potentially confirming ballistic vs pin-pointing vs rest.
        - Normal velocity: velocity along the direction of a plane composed of 3 points

    1. Over-fitting
    Avoid highly specific features that are tied to the experiment setup (one issue I had with [Dalsgaard et. al.s](https://trello.com/c/XxXo7pzG/21-3d-pointing-modelling) paper, which binned data to hand position above, in-line, below the body, which was specific to the 3D grid of targets having 3 levels...).

    1. PCA
    From the features we have, we can use PCA to identify which give us the most information regarding pointing behaviour. We can also use this once we have a model for predicting pointing to select the features that provide the most information for  model parameters.

    1. Clustering
    Some Unsupervised clustering will be helpful for understanding the difference in pointing of different types, before knowing which features can be utilised to best classify the different behaviour.

    1. Heuristics
    If we can identify common features for specific behaviour, we could hopefully define some simple heuristics to categorise pointing behaviour, rather than needing a more extensive model.