import { Component, Injectable, OnInit } from '@angular/core';
import { Model, ComponentCollection, settings as SurveySettings } from "survey-core";
import * as SurveyTheme from "survey-core/themes";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NasaTLXComponent } from './questions/nasa-tlx/nasa-tlx.component';
import { ImiComponent } from './questions/imi/imi.component';
import { BorgRPEComponent } from './questions/borg-rpe/borg-rpe.component';
import { LikertComponent } from './questions/likert/likert.component';
import * as QuestionModels from './questions/questionModels';

/* TODO:
 *  Have welcome page showing PID and button to select relevant condition (starting with precise or casual), or pull from server???
 *  Create model generator, take the condition (standing / sitting (for full session), precise or casual (or whatever we want to call it...))
 *  Need to use angular router, Or update an angular component containing the survey?
 */

const postRoundSurvey = {
  // "progressBarType": "pages",
  // "showProgressBar": "top",
  "fitToContainer": true,
  "showQuestionNumbers": "off",
  // "firstPageIsStarted": true,
  pages: [
    {
      "title": "Welcome",
      "elements": [{
        "type": "html",
        "html": "<p>This Survey is split into 5 sections. The first will be for before you perform any actions within the study, each of the final 4 will be performed after each session within the study. Upon completing each section, please inform the researcher and do not continue filling in questions.</p>"
      }]
    }, 
    QuestionModels.demographics,
    { 
      "title": "Demographics Complete",
      "elements": [{
        "type": "html",
        "html": "<p>Please tell the researcher that you have completed this section of the questionnaire. Do not proceed.</p>"
      }]
    },
    QuestionModels.researcherDemographics,
    {
      "title": "Session 1 - Borg RPE",
      "description": "",
      "elements": QuestionModels.borgQuestions(1)
    }, 
    {
      "title": "Session 1 - NASA TLX",
      "elements": QuestionModels.tlxQuestions(1)
    },
    {
      title: "Session 1 - Perceived Accuracy and Precision",
      elements: QuestionModels.perceivedAccuracyAndPrecision(1)
    },
    {
      title: "Session 4 - IMI",
      elements: QuestionModels.imi(1)
    },
    {
      "title": "Session 1 Complete",
      "elements": [{
        "type": "html",
        "html": "<p>Please tell the researcher that you have completed this section of the questionnaire. Do not proceed.</p>"
      }]
    },
    {
      "title": "Session 2 - Borg RPE",
      "description": "",
      "elements": QuestionModels.borgQuestions(2)
    }, 
    {
      "title": "Session 2 - NASA TLX",
      "elements": QuestionModels.tlxQuestions(2)
    },
    {
      title: "Session 2 - Perceived Accuracy and Precision",
      elements: QuestionModels.perceivedAccuracyAndPrecision(2)
    },
    {
      title: "Session 2 - IMI",
      elements: QuestionModels.imi(2)
    },
    {
      "title": "Session 2 Complete",
      "elements": [{
        "type": "html",
        "html": "<p>Please tell the researcher that you have completed this section of the questionnaire. Do not proceed.</p>"
      }]
    },
    {
      "title": "Session 3 - Borg RPE",
      "description": "",
      "elements": QuestionModels.borgQuestions(3)
    }, 
    {
      "title": "Session 3 - NASA TLX",
      "elements": QuestionModels.tlxQuestions(3)
    },
    {
      title: "Session 3 - Perceived Accuracy and Precision",
      elements: QuestionModels.perceivedAccuracyAndPrecision(3)
    },
    {
      title: "Session 3 - IMI",
      elements: QuestionModels.imi(3)
    },
    {
      "title": "Session 3 Complete",
      "elements": [{
        "type": "html",
        "html": "<p>Please tell the researcher that you have completed this section of the questionnaire. Do not proceed.</p>"
      }]
    },
    {
      "title": "Session 4 - Borg RPE",
      "description": "",
      "elements": QuestionModels.borgQuestions(4)
    }, 
    {
      "title": "Session 4 - NASA TLX",
      "elements": QuestionModels.tlxQuestions(4)
    },
    {
      title: "Session 4 - Perceived Accuracy and Precision",
      elements: QuestionModels.perceivedAccuracyAndPrecision(4)
    },
    {
      title: "Session 4 - IMI",
      elements: QuestionModels.imi(4)
    }
  ],
  completedHtml: "You have now completed the final questionnaire. Please let the researcher know to perform the final interview."
}

