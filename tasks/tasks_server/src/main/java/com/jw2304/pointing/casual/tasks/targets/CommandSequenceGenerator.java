package com.jw2304.pointing.casual.tasks.targets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
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

    public Pair<List<Target>, List<Stroop>> generateSequence(
        TargetType targetType , int targetStartDelayMilliseconds, int targetDurationMilliseconds, int stroopStartDelayMilliseconds, 
        int stroopDurationMilliseconds, boolean distractor, int targetCount
    ) {
        
        List<Target> possibleTargets = new ArrayList<>(taskCount);
        boolean isCluster = targetType.equals(TargetType.CLUSTER);
        int totalTargetCount = isCluster ? targetCount : targetCount * subTargetCount;
        
        int repeats = Math.floorDiv(taskCount, totalTargetCount); // getRepeats(targetCount, totalTargetCount);
        LOG.info("Expected Tasks: %d, Available Targets: %d, Target Repeats: %d".formatted(taskCount, totalTargetCount, repeats));

        // Generate all the repeats, only expected for cluster scenario
        if (repeats > 0) {
            for (int i=0; i<targetCount; i++) {
                if (isCluster) {
                    for (int j=0; j<repeats; j++) {
                        possibleTargets.add(new Target(i, 0, targetStartDelayMilliseconds, targetDurationMilliseconds));
                    } 
                } else {
                    for (int j=0; j<subTargetCount; j++) {
                        for (int k=0; k<repeats; k++) {
                            possibleTargets.add(new Target(i, j, targetStartDelayMilliseconds, targetDurationMilliseconds));
                        }
                    }
                }
            }
        }

        int remaining = taskCount - (repeats * totalTargetCount);
        LOG.info("Remaining Targets After Repeats Processed: %d".formatted(remaining));

        // If there are any left over (or there were no repeats), then we want to distribute the remaining
        if (remaining > 0) {
            List<Target> remainingTargets = new ArrayList<Target>(remaining);
            if (isCluster) {
                List<Target> possibleRemainingTargets  = IntStream.range(0, targetCount)
                    .mapToObj((i) -> new Target(i, 0, targetStartDelayMilliseconds, targetDurationMilliseconds))
                    .collect(Collectors.toList());
                Collections.shuffle(possibleRemainingTargets, rng);
                remainingTargets.addAll(possibleRemainingTargets.subList(0, remaining));
            } else {
                List<List<Target>> possibleRemainingTargets = IntStream.range(0, targetCount)
                    .mapToObj((i) -> {
                        List<Target> possibleRemainingSubTargets = IntStream.range(0, subTargetCount)
                            .mapToObj((j) -> new Target(i, j, targetStartDelayMilliseconds, targetDurationMilliseconds))
                            .collect(Collectors.toList());
                            Collections.shuffle(possibleRemainingSubTargets, rng);
                            return possibleRemainingSubTargets;
                    }).collect(Collectors.toList());

                List<List<Target>> removed = new ArrayList<List<Target>>();

                for (int i=0; i<remaining; i++) {
                    if (possibleRemainingTargets.size() < 1) {
                        possibleRemainingTargets.addAll(removed);
                        removed = new ArrayList<List<Target>>();
                    }

                    int idx = rng.nextInt(possibleRemainingTargets.size());
                    List<Target> possibleRemainingSubTargets = possibleRemainingTargets.remove(idx);
                    remainingTargets.add(possibleRemainingSubTargets.remove(0));
                    removed.add(possibleRemainingSubTargets);
                }
            }
            possibleTargets.addAll(remainingTargets);
        }

        Collections.shuffle(possibleTargets, rng);

        List<Target> finalTargets;
        List<Stroop> stroopMessages;

        if (distractor) {
            // need to insert stroop tasks (will be more than task count), and adjust the target delays (distributed between ahead or behind)
            int preStroop = taskCount / 2;
            int postStroop = taskCount / 2;
            if (taskCount % 2 != 0) {
                if (rng.nextBoolean()) {
                    preStroop++;
                } else {
                    postStroop++;
                }
            }
            finalTargets = new ArrayList<Target>(taskCount);
            stroopMessages = new ArrayList<Stroop>();
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
            boolean previousTargetPostStroop = false;
            int overallElapsed = stroopStartDelayMilliseconds*2 + stroopDurationMilliseconds*2;
            for (Target target : possibleTargets) {
                if (preStroop > 0 && postStroop > 0) {
                    boolean targetPostStroop = rng.nextBoolean();
                    if (targetPostStroop) { // post
                        target.startDelayMilliseconds = (stroopMessages.size() * (stroopStartDelayMilliseconds + stroopDurationMilliseconds)) + targetStartDelayMilliseconds - overallElapsed;
                    } else {
                        if (previousTargetPostStroop) {
                            stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                            // need to add a stroop test between these 2 cases
                        }
                        target.startDelayMilliseconds = (stroopMessages.size() * (stroopStartDelayMilliseconds + stroopDurationMilliseconds)) - targetStartDelayMilliseconds - overallElapsed;
                    }
                    previousTargetPostStroop = targetPostStroop;
                } else if (preStroop > 0) {
                    target.startDelayMilliseconds = (stroopMessages.size() * (stroopStartDelayMilliseconds + stroopDurationMilliseconds)) - targetStartDelayMilliseconds - overallElapsed;
                } else {
                    target.startDelayMilliseconds = (stroopMessages.size() * (stroopStartDelayMilliseconds + stroopDurationMilliseconds)) + targetStartDelayMilliseconds - overallElapsed;
                }
                overallElapsed += target.startDelayMilliseconds + targetDurationMilliseconds;
                stroopMessages.add(getStroop(stroopStartDelayMilliseconds, stroopDurationMilliseconds));
                finalTargets.add(target);
            }
            finalTargets.get(0).startDelayMilliseconds += stroopStartDelayMilliseconds*2 + stroopDurationMilliseconds*2;
        } else {
            finalTargets = possibleTargets;
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
