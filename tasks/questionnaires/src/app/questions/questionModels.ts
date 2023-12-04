export const demographics = {
  name: "demographics",
  elements: [{
    name: "0_age",
    title: "Please enter your age:",
    type: "text",
    inputType: "number",
    isRequired: true
  }, {
    name: "0_0_gender",
    title: "Please enter the gender you identify as:",
    type: "dropdown",
    choices: [
      "Male",
      "Female",
      "Non-Binary", // Do we want info on bio sex or gender identity?
      "Prefer Not To Say"
    ],
    isRequired: true
  }, {
    name: "0_0_pointing-interaction-exposure",
    title: "Have you previously used pointing for interaction?",
    description: "By this we mean interacting with objects beyond arm's reach in VR/AR, either with yours hands or a controller (typically a ray is used), or for example with a Wiimote for the Nintendo Wii.",
    type: "dropdown",
    choices: [
      "No",
      "Yes - Once or Twice",
      "Yes - Multiple Times"
    ],
    isRequired: true
  }, {
    name: "0_0_hand-dom",
    title: "Which hand is your dominant hand?",
    type: "dropdown",
    choices: [
      "I'm Left Handed",
      "I'm Right Handed",
      "I'm Ambidextrous"
    ],
    isRequired: true
  }]
};

export const researcherDemographics = {
    // title: "Researcher Demo and Demographics",
    elements: [{
        type: "panel",
        name: "briefing",
        elements: [
            {
                type: "html",
                      html: `
                        <h2>Brief</h2>
                        <p>
                          So, before we properly begin, let me walk you through the purpose of the study, and what you will be asked to do during the study.
                        </p> <p>
                          With the development of smart home IoT devices (think smart thermostats, lights, and such) and Mixed Reality (things like VR, Augmented Reality) we can interact with devices at a distance, that are out of arms-reach. 
                        </p><p>
                          Pointing is a common way of indicating to a system what you want to interact with, for example you might point at a light switch to turn it on or off.
                        </p>
                        </br>
                        <p>
                          We want to compare the differences between pointing as accurately and precisely as possible, versus pointing as casually and relaxed as possible.
                        </p>
                        <p>
                            With this data we hope to develop a system that can adapt to your pointing behaviour, able to predict where you may be pointing, regardless of the pointing gesture performed.
                        </p>
                        <br>
                        <h2>What Do we mean by pointing?</h2>
                        <p>
                          In this study, we use the term pointing to refer to the action of indicating a specific target in space. 
                        </p><p>
                          In the context of this study, the idea is to communicate to a device a target that you intend to interact with.
                        </p><p>
                          Our intention is for this type of pointing to be used, for example, to switch a light switch on or select an option from a menu.
                        </p></ br><p>
                          During this session a series of LED targets will be turned on. Each target will flash on and off for 3s.
                        </p><p>
                            You do not need to remain pointing for the duration the target is on, please return to a resting position (facing the monitor ahead of you) once you are comfortable you have correctly indicated the target. 
                        </p>
                      `
              }, {
                type: "demo",
                name: "targetLED",
                title: "LED Demo",
                titleLocation: "hidden",
                questionText: "This is what they will look like.",
                demoPath: "demo/target/muted"
              }, {
                type: "html",
                html: `A sound will be emitted from the target when it is turned-on to help you localise where the target is.`
              },{
                type: "demo",
                name: "targetSound",
                title: "Sound Demo",
                titleLocation: "hidden",
                questionText: "A sound will be emitted from the target when it is turned-on to help you localise where the target is. This is what it will sound like.",
                demoPath: "demo/target/buzzer"
              },{
                type: "demo",
                name: "targetDiagram",
                title: "Target Diagram Demo",
                titleLocation: "hidden",
                questionText: "This is what the diagram will look like to show the current target LED.",
                demoPath: "demo/target/muted"
              }
        ]}, {
            type: "panel",
            name: "accuratePointing",
            elements: [{
                type: "html",
                html: `
                    <h2>Accurate Pointing</h2>
                    <p>
                        When we ask you to point in an accurate and precise manner, we would like you to, as the name implies, point as accurately as possible at the target LEDs.
                    </p><p> 
                        You should point in such a way that you are very confident that the system system we're developing would recognise exactly where you are pointing at.
                    </p><p>
                        Please do not overexert yourself, or perform a gesture that you find uncomfortable. There are no further limitations.
                    </p></ br><p>
                        Please remember that we are developing a system that is going to be personalised to the way you point
                    </p><p>
                        In that respect please do not do what you think we want to do, do whatever you find most comfortable and intuitive - there is no wrong way of pointing.
                    </p><p>
                        Please give it a go. I'll run a short sequence of targets to give you something to point at.
                    </p><p>
                        <em>If Wearing glasses: </em> Please could you remove you glasses? We need to use special eye-tracking glasses to track your gaze during the study, and unfortunately they cannot be worn over you glasses. As such we will need to check you can still perform the study without your corrective lenses.
                    </p>
                `
                },{
                    type: "demo",
                    name: "targetSequenceAccurate",
                    title: "Target Sequence Demo",
                    titleLocation: "hidden",
                    questionText: "Please give it a go. I'll run a short sequence of targets to give you something to point at.",
                    demoPath: "demo/target/sequence"
                },{
                    type: "demo",
                    name: "targetSequenceAccurateRepeat",
                    title: "Target Sequence Demo",
                    titleLocation: "hidden",
                    questionText: "Was this comfortable, and do you think you were as accurate and precise as possible? </br> Would you like to give it another go?",
                    demoPath: "demo/target/sequence"
                },{
                    type: "html",
                    html: `
                        <p>
                            That's great thank you. 
                        </p><p>
                            You don't have to do that exactly each time, please feel free to change hands or pose throughout the set of trials.
                        </p>
                    `
        }]},{
            type: "panel",
            name: "casualPointing",
            elements: [{
                type: "html",
                html: `
                    <h2>Casual Pointing</h2>
                    <p>
                        When we ask for casual pointing, we're asking you to prioritise pointing as casually as possible, while also being more physically relaxed than the accurate pointing. 
                    </p><p>
                        We would like you to still point at the individual LEDs, but you can assume that the system we're developing will be able to determine where exactly you're pointing at, as long as you attempt to point in the vicinity of the target. 
                    </p><p>
                        Please ensure you are more physically relaxed, exerting less effort, than you were for the accurate pointing you did. There are no further limitations.
                    </p></br><p>
                        Please remember that we are developing a system that is going to be personalised to the way you point.
                    </p><p> 
                        In that respect please do not do what you think we want to do, do whatever you find most comfortable and intuitive - there is no wrong way of pointing.
                    </p>    
                `
                },{
                    type: "demo",
                    name: "targetSequenceCasual",
                    title: "Target Sequence Demo",
                    titleLocation: "hidden",
                    questionText: "Please give it a go. I'll run a short sequence of targets to give you something to point at.",
                    demoPath: "demo/target/sequence"
                },{
                    type: "demo",
                    name: "targetSequenceCasualRepeat",
                    title: "Target Sequence Demo",
                    titleLocation: "hidden",
                    questionText: "Was this comfortable, and do you think you were as casual and relaxed as possible? Would you like to give it another go?",
                    demoPath: "demo/target/sequence"
                },{
                    type: "html",
                    html: `
                        <p>
                            That's great thank you. 
                        </p><p>
                            You don't have to do that exactly each time, please feel free to change hands or pose throughout the set of trials.
                        </p>
                    `
        }]},{
            type: "panel",
            name: "stroop",
            elements: [{
                type: "html",
                html: `
                    <h2>Distractor Task</h2>
                    <p>
                        Now, if you're comfortable with the pointing aspect of this study, we'll walkthrough the distractor task that you will be asked to do for half of the study.
                    </p><p>
                        To better understand how people's pointing is impacted by focus on another task, we'd also like to get you to point while focusing on a different task.
                    </p><p>
                        This task is called the Stroop test and involves reading aloud the colour of the word shown on screen. The twist is that the word itself will be the name of a colour, and it may not match the colour of the word.
                    </p></br><p>
                        During the sessions where you'll be asked to perform the Stroop test you'll also still be required to point at LEDs as an when they appear. However we will ask that you prioritise reading aloud the colour of the word as fast as possible once the word appears.
                    </p><p>
                        The words will only be present for a couple seconds. For each correct colour, you will gain one point, for each incorrect or missed colour, you will lose a point. Please try to get as many points as possible.
                    </p><p>
                        To let you know when a word is shown (in the event you aren't looking at the screen, a tone will be played by the screen. Pointing tasks will be much the same, however they may appear slightly before or slightly after a new word appears on screen, and they will not appear for every word. The targets will remain on for 3 seconds, as they did previously.
                    </p><p>
                        Given we're asking you to read aloud the colour as fast as possible, please ensure you are looking at the screen whenever you can, though you are still permitted to look at targets if you need to.
                    </p>
                `
                },{
                    type: "demo",
                    name: "stroopColours",
                    title: "Stroop Colour",
                    titleLocation: "hidden",
                    questionText: "Let's just go through the colours in the Stroop test, these will cycle through on-screen now.",
                    demoPath: "demo/stroop/colours"
                },{
                    type: "html",
                    html: `
                        <p>
                            Could you clearly make-out the words?
                        </p></br><p>
                            Were there any colours that you think you might mix-up?
                        </p>
                    `
                },{
                    "type": "tagbox",
                    titleLocation: "hidden",
                    "name": "0_1_colour-blindness",
                    "choices": [
                        "RED",
                        "BROWN",
                        "BLUE",
                        "GREEN",
                        "PURPLE",
                    ]
                },{
                    type: "demo",
                    name: "stroopTest",
                    title: "Stroop Test",
                    titleLocation: "hidden",
                    questionText: "Here is an actual example of a Stroop test where the words might not match the colours.",
                    demoPath: "demo/stroop/test"
                },{
                    type: "demo",
                    name: "stroopTestFull",
                    title: "Stroop Test Full",
                    titleLocation: "hidden",
                    questionText: "That's great thank you. Would you like to try while also pointing?",
                    demoPath: "demo/stroop/point"
                },{
                    type: "html",
                    html: `
                        <p>
                            That's great thank you.
                        </p><p>
                            You don't have to do that exactly each time, please feel free to change hands or pose throughout the set of trials.
                        </p>
                    `
        }]},{
            type: "panel",
            name: "participantPrep",
            elements: [{
                type: "html",
                html: `
                <h2>Mocap Preparation</h2>
                <p>
                    Now that we've done the briefing, we'll need to get you ready to be tracked by the motion tracking system.
                </p><p>
                    This won't require too much on your part.
                </p>
                <h3>Glasses</h3>
                <p>
                    The first step will be to wear these glasses. Please put them on so that they are comfortable, and use the clip on the cable to affix the cable to your top, leaving enough give to comfortably move your head without getting caught.
                </p><p>
                    Later we will plug this into a processing unit via the cable, which we will clip to you using this belt...<em>Difficult for them to do clip to themselves, think best to have some form of belt they can wear with a place to clip the unit to.</em>
                </p>
                <h3>Mocap markers</h3>
                <p>
                    In order to track your hands, to know where you're pointing, we are going to stick some small tracking markers in these spots on your hands <em>Show diagram of marker placement</em>.
                </p><p>
                    If you could roll up your sleeves, and you may wish to take off any watches or bracelets <em>Provide a bowl/box to place discarded items</em>.
                </p><p>
                    Please place your hands here, with your fingers slightly flexed. I'll be attaching these markers using some adhesive tape. it should be easy to remove.
                </p><p>
                    <em>This should not be the case, but please let me know if the adhesive is uncomfortable or painful.</em>
                </p>
                `
            },{
                type: "html",
                html: `
                    <h2>Tobii Eye Calibration</h2>
                    <p>
                        Now that you're ready to be tracked, let us calibrate the glasses.
                    </p><p>
                        First let's determine which of your eyes is the dominant one.
                    </p><p>
                        Please stand in the box here (<em>Indicate the box</em>).
                    </p>
                `
            },{
                name: "0_1_eye-dom",
                title: "Which eye is your dominant eye?",
                description: "Please hold your arms at arm's length, then form a triangle with your thumbs and index fingers. I can demo this for you if you'd like. Please look ",
                type: "dropdown",
                choices: [
                    "Left Eye Dominant",
                    "Right Eye Dominant"
                ],
                isRequired: true
            },{
                type: "html",
                html: `
                    <p>
                        Perfect, now we need to calibrate the glasses.
                    </p><p>
                        Please look at the dot in the centre of the black circle on the card in front of you (<em>Indicate the circle</em>)
                    </p><p>
                        Please keep your vision focused on this dot until I say so.
                    </p><p>
                        <em>Once Calibrated</em> - Brilliant, thank you.
                    </p>
                `
            }
        ]
    }]
  };

