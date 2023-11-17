package com.jw2304.pointing.casual.tasks.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import com.jw2304.pointing.casual.tasks.stroop.data.Stroop;
import com.jw2304.pointing.casual.tasks.stroop.data.StroopColour;
import com.jw2304.pointing.casual.tasks.targets.data.Target;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;

public class CommandSequenceGenerator {

    public static Logger LOG = LoggerFactory.getLogger(CommandSequenceGenerator.class);

    // Target Config
    private final int subTargetCount;

    private final int taskCount;

    // private final TargetColour colour;
    
    private CommandSequenceGenerator(TargetColour colour, int subTargetCount, int taskCount) {
        // this.colour = colour;
        this.subTargetCount = subTargetCount;
        this.taskCount = taskCount;
    }

    private final Random rng = new Random(System.currentTimeMillis());    
    private final Random sequenceRNG = new Random(123456789); // Inventive, I know

    public Pair<List<Target>, List<Stroop>> generateSequence(
        TargetType targetType , int targetStartDelayMilliseconds, int targetDurationMilliseconds, 
        int stroopStartDelayMilliseconds, int stroopDurationMilliseconds, int jitterAmount, 
        boolean distractor, int targetCount
    ) {
        boolean isCluster = targetType.equals(TargetType.CLUSTER);
        int totalTargetCount = isCluster ? taskCount * targetCount : taskCount * (targetCount * 3);
        
        List<Target> possibleTargets = new ArrayList<>(totalTargetCount);
        
        if (isCluster) {
            // create repeat of each cluster
            for (int i=0; i<targetCount; i++) { // this just looks stupid...
                Collections.addAll(
                    possibleTargets, 
                    new Target(i, -1, targetStartDelayMilliseconds, targetDurationMilliseconds),
                    new Target(i, -1, targetStartDelayMilliseconds, targetDurationMilliseconds),
                    new Target(i, -1, targetStartDelayMilliseconds, targetDurationMilliseconds)
                );
            }
        } else {
            // pick at random one subtarget from each target
            // remove others on the same row and col
            // pick at random from remaining
            // remove others on the same row and col
            // pick only remaining
            // repeat by 3
            for (int i=0; i<targetCount; i++) { // for each cluster
                LOG.info("Repeating Individual LEDs - Cluster[%d]".formatted(i));
                int[] subTargetIndexes = IntStream.range(0, subTargetCount).toArray();
                for (int j=0; j<2; j++) {
                    int subTarget = subTargetIndexes[sequenceRNG.nextInt(subTargetIndexes.length)];
                    LOG.info("Repeating Individual LEDs - LED[%d]".formatted(subTarget));
                    Collections.addAll(
                        possibleTargets, 
                        new Target(i, subTarget, targetStartDelayMilliseconds, targetDurationMilliseconds),
                        new Target(i, subTarget, targetStartDelayMilliseconds, targetDurationMilliseconds),
                        new Target(i, subTarget, targetStartDelayMilliseconds, targetDurationMilliseconds)
                    );
                    subTargetIndexes = IntStream.of(subTargetIndexes)
                        .filter((idx) -> !(idx / 3 == subTarget / 3 || idx % 3 == subTarget % 3))
                        .toArray();
                    
                    LOG.info("Repeating Individual LEDs - Remaining LEDs: %s".formatted(Arrays.toString(subTargetIndexes)));
                }
                // after final filter, should only be one option
                Collections.addAll(
                    possibleTargets, 
                    new Target(i, subTargetIndexes[0], targetStartDelayMilliseconds, targetDurationMilliseconds),
                    new Target(i, subTargetIndexes[0], targetStartDelayMilliseconds, targetDurationMilliseconds),
                    new Target(i, subTargetIndexes[0], targetStartDelayMilliseconds, targetDurationMilliseconds)
                );
            }
        }
        LOG.info("Shuffling possible targets");
        Collections.shuffle(possibleTargets, sequenceRNG);

        List<Target> finalTargets = new ArrayList<Target>(totalTargetCount);
        List<Stroop> stroopMessages;

        if (distractor) {
            // need to insert stroop tasks (will be more than task count), and adjust the target delays (distributed between ahead or behind)
            int preStroop = totalTargetCount / 2;
            int postStroop = totalTargetCount / 2;
            if (totalTargetCount % 2 != 0) {
                if (rng.nextBoolean()) {
                    preStroop++;
                } else {
                    postStroop++;
                }
            }
            stroopMessages = new ArrayList<Stroop>();
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));

