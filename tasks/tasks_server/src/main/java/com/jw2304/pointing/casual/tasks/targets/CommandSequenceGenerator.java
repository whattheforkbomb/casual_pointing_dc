package com.jw2304.pointing.casual.tasks.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.Set;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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

    public List<Pair<List<Target>, List<Stroop>>> generateSequence(
        TargetType targetType , int targetStartDelayMilliseconds, int targetDurationMilliseconds, 
        int stroopStartDelayMilliseconds, int stroopDurationMilliseconds, int jitterAmount, 
        boolean distractor, int targetCount, Set<StroopColour> colourFilter
    ) {
        boolean isCluster = targetType.equals(TargetType.CLUSTER);
        int totalTargetCount = isCluster ? taskCount * targetCount : taskCount * (targetCount * 3);
        
        List<Target> possibleTargets = new ArrayList<>(totalTargetCount);
        
        if (isCluster) {
            // create repeat of each cluster
            for (int i=0; i<targetCount; i++) { // this just looks stupid...
                Collections.addAll(
                    possibleTargets, 
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
                    possibleTargets.add(new Target(i, subTarget, targetStartDelayMilliseconds, targetDurationMilliseconds));
                    subTargetIndexes = IntStream.of(subTargetIndexes)
                        .filter((idx) -> !(idx / 3 == subTarget / 3 || idx % 3 == subTarget % 3))
                        .toArray();
                    
                    LOG.info("Repeating Individual LEDs - Remaining LEDs: %s".formatted(Arrays.toString(subTargetIndexes)));
                }
                possibleTargets.add(new Target(i, subTargetIndexes[0], targetStartDelayMilliseconds, targetDurationMilliseconds));
            }
        }

        List<Pair<List<Target>, List<Stroop>>> finalSequence = new ArrayList();

        for (int i=0; i<taskCount; i++) {
            Collections.shuffle(possibleTargets);
            // Pair<List<Targets>, 
            if (distractor) {
                finalSequence.add(
                    generateStroopSequence(possibleTargets, totalTargetCount, stroopStartDelayMilliseconds, stroopDurationMilliseconds, targetStartDelayMilliseconds, targetDurationMilliseconds, colourFilter)
                );
            } else {
                List<Target> targetSequence = new ArrayList();
                for (Target target : possibleTargets) {
                    // target.startDelayMilliseconds += rng.nextInt(jitterAmount*2) - jitterAmount;
                    targetSequence.add(new Target(target.id, target.subTarget, target.startDelayMilliseconds, target.durationMilliseconds));
                }
                finalSequence.add(Pair.of(targetSequence, Collections.emptyList()));
            }
        }

        return finalSequence;
    }

    private Pair<List<Target>, List<Stroop>> generateStroopSequence(
        List<Target> possibleTargets,
        int totalTargetCount,
        int stroopStartDelayMilliseconds, 
        int stroopDurationMilliseconds, 
        int targetStartDelayMilliseconds, 
        int targetDurationMilliseconds, 
        Set<StroopColour> colourFilter
    ) {
        List<Target> finalTargets = new ArrayList();
        int preStroop = totalTargetCount / 2;
        int postStroop = totalTargetCount / 2;
        if (totalTargetCount % 2 != 0) {
            if (rng.nextBoolean()) {
                preStroop++;
            } else {
                postStroop++;
            }
        }
        List<Stroop> stroopMessages = new ArrayList<Stroop>();
        stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds, colourFilter));
        stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds, colourFilter));
        stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds, colourFilter));
        stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds, colourFilter));

        int initialDelay = (stroopStartDelayMilliseconds + stroopDurationMilliseconds) * 3;
        // Need to check if current stroop duration will elapse current target, if not need another stroop

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
                    --postStroop;
                }
            }

            // step 3, calc offset
            //  need to be based on the start of the latest stroop (are we early or late), this needs to be adjusted by the diff between the target elapsed.
            int offset = (targetPreStroop ? -targetStartDelayMilliseconds : targetStartDelayMilliseconds);
            offset += stroopElapsed - targetElapsed;

            finalTargets.add(new Target(target.id, target.subTarget, offset, targetDurationMilliseconds));
            targetElapsed += offset + targetDurationMilliseconds;
            
            // step 1, ensure that the elapsed target time is greater than the stroop by adding stroops.
            while(targetElapsed+1500 > stroopElapsed) {
                stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds, colourFilter));
                stroopElapsed += stroopStartDelayMilliseconds + stroopDurationMilliseconds;
            }

        }

        finalTargets.get(0).startDelayMilliseconds += initialDelay;
        return Pair.of(finalTargets, stroopMessages);
    }

    private Stroop getStroop(int delay, int duration, Set<StroopColour> colourFilter) {
        return new Stroop(
            StroopColour.values()[rng.nextInt(StroopColour.values().length)].name(), 
            Stream.of(StroopColour.values()).filter(Predicate.not(colourFilter::contains)).toArray(StroopColour[]::new)[rng.nextInt(StroopColour.values().length - colourFilter.size())], 
            delay, 
            duration
        );
    }

    public static CommandSequenceGenerator create(TargetColour colour, int subTargetCount, int taskCount) {
        return new CommandSequenceGenerator(colour, subTargetCount, taskCount);
    }

}