export const accurateInstructions = {
    type: "html",
    html: `<p>
            In this session we're going to ask you to point as accurately and precisely as possible.
        </p><p>
            As a reminder, by this we mean that you should point as accurately as possible in such a way that you are very confident the system we are developing would recognise exactly where you are pointing at. Please do not overexert yourself, or perform a gesture that you find uncomfortable. There are no further limitations.
        </p></br><p>
            Please remember that we are developing a system that is going to be personalised to the way you point.
        </p><p>
            In that respect, please do not do what you think we want to do, do whatever you find most comfortable and intuitive - there is no wrong way of pointing.
        </p>
    `
}

export const casualInstructions = {
    type: "html",
    html: `<p>
            In this session we're going to ask you to point as casually and relaxed as possible.
        </p><p>
            As a reminder, by this we mean that you are pointing in a more casual manner, and should be more physically relaxed compared to the accurate pointing you have done.
        </p><p>
            We would like you to point at the individual LEDs and you can assume that the system we're developing will be able to determine where exactly you're pointing at, as long as you attempt to point in the vicinity of the target. 
        </p><p>
            Please ensure you are more physically relaxed, exerting less effort, than you were for the accurate pointing you did. There are no further limitations.
        </p></br><p>
            Please remember that we are developing a system that is going to be personalised to the way you point.
        </p><p>
            In that respect, please do not do what you think we want to do, do whatever you find most comfortable and intuitive - there is no wrong way of pointing.
        </p>
    `
}