            // int initialDelay = (stroopStartDelayMilliseconds * 3) + (stroopDurationMilliseconds * 4);
            int initialDelay = (stroopStartDelayMilliseconds + stroopDurationMilliseconds) * 4;
            // Need to check if current stroop duration will elapse current target, if not need another stroop

            boolean previousTargetPostStroop = false;
            long stroopElapsed = initialDelay;
            long targetElapsed = initialDelay; // by default will have same delay at start.

            for (Target target : possibleTargets) {
                boolean targetPreStroop = rng.nextBoolean();
                if (preStroop < 1) { // No more preStroops, so do post
                    targetPreStroop = false;
                } else if (postStroop < 1) { // No more postStroops, so do pre
                    targetPreStroop = true;
                } else {
                    if (targetPreStroop) {
                        --preStroop;
                    } else {
                        --preStroop;
                    }
                }
                
                // int currentStroopDelay = (int)((stroopMessages.size() * (stroopStartDelayMilliseconds + stroopDurationMilliseconds)) - stroopElapsed);

                // step 1, ensure that the elapsed target time is greater than the stroop by adding stroops.
                while(targetElapsed > stroopElapsed) {
                    stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                    stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                }

                // step 2, inject an additional stroop if a post and pre stroop test are B2B
                if (previousTargetPostStroop && targetPreStroop) { // need to add a stroop test between these 2 cases
                    stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                    stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                }
                
                stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;

                // step 3, calc offset
                //  need to be based on the start of the latest stroop (are we early or late), this needs to be adjusted by the diff between the target elapsed.

                int offset = (targetPreStroop ? -targetStartDelayMilliseconds : targetStartDelayMilliseconds);
                offset += stroopElapsed - targetElapsed;

                // if (offset < 0) {
                //     stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                //     stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                //     offset += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                // }

                // add target with offset relative to the stroop.
                target.startDelayMilliseconds = (offset);
                finalTargets.add(target);
                targetElapsed += offset + target.durationMilliseconds;

                // Need to now add additional stroop to cover the duration of the target;
                // int targetDurationStroopOverlap = target.durationMilliseconds + offset;
                // while(targetDurationStroopOverlap > stroopStartDelayMilliseconds + stroopDurationMilliseconds) {
                //     stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                //     stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                //     targetDurationStroopOverlap -= stroopStartDelayMilliseconds + stroopDurationMilliseconds;
                // }
                // stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                // stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
            }

            // remove dead-space at the end
            while(targetElapsed-stroopDurationMilliseconds > stroopElapsed) {
                stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
            }
            
            finalTargets.get(0).startDelayMilliseconds += initialDelay;
        } else {
            // finalTargets = possibleTargets;
            // add some jitter across target delays (e.g. +/-(0 - jitterAmount))
            for (Target target : possibleTargets) {
                target.startDelayMilliseconds += rng.nextInt(jitterAmount*2) - jitterAmount;
                finalTargets.add(target);
            }
            stroopMessages = Collections.emptyList();
        }

        return Pair.of(finalTargets, stroopMessages);
    }

    private Stroop getStroop(int delay, int duration) {
        return new Stroop(StroopColour.values()[rng.nextInt(StroopColour.values().length)].name(), StroopColour.values()[rng.nextInt(StroopColour.values().length)], delay, duration);
    }

    public static CommandSequenceGenerator create(TargetColour colour, int subTargetCount, int taskCount) {
        return new CommandSequenceGenerator(colour, subTargetCount, taskCount);
    }

}
