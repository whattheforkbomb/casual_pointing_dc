import { Component, Injectable, OnInit } from '@angular/core';
import { Model } from "survey-core";
import { HttpClient } from '@angular/common/http';
import { NasaTLXComponent } from './questions/nasa-tlx/nasa-tlx.component';
import { ImiComponent } from './questions/imi/imi.component';
import { BorgRPEComponent } from './questions/borg-rpe/borg-rpe.component';
import { LikertComponent } from './questions/likert/likert.component';

const postRoundSurvey = {
  pages: [
    {
      name: "Demographics",
      elements: [/*{
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
          "Non-Binary",
          "Prefer Not To Say"
        ],
        isRequired: true
      },*/ {
        name: "q1",
        title: "NASA TLX",
        type: "nasatlx",
      }, {
        name: "q2",
        title: "Borg RPE",
        type: "borgrpe"
      }, {
        name: "q3",
        title: "IMI",
        type: "imi"
      }, {
        name: "q4",
        title: "Likert",
        type: "likert"
      }]
    }
  ]
}

const inProgressStorageKey = "study-questionnaire-id"

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
  httpClient!: HttpClient;
  pid!: String;

  constructor(private http: HttpClient) { 
    this.httpClient = http;
  }

  inProgressSaveChanges(sender: Model) {
    sender.data.pageNo = this.surveyModel.currentPageNo;
    window.localStorage.setItem(inProgressStorageKey, JSON.stringify(sender.data));
  }

  surveyComplete(sender: Model) {
    this.httpClient.post(`http://${window.location.hostname}:8080/app/questionnaire/save/${this.pid}`, JSON.stringify(sender.data), {headers: {'Content-Type': 'application/json'}})
  }

  ngOnInit(): void {
    const survey = new Model(postRoundSurvey);
    this.surveyModel = survey;
    this.surveyModel.onCurrentPageChanged.add(this.inProgressSaveChanges);
    this.surveyModel.onComplete.add(this.surveyComplete);
    this.surveyModel.onComplete.add(() => {
      window.localStorage.setItem(inProgressStorageKey, "");
    });
    let incompleteSurveyData = window.localStorage.getItem(inProgressStorageKey) || null;
    if (incompleteSurveyData) {
      let surveyData = JSON.parse(incompleteSurveyData)
      this.surveyModel.data = surveyData;
      if (surveyData.pageNo) {
        this.surveyModel.currentPageNo = surveyData.pageNo;
      }
    }
    this.httpClient.get(`http://${window.location.hostname}:8080/study/pid`, {responseType: 'text'}).subscribe((resp: String) => {
      this.pid = resp;
    })
  }
}
