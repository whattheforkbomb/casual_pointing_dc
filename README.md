# casual_pointing_dc
Data Collection for Casual Pointing Study

## TODO:
- Add logic for controlling the Arduinos (via tcp, sending bytes through the offline LAN)
- Provide logic for generating the conditions (based on prior set of conditions?)
- Provide logic for generating the pseudo-random target sequences (7 repeats of clusters, with random order, though maybe try to have uniformly distributed; sequence of 7-out-of-9 within each cluster, ideally want to have the position of missing LEDs distributed across the targets, e.g. don't have the bottom-centre LED missing for all the targets)
    - Probably easier to calculate the missing LEDs in each cluster, pulling uniformly randomly form the set of possible missing locations/conditions, then randomly select order from remaining LEDs.
- Provide logic for generating Stroop test
    - Need to have ability to account for colour-blindness?
    - Should randomly select a word for a colour and a colour value to use to display the word. Which Font?
    - How long to keep on screen. Should be out-of-sync with the LED targets, as to not have participants performing the gesture simultaneously with the Stroop test. Pilot to find ideal speed.