// const inProgressStorageKey = "study-questionnaire-id"

const model = new Model(postRoundSurvey);

const headers = new HttpHeaders({
    'Content-Type': 'application/json'
});

@Injectable()
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit {
  static declaration = [NasaTLXComponent, ImiComponent, BorgRPEComponent, LikertComponent];
  title = 'Questionnaire';
  surveyModel!: Model;
  pid!: String;

  constructor(private http: HttpClient) { 
    this.http.get(`http://${window.location.host}/study/pid`, {responseType: 'text'}).subscribe((resp: String) => {
      this.pid = resp;
      console.log("We have a pid.", this.pid, resp);
    })
    this.surveyModel = model;
    console.log("Model", this.surveyModel);

    // this.surveyModel.onCurrentPageChanged.add(this.inProgressSaveChanges);
    this.surveyModel.onComplete.add((sender) => {
      let data = JSON.stringify(sender.data);
      console.log("attempting to save data", data, sender);
      http.post(`http://${window.location.host}/questionnaire/save/${this.pid}`, data, {headers: headers})
        .subscribe(res => {
          console.log("Maybe Sent?");
        });
      console.log("Data hopefully sent");
      
      // window.localStorage.setItem(inProgressStorageKey, "");
      console.log("cleared temp stored progress");
    });
  }

  // inProgressSaveChanges(sender: Model) {
  //   console.log("savingData", sender, model, this.surveyModel);
  //   if (model.currentPageNo == undefined) {
  //     console.log("saving failed, cannot determine page");
  //     return
  //   }
  //   sender.data.pageNo = model.currentPageNo;
  //   console.log("Saving...", sender.data);
  //   // window.localStorage.setItem(inProgressStorageKey, JSON.stringify(sender.data));
  //   // console.log("Saved?", window.localStorage.getItem(inProgressStorageKey));
  // }

  surveyComplete(client: HttpClient, sender: Model) {
    this.http.post(`http://${window.location.host}/questionnaire/save/${this.pid}`, JSON.stringify(sender.data), {headers: {'Content-Type': 'application/json'}}).subscribe;
  }

  ngOnInit(): void {
    // const survey = new Model(postRoundSurvey);
    // this.surveyModel = survey;
    // this.surveyModel.width = "50%";
    // let incompleteSurveyData = window.localStorage.getItem(inProgressStorageKey);
    // if (incompleteSurveyData != null && incompleteSurveyData.length > 0) {
    //   console.log("Loading data", incompleteSurveyData)
    //   let surveyData = JSON.parse(incompleteSurveyData)
    //   this.surveyModel.data = surveyData;
    //   if (surveyData.pageNo) {
    //     this.surveyModel.currentPageNo = surveyData.pageNo;
    //   }
    // }

    // this.surveyModel.applyTheme(SurveyTheme.PlainLight);
    this.surveyModel.applyTheme({
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
          "--sjs-primary-backcolor": "#2772CB",
          "--sjs-primary-backcolor-light": "rgba(39, 114, 203, 0.1)",
          "--sjs-primary-backcolor-dark": "rgba(36, 106, 188, 1)",
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
          "--sjs-article-font-default-textCase": "none",
          "--sjs-font-size": "20px"
      },
      "themeName": "bathhci",
      "colorPalette": "light",
      "isPanelless": false
    });
  }
}
