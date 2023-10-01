import { Component, Injectable, OnInit } from '@angular/core';
import { Model, Serializer, CustomWidgetCollection, ElementFactory } from "survey-core";
import { HttpClient } from '@angular/common/http';
import { QuestionColorPickerModel } from './questions/questions';
import { AngularComponentFactory } from 'survey-angular-ui';
import { ColorPickerComponent } from './questions/color-picker/color-picker.component';

const postRoundSurvey = {
  pages: [
    {
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
          "Non-Binary",
          "Prefer Not To Say"
        ],
        isRequired: true
      }, {
        name: "BorgTest",
        title: "Borg RPE Test",
        type: "borgQuestionType"
      }]
    }
  ]
}
// const surveyHeaders = {'Content-Type': 'application/json'}
const localhost = 'http://localhost:8888'
const inProgressStorageKey = "study-questionnaire-id"

const borgQuestionType = "borg"
AngularComponentFactory.Instance.registerComponent(borgQuestionType + "-question", ColorPickerComponent);

Serializer.addClass(
  borgQuestionType,
  [{
    name: "colorPickerType",
    default: "Slider",
    choices: ["Slider", "Sketch", "Compact"],
    category: "general",
    visibleIndex: 2 // Place after the Name and Title
  }, {
    name: "disableAlpha:boolean",
    dependsOn: "colorPickerType",
    visibleIf: function (obj: any) {
      return obj.colorPickerType === "Sketch";
    },
    category: "general",
    visibleIndex: 3 // Place after the Name, Title, and Color Picker Type
  }],
  function () {
    return new QuestionColorPickerModel("");
  },
  "question"
);

ElementFactory.Instance.registerElement(borgQuestionType, (name) => {
  return new QuestionColorPickerModel(name);
});

// var borgRPEWidget = {
//   name: "borg",
//   title: "Borg RPE",
//   // iconName: "icon-editor",
//   widgetIsLoaded: function () {
//     return true; //We do not have external scripts
//   },
//   isFit: function (question: any) {
//     return question.getType() == "borg";
//   },
//   init() {
//     Serializer.addClass("borg", []);
//   },
//   htmlTemplate:
//     '<div>\
//     <div>\
//       <button onclick="document.execCommand(\'bold\')">Bold</a>\
//       <button onclick="document.execCommand(\'italic\')">Italic</a>\
//       <button onclick="document.execCommand(\'insertunorderedlist\')">List</a>\
//     </div>\
//     <div class="widget_rich_editor" contenteditable=true style="height:200px"></div>\
//   </div>',
//   afterRender: function (question: any, el: any) {
//     var editor = el.getElementsByClassName("widget_rich_editor");
//     if (editor.length == 0) return;
//     editor = editor[0];
//     editor.innerHTML = question.value || "";
//     var changingValue = false;
//     var updateQuestionValue = function () {
//       if (changingValue) return;
//       changingValue = true;
//       question.value = editor.innerHTML;
//       changingValue = false;
//     };
//     if (editor.addEventListener) {
//       var types = [
//         "input",
//         "DOMNodeInserted",
//         "DOMNodeRemoved",
//         "DOMCharacterDataModified",
//       ];
//       for (var i = 0; i < types.length; i++) {
//         editor.addEventListener(types[i], updateQuestionValue, false);
//       }
//     }
//     question.valueChangedCallback = function () {
//       if (changingValue) return;
//       changingValue = true;
//       editor.innerHTML = question.value || "";
//       changingValue = false;
//     };
//     var updateReadOnly = function () {
//       var enabled = !question.isReadOnly;
//       var buttons = el.getElementsByTagName("button");
//       for (var i = 0; i < buttons.length; i++) {
//       }
//     };
//     updateReadOnly();
//     question.readOnlyChangedCallback = function () {
//       updateReadOnly();
//     };
//   },
// };

// CustomWidgetCollection.Instance.add(
//   borgRPEWidget,
//   "borg"
// );

@Injectable()
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit {
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
    this.httpClient.post(`http://localhost:8888/questionnaire/save/${this.pid}`, JSON.stringify(sender.data), {headers: {'Content-Type': 'application/json'}})
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
    this.httpClient.get(`${localhost}/study/pid`, {responseType: 'text'}).subscribe((resp: String) => {
      this.pid = resp;
    })
  }
}