export const distractorInstructions = {
    type: "html",
    html: `<p>
            In this session you are also going to be performing the Stroop test I showed you in the beginning.
        </p><p>
            As a reminder, on the screen ahead of you there will be a series of words (these will be the names of colours). You need to read aloud the colour of the word, not the word itself.
        </p><p>
            We want you to do this as fast as you can, once a word appears.
        </p></br><p>
            Words will change every 2 seconds, and a tone will sound from the screen when a new one appears.
        </p><p>
            For each correct colour said aloud, you will gain one point. For each wrong or missed colour, you will lose 1 point.
        </p><p>
            If you say the word, or the wrong colour, you may correct yourself before the next word appears.
        </p></br><p>
            Alongside this test, you will also need to still point in the manner I described to you.
        </p><p>
            Please prioritise reading aloud the correct colour as soon as you can. You can try to complete the Stroop test before pointing at the current target, or try pointing while staying focused on the screen. there is no limitations, as long as you prioritise reading aloud the colour as soon as possible, and manage to point at the targets as they appear.
        </p>
    `
}

export function getInstructions(behaviour: string, distractor: boolean): any {
    let instructions: any[] = [{
        type: "html",
        html: `
            <p>
                We are now going to start a session of pointing.
            </p><p>
                This will be split into 3 sections, each containing a sequence of 45 targets.
            </p><p>
                Each target you need to point at will emit a tone when it turns on, and the respective target will be shown on the screen ahead of you. This is to help you locate the target.
            </p><p>
                Each target LED will flash on and off for 3 seconds. You do not need to point at the LED for for the duration, only for as long as you feel necessary to have confidently identified the target in the manner which we ask you to point.
            </p>
        `
    }];
    
    if ("ACCURATE" == behaviour) {
        instructions.push(accurateInstructions);
    } else {
        instructions.push(casualInstructions);
    }
    
    if (distractor) {
        instructions.push(distractorInstructions);
    }
    
    return {
        type: "panel",
        elements: [{
            type: "panel",
            elements: instructions
        },{
            type: "demo",
            titleLocation: "hidden",
            condition: behaviour,
            questionText: "Are you ready to begin?",
            demoPath: distractor ? "study/start/stroop" : "study/start"
        }]
    }
}

