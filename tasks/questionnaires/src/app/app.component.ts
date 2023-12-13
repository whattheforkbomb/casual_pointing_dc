import { Component, Injectable, OnInit } from '@angular/core';
import { Model, ComponentCollection, settings as SurveySettings } from "survey-core";
import * as SurveyTheme from "survey-core/themes";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NasaTLXComponent } from './questions/nasa-tlx/nasa-tlx.component';
import { ImiComponent } from './questions/imi/imi.component';
import { BorgRPEComponent } from './questions/borg-rpe/borg-rpe.component';
import { LikertComponent } from './questions/likert/likert.component';
import * as QuestionModels from './questions/questionModels';
import { DemoComponent } from './questions/demo/demo.component';

/* TODO:
 *  Have welcome page showing PID and button to select relevant condition (starting with precise or casual), or pull from server???
 *  Create model generator, take the condition (standing / sitting (for full session), precise or casual (or whatever we want to call it...))
 *  Need to use angular router, Or update an angular component containing the survey?
 */

function getSurvey(conditionOrder: string[]) {
  return {
    // "progressBarType": "pages",
    // "showProgressBar": "top",
    "fitToContainer": true,
    "showQuestionNumbers": "off",
    // "firstPageIsStarted": true,
    pages: [
      {
        "title": "Welcome",
        type: "panel",
        "elements": [{
          type: "panel",
          elements: [{
          "type": "html",
          "html": `
            <p>
              Thank you for signing-up to take part in this study!
            </p><p>
              Please take a look through the Participant Information Sheet provided to you before continuing with this questionnaire. The information sheet will describe in detail the purpose for the study, what is expected of your involvement within the study, and how data collected from the study will be managed.
            </p><p>
              If you have any questions regarding the nature of the study, please ask the researcher present.
            </p></br><p>
              Once you have read through the information sheet, please begin press the 'Start' button below to begin the questionnaire.
            </p>
            `
        }]}]
      }, 
      QuestionModels.demographics,
      { 
        "title": "Demographics Complete",
        name: "complete0_0",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
          }]
        }]
      },
      QuestionModels.researcherDemographics,
      {
        name: "complete0_1",
        elements: [{
          type: "panel",
          elements: [{
            type: "html",
            html: "Prepare first session."
          }]
        }]
      },
      QuestionModels.getInstructions(conditionOrder[0], false),
      {
        "title": "Session 1.1 - Borg RPE",
        "elements": QuestionModels.borgQuestions("1_1")
      }, 
      {
        "title": "Session 1.1 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("1_1")
      },
      {
        title: "Session 1.1 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("1_1")
      },
      {
        "title": "Session 1.1 Complete",
        name: "complete1_1",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
          }]
        }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[0],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 1.2 - Borg RPE",
        "elements": QuestionModels.borgQuestions("1_2")
      }, 
      {
        "title": "Session 1.2 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("1_2")
      },
      {
        title: "Session 1.2 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("1_2")
      },      
      {
        "title": "Session 1.2 Complete",
        name: "complete1_2",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
          }]
        }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[0],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 1.3 - Borg RPE",
        "elements": QuestionModels.borgQuestions("1_3")
      }, 
      {
        "title": "Session 1.3 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("1_3")
      },
      {
        title: "Session 1.3 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("1_3")
      },
      {
        title: "Session 1.3 - IMI",
        elements: QuestionModels.imi("1_3")
      },
      
      {
        "title": "Session 1.3 Complete",
        name: "complete1_3",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
          }]
        }]
      },
      {
        title: "Stroop Introduction", 
        elements: [QuestionModels.stroopFamiliarity]
      },
      QuestionModels.getInstructions(conditionOrder[0], true),
      {
        "title": "Session 2.1 - Borg RPE",
        "elements": QuestionModels.borgQuestions("2_1")
      }, 
      {
        "title": "Session 2.1 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("2_1")
      },
      {
        title: "Session 2.1 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("2_1")
      },
      {
        "title": "Session 2.1 Complete",
        name: "complete2_1",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
          }]
        }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[0],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 2.2 - Borg RPE",
        "elements": QuestionModels.borgQuestions("2_2")
      }, 
      {
        "title": "Session 2.2 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("2_2")
      },
      {
        title: "Session 2.2 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("2_2")
      },
      {
        "title": "Session 2.2 Complete",
        name: "complete2_2",
        "elements": [{
          type: "panel",
          elements: [{
            "type": "html",
            "html": `<p>
            Thank you for completing this section of the questionnaire.
          </p><p>
              Please hand the tablet back to the researcher. <em>Do not proceed.</em>
          </p>`
          }]
        }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[0],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 2.3 - Borg RPE",
        "elements": QuestionModels.borgQuestions("2_3")
      }, 
      {
        "title": "Session 2.3 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("2_3")
      },
      {
        title: "Session 2.3 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("2_3")
      },
      {
        title: "Session 2.3 - IMI",
        elements: QuestionModels.imi("2_3")
      },
      {
        "title": "Session 2.3 Complete",
        name: "complete2_3",
        "elements": [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
            }]
          }]
      },
      {
        title: "Interview 1",
        elements: QuestionModels.interviewQuestions(conditionOrder[0], false)
      },
      QuestionModels.getInstructions(conditionOrder[1], false),
      {
        "title": "Session 3.1 - Borg RPE",
        "elements": QuestionModels.borgQuestions("3_1")
      }, 
      {
        "title": "Session 3.1 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("3_1")
      },
      {
        title: "Session 3.1 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("3_1")
      },
      {
        name: "complete3_1",
        title: "Session 3.1 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
            }]
          }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[1],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 3.2 - Borg RPE",
        "elements": QuestionModels.borgQuestions("3_2")
      }, 
      {
        "title": "Session 3.2 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("3_2")
      },
      {
        title: "Session 3.2 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("3_2")
      },
      {
        name: "complete3_2",
        title: "Session 3.2 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
            }]
          }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[1],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 3.3 - Borg RPE",
        "elements": QuestionModels.borgQuestions("3_3")
      }, 
      {
        "title": "Session 3.3 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("3_3")
      },
      {
        title: "Session 3.3 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("3_3")
      },
      {
        title: "Session 3.3 - IMI",
        elements: QuestionModels.imi("3_3")
      },
      {
        name: "complete3_3",
        title: "Session 3.3 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
          }]
        }]
      },
      QuestionModels.getInstructions(conditionOrder[1], true),
      {
        "title": "Session 4.1 - Borg RPE",
        "elements": QuestionModels.borgQuestions("4_1")
      }, 
      {
        "title": "Session 4.1 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("4_1")
      },
      {
        title: "Session 4.1 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("4_1")
      },
      {
        name: "complete4_1",
        title: "Session 4.1 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
            }]
          }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[1],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 4.2 - Borg RPE",
        "elements": QuestionModels.borgQuestions("4_2")
      }, 
      {
        "title": "Session 4.2 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("4_2")
      },
      {
        title: "Session 4.2 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("4_2")
      },
      {
        name: "complete4_2",
        title: "Session 4.2 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
                Thank you for completing this section of the questionnaire.
              </p><p>
                  Please hand the tablet back to the researcher. <em>Do not proceed.</em>
              </p>`
            }]
          }]
      },
      {
        title: "Resume",
        "elements": [{
          type: "demo",
          titleLocation: "hidden",
          condition: conditionOrder[1],
          questionText: "Are you ready to continue?",
          demoPath: "study/resume"
        }]
      },
      {
        "title": "Session 4.3 - Borg RPE",
        "elements": QuestionModels.borgQuestions("4_3")
      }, 
      {
        "title": "Session 4.3 - NASA TLX",
        "elements": QuestionModels.tlxQuestions("4_3")
      },
      {
        title: "Session 4.3 - Perceived Accuracy and Precision",
        elements: QuestionModels.perceivedAccuracyAndPrecision("4_3")
      },
      {
        title: "Session 4.3 - IMI",
        elements: QuestionModels.imi("4_3")
      },
      {
        name: "complete4_3",
        title: "Session 4.3 Complete",
        elements: [{
          type: "panel",
          elements: [{
              "type": "html",
              "html": `<p>
              Thank you for completing this section of the questionnaire. </br> You have now completed the final questionnaire. Please let the researcher know to perform the final interview.
            </p><p>
                Please hand the tablet back to the researcher. <em>Do not proceed.</em>
            </p>`
          }]
        }]
      },
      {
        title: "Interview 2",
        elements: QuestionModels.interviewQuestions(conditionOrder[1], true)
      }
    ]
  };
}

const inProgressStorageKey = "study-questionnaire-id"

// const model = new Model(postRoundSurvey);

const headers = new HttpHeaders({
    'Content-Type': 'application/json'
});

interface ParticipantInfo {
  PID: string;
  condition: string[];
}

@Injectable()
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent /* implements OnInit */ {
  static declaration = [NasaTLXComponent, ImiComponent, BorgRPEComponent, LikertComponent, DemoComponent];
  title = 'Questionnaire';
  surveyModel!: Model;

  showSurvey: boolean = false;
  PID: string = "";
  condition: string[] = [];
  httpClient!: HttpClient;

  constructor(private http: HttpClient) { 
    this.httpClient = http;
  }

  // Need the http client
  // send call to UI on button click

  generateParticipantDetails(pIdxStr: string) {
    window.localStorage.setItem(inProgressStorageKey, "");
    let pIdx = parseInt(pIdxStr);
    this.http.post(`http://${window.location.hostname}:8080/study/pid/${pIdx}`, {}, {responseType: 'text'}).subscribe((resp: string) => {
      let parsedResp = <ParticipantInfo>JSON.parse(resp);
      this.PID = parsedResp.PID;
      this.condition = parsedResp.condition;
      console.log("We have new pid.", this.PID, this.condition);
    })
  }

  fetchParticipantDetails() {
    this.http.get(`http://${window.location.hostname}:8080/study/pid`, {responseType: 'text'}).subscribe((resp: string) => {
      let parsedResp = <ParticipantInfo>JSON.parse(resp);
      this.PID = parsedResp.PID;
      this.condition = parsedResp.condition;
      console.log("We fetch a pid.", this.PID, this.condition);
    })
  }

  startSurvey() {
    const survey = new Model(getSurvey(this.condition));
    this.surveyModel = survey;
    this.applyTheme(this.surveyModel);
    this.surveyModel.width = "50%";
    let incompleteSurveyData = window.localStorage.getItem(inProgressStorageKey);
    if (incompleteSurveyData != null && incompleteSurveyData.length > 0) {
      console.log("Loading data", incompleteSurveyData)
      let surveyData = JSON.parse(incompleteSurveyData)
      this.surveyModel.data = surveyData;
      if (surveyData.currentPageNo) {
        this.surveyModel.currentPageNo = surveyData.currentPageNo;
      }
    }
    this.surveyModel.onCurrentPageChanged.add((sender: Model) => {
      // console.log(this.surveyModel.currentPage);
      if (this.surveyModel.currentPage.name.includes("complete")) {
        let section = this.surveyModel.currentPage.name.slice(-3);
        console.log("temp save: ", section);
        let filteredData = Object.keys(this.surveyModel.data)
          .filter(key => key.includes(`${section}_`))
          .reduce((obj, key) => {
            return {
              ...obj,
              [key]: this.surveyModel.data[key]
            }
          }, {});
        let data = JSON.stringify(filteredData);
        this.httpClient.post(`http://${window.location.hostname}:8080/questionnaire/save/${this.PID}/section_${section}`, data, {headers: headers})
          .subscribe(res => {
            console.log("Maybe Sent?");
          });
      }
      this.inProgressSaveChanges(sender, this.surveyModel.currentPageNo, this.surveyModel.currentPage.name);
    });
    this.surveyModel.onComplete.add((sender) => {
      let data = JSON.stringify(sender.data);
      console.log("attempting to save data", data, sender);
      this.httpClient.post(`http://${window.location.hostname}:8080/questionnaire/save/${this.PID}/FULL`, data, {headers: headers})
        .subscribe(res => {
          console.log("Maybe Sent?");
        });
      console.log("Data hopefully sent");
      
      window.localStorage.setItem(inProgressStorageKey, "");
      console.log("cleared temp stored progress");
    });

    this.showSurvey = true;
  }


  inProgressSaveChanges(sender: Model, pageNum: number, title: string) {
    console.log("savingData", sender, pageNum, title);
    if (pageNum == undefined) {
      console.log("saving failed, cannot determine page");
      return;
    }
    sender.data.currentPageNo = pageNum;
    // console.log("Saving...", sender.data);
    window.localStorage.setItem(inProgressStorageKey, JSON.stringify(sender.data));
    console.log("Saved?", window.localStorage.getItem(inProgressStorageKey));
  }

  applyTheme(model: Model) {
      model.applyTheme({
        "backgroundImageFit": "cover",
        "backgroundImageAttachment": "scroll",
        "backgroundOpacity": 1,
        "cssVariables": {
            "--sjs-general-backcolor": "rgba(255, 255, 255, 1)",
            "--sjs-general-backcolor-dark": "rgba(248, 248, 248, 1)",
            "--sjs-general-backcolor-dim": "rgba(243, 243, 243, 1)",
            "--sjs-general-backcolor-dim-light": "rgba(249, 249, 249, 1)",
            "--sjs-general-backcolor-dim-dark": "rgba(243, 243, 243, 1)",
            "--sjs-general-forecolor": "rgba(0, 0, 0, 0.91)",
            "--sjs-general-forecolor-light": "rgba(0, 0, 0, 0.45)",
            "--sjs-general-dim-forecolor": "rgba(0, 0, 0, 0.91)",
            "--sjs-general-dim-forecolor-light": "rgba(0, 0, 0, 0.45)",
            "--sjs-primary-backcolor": "#094685",
            "--sjs-primary-backcolor-light": "rgba(9, 70, 133, 0.1)",
            "--sjs-primary-backcolor-dark": "rgba(8, 62, 118, 1)",
            "--sjs-primary-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-primary-forecolor-light": "rgba(255, 255, 255, 0.25)",
            "--sjs-base-unit": "8px",
            "--sjs-corner-radius": "4px",
            "--sjs-secondary-backcolor": "rgba(255, 152, 20, 1)",
            "--sjs-secondary-backcolor-light": "rgba(255, 152, 20, 0.1)",
            "--sjs-secondary-backcolor-semi-light": "rgba(255, 152, 20, 0.25)",
            "--sjs-secondary-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-secondary-forecolor-light": "rgba(255, 255, 255, 0.25)",
            "--sjs-shadow-small": "0px 1px 2px 0px rgba(0, 0, 0, 0.15)",
            "--sjs-shadow-medium": "0px 2px 6px 0px rgba(0, 0, 0, 0.1)",
            "--sjs-shadow-large": "0px 8px 16px 0px rgba(0, 0, 0, 0.1)",
            "--sjs-shadow-inner": "inset 0px 1px 2px 0px rgba(0, 0, 0, 0.15)",
            "--sjs-border-light": "rgba(0, 0, 0, 0.09)",
            "--sjs-border-default": "rgba(0, 0, 0, 0.16)",
            "--sjs-border-inside": "rgba(0, 0, 0, 0.16)",
            "--sjs-special-red": "rgba(229, 10, 62, 1)",
            "--sjs-special-red-light": "rgba(229, 10, 62, 0.1)",
            "--sjs-special-red-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-special-green": "rgba(25, 179, 148, 1)",
            "--sjs-special-green-light": "rgba(25, 179, 148, 0.1)",
            "--sjs-special-green-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-special-blue": "rgba(67, 127, 217, 1)",
            "--sjs-special-blue-light": "rgba(67, 127, 217, 0.1)",
            "--sjs-special-blue-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-special-yellow": "rgba(255, 152, 20, 1)",
            "--sjs-special-yellow-light": "rgba(255, 152, 20, 0.1)",
            "--sjs-special-yellow-forecolor": "rgba(255, 255, 255, 1)",
            "--sjs-article-font-xx-large-textDecoration": "none",
            "--sjs-article-font-xx-large-fontWeight": "700",
            "--sjs-article-font-xx-large-fontStyle": "normal",
            "--sjs-article-font-xx-large-fontStretch": "normal",
            "--sjs-article-font-xx-large-letterSpacing": "0",
            "--sjs-article-font-xx-large-lineHeight": "64px",
            "--sjs-article-font-xx-large-paragraphIndent": "0px",
            "--sjs-article-font-xx-large-textCase": "none",
            "--sjs-article-font-x-large-textDecoration": "none",
            "--sjs-article-font-x-large-fontWeight": "700",
            "--sjs-article-font-x-large-fontStyle": "normal",
            "--sjs-article-font-x-large-fontStretch": "normal",
            "--sjs-article-font-x-large-letterSpacing": "0",
            "--sjs-article-font-x-large-lineHeight": "56px",
            "--sjs-article-font-x-large-paragraphIndent": "0px",
            "--sjs-article-font-x-large-textCase": "none",
            "--sjs-article-font-large-textDecoration": "none",
            "--sjs-article-font-large-fontWeight": "700",
            "--sjs-article-font-large-fontStyle": "normal",
            "--sjs-article-font-large-fontStretch": "normal",
            "--sjs-article-font-large-letterSpacing": "0",
            "--sjs-article-font-large-lineHeight": "40px",
            "--sjs-article-font-large-paragraphIndent": "0px",
            "--sjs-article-font-large-textCase": "none",
            "--sjs-article-font-medium-textDecoration": "none",
            "--sjs-article-font-medium-fontWeight": "700",
            "--sjs-article-font-medium-fontStyle": "normal",
            "--sjs-article-font-medium-fontStretch": "normal",
            "--sjs-article-font-medium-letterSpacing": "0",
            "--sjs-article-font-medium-lineHeight": "32px",
            "--sjs-article-font-medium-paragraphIndent": "0px",
            "--sjs-article-font-medium-textCase": "none",
            "--sjs-article-font-default-textDecoration": "none",
            "--sjs-article-font-default-fontWeight": "400",
            "--sjs-article-font-default-fontStyle": "normal",
            "--sjs-article-font-default-fontStretch": "normal",
            "--sjs-article-font-default-letterSpacing": "0",
            "--sjs-article-font-default-lineHeight": "28px",
            "--sjs-article-font-default-paragraphIndent": "0px",
            "--sjs-article-font-default-textCase": "none"
        },
        "themeName": "default_exported",
        "colorPalette": "light",
        "isPanelless": false
    });
  }

  // ngOnInit(): void {
    
  //   // this.surveyModel.applyTheme({
  //   //   "backgroundImageFit": "cover",
  //   //   "backgroundImageAttachment": "scroll",
  //   //   "backgroundOpacity": 1,
  //   //   "cssVariables": {
  //   //       "--sjs-general-backcolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-general-backcolor-dark": "rgba(248, 248, 248, 1)",
  //   //       "--sjs-general-backcolor-dim": "rgba(243, 243, 243, 1)",
  //   //       "--sjs-general-backcolor-dim-light": "rgba(249, 249, 249, 1)",
  //   //       "--sjs-general-backcolor-dim-dark": "rgba(243, 243, 243, 1)",
  //   //       "--sjs-general-forecolor": "rgba(0, 0, 0, 0.91)",
  //   //       "--sjs-general-forecolor-light": "rgba(0, 0, 0, 0.45)",
  //   //       "--sjs-general-dim-forecolor": "rgba(0, 0, 0, 0.91)",
  //   //       "--sjs-general-dim-forecolor-light": "rgba(0, 0, 0, 0.45)",
  //   //       "--sjs-primary-backcolor": "#2772CB",
  //   //       "--sjs-primary-backcolor-light": "rgba(39, 114, 203, 0.1)",
  //   //       "--sjs-primary-backcolor-dark": "rgba(36, 106, 188, 1)",
  //   //       "--sjs-primary-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-primary-forecolor-light": "rgba(255, 255, 255, 0.25)",
  //   //       "--sjs-base-unit": "8px",
  //   //       "--sjs-corner-radius": "4px",
  //   //       "--sjs-secondary-backcolor": "rgba(255, 152, 20, 1)",
  //   //       "--sjs-secondary-backcolor-light": "rgba(255, 152, 20, 0.1)",
  //   //       "--sjs-secondary-backcolor-semi-light": "rgba(255, 152, 20, 0.25)",
  //   //       "--sjs-secondary-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-secondary-forecolor-light": "rgba(255, 255, 255, 0.25)",
  //   //       "--sjs-shadow-small": "0px 1px 2px 0px rgba(0, 0, 0, 0.15)",
  //   //       "--sjs-shadow-medium": "0px 2px 6px 0px rgba(0, 0, 0, 0.1)",
  //   //       "--sjs-shadow-large": "0px 8px 16px 0px rgba(0, 0, 0, 0.1)",
  //   //       "--sjs-shadow-inner": "inset 0px 1px 2px 0px rgba(0, 0, 0, 0.15)",
  //   //       "--sjs-border-light": "rgba(0, 0, 0, 0.09)",
  //   //       "--sjs-border-default": "rgba(0, 0, 0, 0.16)",
  //   //       "--sjs-border-inside": "rgba(0, 0, 0, 0.16)",
  //   //       "--sjs-special-red": "rgba(229, 10, 62, 1)",
  //   //       "--sjs-special-red-light": "rgba(229, 10, 62, 0.1)",
  //   //       "--sjs-special-red-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-special-green": "rgba(25, 179, 148, 1)",
  //   //       "--sjs-special-green-light": "rgba(25, 179, 148, 0.1)",
  //   //       "--sjs-special-green-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-special-blue": "rgba(67, 127, 217, 1)",
  //   //       "--sjs-special-blue-light": "rgba(67, 127, 217, 0.1)",
  //   //       "--sjs-special-blue-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-special-yellow": "rgba(255, 152, 20, 1)",
  //   //       "--sjs-special-yellow-light": "rgba(255, 152, 20, 0.1)",
  //   //       "--sjs-special-yellow-forecolor": "rgba(255, 255, 255, 1)",
  //   //       "--sjs-article-font-xx-large-textDecoration": "none",
  //   //       "--sjs-article-font-xx-large-fontWeight": "700",
  //   //       "--sjs-article-font-xx-large-fontStyle": "normal",
  //   //       "--sjs-article-font-xx-large-fontStretch": "normal",
  //   //       "--sjs-article-font-xx-large-letterSpacing": "0",
  //   //       "--sjs-article-font-xx-large-lineHeight": "64px",
  //   //       "--sjs-article-font-xx-large-paragraphIndent": "0px",
  //   //       "--sjs-article-font-xx-large-textCase": "none",
  //   //       "--sjs-article-font-x-large-textDecoration": "none",
  //   //       "--sjs-article-font-x-large-fontWeight": "700",
  //   //       "--sjs-article-font-x-large-fontStyle": "normal",
  //   //       "--sjs-article-font-x-large-fontStretch": "normal",
  //   //       "--sjs-article-font-x-large-letterSpacing": "0",
  //   //       "--sjs-article-font-x-large-lineHeight": "56px",
  //   //       "--sjs-article-font-x-large-paragraphIndent": "0px",
  //   //       "--sjs-article-font-x-large-textCase": "none",
  //   //       "--sjs-article-font-large-textDecoration": "none",
  //   //       "--sjs-article-font-large-fontWeight": "700",
  //   //       "--sjs-article-font-large-fontStyle": "normal",
  //   //       "--sjs-article-font-large-fontStretch": "normal",
  //   //       "--sjs-article-font-large-letterSpacing": "0",
  //   //       "--sjs-article-font-large-lineHeight": "40px",
  //   //       "--sjs-article-font-large-paragraphIndent": "0px",
  //   //       "--sjs-article-font-large-textCase": "none",
  //   //       "--sjs-article-font-medium-textDecoration": "none",
  //   //       "--sjs-article-font-medium-fontWeight": "700",
  //   //       "--sjs-article-font-medium-fontStyle": "normal",
  //   //       "--sjs-article-font-medium-fontStretch": "normal",
  //   //       "--sjs-article-font-medium-letterSpacing": "0",
  //   //       "--sjs-article-font-medium-lineHeight": "32px",
  //   //       "--sjs-article-font-medium-paragraphIndent": "0px",
  //   //       "--sjs-article-font-medium-textCase": "none",
  //   //       "--sjs-article-font-default-textDecoration": "none",
  //   //       "--sjs-article-font-default-fontWeight": "400",
  //   //       "--sjs-article-font-default-fontStyle": "normal",
  //   //       "--sjs-article-font-default-fontStretch": "normal",
  //   //       "--sjs-article-font-default-letterSpacing": "0",
  //   //       "--sjs-article-font-default-lineHeight": "28px",
  //   //       "--sjs-article-font-default-paragraphIndent": "0px",
  //   //       "--sjs-article-font-default-textCase": "none",
  //   //       "--sjs-font-size": "20px"
  //   //   },
  //   //   "themeName": "bathhci",
  //   //   "colorPalette": "light",
  //   //   "isPanelless": false
  //   // });
  // }
}
