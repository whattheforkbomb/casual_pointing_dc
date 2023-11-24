export const demographics = {
  name: "Demographics",
  elements: [{
    name: "Age",
    title: "Please enter your age:",
    type: "text",
    inputType: "number",
    isRequired: true
  }, {
    name: "Gender",
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
    name: "VR / AR Exposure",
    title: "Have you used VR or AR headsets before?",
    type: "dropdown",
    choices: [
      "No",
      "Yes - Once or Twice",
      "Yes - A few Times",
      "Yes - Frequently"
    ],
    isRequired: true
  }, {
    name: "Hand Dominance",
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
    name: "Researcher Demographics",
    elements: [{
      name: "Age",
      title: "Please enter your age:",
      type: "text",
      inputType: "number",
      isRequired: true
    }, {
      name: "Gender",
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
      name: "VR / AR Exposure",
      title: "Have you used VR or AR headsets before?",
      type: "dropdown",
      choices: [
        "No",
        "Yes - Once or Twice",
        "Yes - A few Times",
        "Yes - Frequently"
      ],
      isRequired: true
    }]
  };

export function borgQuestions(i: number) {
    return [
        {
            type: "html",
            html: "<p>This section of questions is intended to provide us with information on how much effort you believe you exerted in performing pointing gestures in the previous task.</ p><p>The format of the questions will involve using the Borg RPE scale. The RPE is a scale of numbers, 6-20, which represent how much effort you believe you exerted.<br>6 represents minimal effort, should not cause any discomfort or raise your heart-rate.<br>9 would indicate the activity required effort, comparable to walking slowly for a few minutes.<br>13 indicates that the work performed was starting to become difficult, but you would still be comfortable to continue.<br>17 is feasible for a healthy person to perform, but would require pushing themselves beyond comfort to continue.<br>19 is extremely strenuous exercise that would likely be the hardest the average person would have experienced.</ p><p>If you have any questions regarding the scale, or would like some examples, please ask the researcher present.</br>If you have any questions regarding the set of targets proposed in the question, please ask the researcher present to clarify.</ p>"
        },
        { type: "panel",
            elements: [
                {
                    name: `brpe${i}1`,
                    title: "Pointing in General",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets."
                }, {
                    name: `brpe${i}2`,
                    title: "Highest Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets on the left-hand side."
                }, {
                    name: `brpe${i}3`,
                    title: "Eye-Level Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets at eye-level."
                }, {
                    name: `brpe${i}4`,
                    title: "Lowest Targets",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets below eye-level."
                }, {
                    name: `brpe${i}5`,
                    title: "Targets in the Centre",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets in the centre."
                }, {
                    name: `brpe${i}6`,
                    title: "Targets on the Left",
                    type: "borgrpe",
                    isRequired: true,
                    questionText: "Please rate the effort you required to point at the set of targets on the left-hand side."
                }, {
                    name: `brpe${i}7`,
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


export function tlxQuestions(i: number) {
    return [
        {
            type: "html",
            html: "<p>This section of questions is intended to provide us with information on the work load you experienced across the entire task.</ p><p>The format of the question will involve using the NASA TLX. It is composed of 6 scales, each evaluating a different aspect of work.</ p><p>If you have any questions regarding a specific scale, or the question in general, please ask the researcher present to clarify.</ p>"
        }, {
            type: "panel",
            elements: [
                {
                    name: `nasatlx${i}1`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Mental Demand",
                    scaleDescription: "How mentally demanding was the task?"
                }, {
                    name: `nasatlx${i}2`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Physical Demand",
                    scaleDescription: "How physically demanding was the task?"
                }, {
                    name: `nasatlx${i}3`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Temporal  Demand",
                    scaleDescription: "How hurried or rushed was the pace of the task?"
                }, {
                    name: `nasatlx${i}4`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Performance",
                    scaleDescription: "How successful were you in accomplishing what you were asked to do?"
                }, {
                    name: `nasatlx${i}5`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Effort",
                    scaleDescription: "How hard did you have to work to accomplish your level of performance?"
                }, {
                    name: `nasatlx${i}6`,
                    title: " ",
                    type: "nasatlx",
                    isRequired: true,
                    scaleTitle: "Frustration",
                    scaleDescription: "How insecure, discouraged, irritated, stressed, and annoyed were you?"
                }
            ]
        }
    ];
}

export function perceivedAccuracyAndPrecision(i: number) {
    return [
        {
        "type": "matrix",
        "name": `paap${i}`,
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

export function imi(i: number) {
    return [
        {
            type: "html",
            html: `<p>
                This section is the Intrinsic Motivations Inventory (IMI) question set. The purpose of this questionnaire is to assess your subjective experience with the style of pointing employed in your last set of tasks.
            </ p>
            <p>
                The Intrinsic Motivations Inventory questionnaire uses a scale of 1-7 of how true you believe a statement to be, where 1 is not agreeing with the statement and 7 being total agreement with the statement.
            </ p>`
        }, {
            type: "panel",
            title: "Interest / Enjoyment",
            elements: [
                {
                    name: `imi-interest-enjoyment${i}1`,
                    title: "I enjoyed doing this activity very much",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}2`,
                    title: "This activity was fun to do",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}3`,
                    title: "I thought this was a boring activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}4`,
                    title: "This activity did not hold my attention at all",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}5`,
                    title: "I would describe this activity as very interesting",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}6`,
                    title: "I thought this activity was quite enjoyable",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-interest-enjoyment${i}7`,
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
                    name: `imi-perceived-competence${i}1`,
                    title: "I think I am pretty good at this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-perceived-competence${i}2`,
                    title: "I think I did pretty well at this activity, compared to other participants",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-perceived-competence${i}3`,
                    title: "After working at this activity for awhile, I felt pretty competent",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-perceived-competence${i}4`,
                    title: "I am satisfied with my performance at this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-perceived-competence${i}5`,
                    title: "I was pretty skilled at this activity ",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-perceived-competence${i}6`,
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
                    name: `imi-effort-importance${i}1`,
                    title: "I put a lot of effort into this",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-effort-importance${i}2`,
                    title: "I didn't try very hard to do well at this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-effort-importance${i}3`,
                    title: "I tried very hard on this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-effort-importance${i}4`,
                    title: "It was important to me to do well at this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-effort-importance${i}5`,
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
                    name: `imi-pressure-tension${i}1`,
                    title: "I did not feel nervous at all while doing this",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-pressure-tension${i}2`,
                    title: "I felt very tense while doing this activity",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-pressure-tension${i}3`,
                    title: "I was very relaxed in doing these",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-pressure-tension${i}4`,
                    title: "I was anxious while working on this task",
                    type: "imi",
                    isRequired: true,
                }, {
                    name: `imi-pressure-tension${i}5`,
                    title: "I felt pressured while doing these",
                    type: "imi",
                    isRequired: true,
                }
            ]
        }
    ];
}