export function borgQuestions(i: string) {
    return [{
            type: 'panel',
            elements: [
                {
                    type: "html",
                    html: `<p>
                        This section of questions is intended to provide us with information on how much effort you believe you exerted in performing pointing gestures in the previous task.
                    </ p><p>
                        The format of the questions will involve using the Borg RPE scale. The RPE is a scale of numbers, 6-20, which represent how much effort you believe you exerted.
                        <br>6 represents minimal effort, should not cause any discomfort or raise your heart-rate.
                        <br>9 would indicate the activity required effort, comparable to walking slowly for a few minutes.
                        <br>13 indicates that the work performed was starting to become difficult, but you would still be comfortable to continue.
                        <br>17 is feasible for a healthy person to perform, but would require pushing themselves beyond comfort to continue.<br>19 is extremely strenuous exercise that would likely be the hardest the average person would have experienced.
                    </ p><p>
                        If you have any questions regarding the scale, or would like some examples, please ask the researcher present.
                        </br>If you have any questions regarding the set of targets proposed in the question, please ask the researcher present to clarify.
                    </ p>
                    `
                }
            ]
        },{ type: "panel",
            elements: [
                {
                    name: `${i}_brpe_1`,
                    title: "Pointing in General",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets."
                }, {
                    name: `${i}_brpe_2`,
                    title: "Highest Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets on the left-hand side."
                }, {
                    name: `${i}_brpe_3`,
                    title: "Eye-Level Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets at eye-level."
                }, {
                    name: `${i}_brpe_4`,
                    title: "Lowest Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets below eye-level."
                }, {
                    name: `${i}_brpe_5`,
                    title: "Targets in the Centre",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets in the centre."
                }, {
                    name: `${i}_brpe_6`,
                    title: "Targets on the Left",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets on the left-hand side."
                }, {
                    name: `${i}_brpe_7`,
                    title: "Targets on the Right",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets on the right-hand side."
                }
            ]
        }
    ];
}

export const tlxWeightQuestions = [
    {
        type: "html",
        html: "<p>The first stage in completing a NASA TLX form is to determine what aspects of completing a task are most important to you. This only needs to be done once before the first TLX form you complete.</ p><p>In this section you will have 15 comparisons to pick between.</ p>"
    }, {
        name: "txlweight1",
        // title: "Targets on the Right",
        type: "nasatlxweight",
        isRequired: true,
        questionText: "Please select which aspect of completing a task is most important."
    }
];


export function tlxQuestions(i: string) {
    return [
        {
            type: 'panel',
            elements: [{
                type: "html",
                html: `<p>
                        This section of questions is intended to provide us with information on the work load you experienced across the entire task.
                    </ p><p>
                        The format of the question will involve using the NASA TLX. It is composed of 6 scales, each evaluating a different aspect of work.
                    </ p><p>
                        If you have any questions regarding a specific scale, or the question in general, please ask the researcher present to clarify.
                    </ p>`
            }]
        }, {
            type: "panel",
            elements: [
                {
                    name: `${i}_nasatlx_1`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Mental Demand",
                    scaleDescription: "How mentally demanding was the task?"
                }, {
                    name: `${i}_nasatlx_2`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Physical Demand",
                    scaleDescription: "How physically demanding was the task?"
                }, {
                    name: `${i}_nasatlx_3`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Temporal  Demand",
                    scaleDescription: "How hurried or rushed was the pace of the task?"
                }, {
                    name: `${i}_nasatlx_4`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Performance",
                    scaleDescription: "How successful were you in accomplishing what you were asked to do?"
                }, {
                    name: `${i}_nasatlx_5`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Effort",
                    scaleDescription: "How hard did you have to work to accomplish your level of performance?"
                }, {
                    name: `${i}_nasatlx_6`,
                    titleLocation: "hidden",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Frustration",
                    scaleDescription: "How insecure, discouraged, irritated, stressed, and annoyed were you?"
                }
            ]
        }
    ];
}

export function perceivedAccuracyAndPrecision(i: string) {
    return [
        {
        "type": "matrix",
        "name": `${i}_paap`,
        isRequired: true,
        "title": "Please indicate if you agree or disagree with the following statements",
        "columns": [{
            "value": 7,
            "text": "Strongly Disagree"
        }, {
            "value": 6,
            "text": "Disagree"
        }, {
            "value": 5,
            "text": "Somewhat Disagree"
        }, {
            "value": 4,
            "text": "Neutral"
        }, {
            "value": 3,
            "text": "Somewhat Agree"
        }, {
            "value": 2,
            "text": "Agree"
        }, {
            "value": 1,
            "text": "Strongly Agree"
        }],
        "rows": [
            {
            "value": "accurately",
            "text": "I selected the targets accurately."
            },
            {
            "value": "precisely",
            "text": "I selected the targets precisely."
            }
        ],
        "alternateRows": false,
        "isAllRowRequired": true
        }
    ];
}

export function imi(i: string) {
    return [
        {
            elements: [{
                type: "html",
                html: `<p>
                    This section is the Intrinsic Motivations Inventory (IMI) question set. The purpose of this questionnaire is to assess your subjective experience with the style of pointing employed in your last set of tasks.
                </ p>
                <p>
                    The Intrinsic Motivations Inventory questionnaire uses a scale of 1-7 of how true you believe a statement to be, where 1 is not agreeing with the statement and 7 being total agreement with the statement.
                </ p>`
            }]
        }, {
            type: "panel",
            title: "Interest / Enjoyment",
            elements: [
                {
                    name: `${i}_imi-interest-enjoyment_1`,
                    title: "I enjoyed doing this activity very much",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_2`,
                    title: "This activity was fun to do",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_3`,
                    title: "I thought this was a boring activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_4`,
                    title: "This activity did not hold my attention at all",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_5`,
                    title: "I would describe this activity as very interesting",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_6`,
                    title: "I thought this activity was quite enjoyable",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-interest-enjoyment_7`,
                    title: "While I was doing this activity, I was thinking about how much I enjoyed it",
                    type: "imi",
                    isRequired: true,
                }, 
            ]
        }, {
            type: "panel",
            title: "Perceived Competence",
            elements: [
                {
                    name: `${i}_imi-perceived-competence_1`,
                    title: "I think I am pretty good at this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-perceived-competence_2`,
                    title: "I think I did pretty well at this activity, compared to other participants",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-perceived-competence_3`,
                    title: "After working at this activity for awhile, I felt pretty competent",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-perceived-competence_4`,
                    title: "I am satisfied with my performance at this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-perceived-competence_5`,
                    title: "I was pretty skilled at this activity ",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-perceived-competence_6`,
                    title: "This was an activity that I couldn't do very well",
                    type: "imi",
                    isRequired: true,
                }
            ]
        }, {
            type: "panel",
            title: "Effort / Importance",
            elements: [
                {
                    name: `${i}_imi-effort-importance_1`,
                    title: "I put a lot of effort into this",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-effort-importance_2`,
                    title: "I didn't try very hard to do well at this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-effort-importance_3`,
                    title: "I tried very hard on this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-effort-importance_4`,
                    title: "It was important to me to do well at this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-effort-importance_5`,
                    title: "I didn't put much energy into this",
                    type: "imi",
                    isRequired: true,
                }
            ]
        }, {
            type: "panel",
            title: "Pressure / Tension",
            elements: [
                {
                    name: `${i}_imi-pressure-tension_1`,
                    title: "I did not feel nervous at all while doing this",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-pressure-tension_2`,
                    title: "I felt very tense while doing this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-pressure-tension_3`,
                    title: "I was very relaxed in doing these",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-pressure-tension_4`,
                    title: "I was anxious while working on this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `${i}_imi-pressure-tension_5`,
                    title: "I felt pressured while doing these",
                    type: "imi",
                    isRequired: true,
                }
            ]
        }
    ];
}