import { Component, Injectable, OnInit } from '@angular/core';
import { Model, Serializer, CustomWidgetCollection, ElementFactory, JsonObject} from "survey-core";
import { HttpClient } from '@angular/common/http';
import { QuestionColorPickerModel } from './questions/questions';
import { AngularComponentFactory } from 'survey-angular-ui';
import { ColorPickerComponent } from './questions/color-picker/color-picker.component';

var widget = {
  //the widget name. It should be unique and written in lowcase.
  name: "textwithbutton",
  //the widget title. It is how it will appear on the toolbox of the SurveyJS Editor/Builder
  title: "Text with button",
  //the name of the icon on the toolbox. We will leave it empty to use the standard one
  iconName: "",
  //If the widgets depends on third-party library(s) then here you may check if this library(s) is loaded
  widgetIsLoaded: function () {
      //return typeof $ == "function" && !!$.fn.select2; //return true if jQuery and select2 widget are loaded on the page
      return true; //we do not require anything so we just return true. 
  },
  //SurveyJS library calls this function for every question to check, if it should use this widget instead of default rendering/behavior
  isFit: function (question: any) {
      //we return true if the type of question is textwithbutton
      console.log("Checking if text with button is fit for use");
      return question.getType() === 'textwithbutton';
      //the following code will activate the widget for a text question with inputType equals to date
      //return question.getType() === 'text' && question.inputType === "date";
  },
  //Use this function to create a new class or add new properties or remove unneeded properties from your widget
  //activatedBy tells how your widget has been activated by: property, type or customType
  //property - it means that it will activated if a property of the existing question type is set to particular value, for example inputType = "date" 
  //type - you are changing the behaviour of entire question type. For example render radiogroup question differently, have a fancy radio buttons
  //customType - you are creating a new type, like in our example "textwithbutton"
  activatedByChanged: function (activatedBy: any) {
      //we do not need to check acticatedBy parameter, since we will use our widget for customType only
      //We are creating a new class and derived it from text question type. It means that text model (properties and fuctions) will be available to us
      JsonObject.metaData.addClass("textwithbutton", [], () => null, "text");
      //signaturepad is derived from "empty" class - basic question class
      //Survey.JsonObject.metaData.addClass("signaturepad", [], null, "empty");

      //Add new property(s)
      //For more information go to https://surveyjs.io/Examples/Builder/?id=addproperties#content-docs
      JsonObject.metaData.addProperties("textwithbutton", [
          { name: "buttonText", default: "Click Me" }
      ]);
  },
  //If you want to use the default question rendering then set this property to true. We do not need any default rendering, we will use our our htmlTemplate
  isDefaultRender: false,
  //You should use it if your set the isDefaultRender to false
  htmlTemplate: "<div><input /><button></button></div>",
  //The main function, rendering and two-way binding
  afterRender: function (question: any, el: any) {
      //el is our root element in htmlTemplate, is "div" in our case
      //get the text element
      var text = el.getElementsByTagName("input")[0];
      //set some properties
      text.inputType = question.inputType;
      text.placeholder = question.placeHolder;
      //get button and set some rpoeprties
      var button = el.getElementsByTagName("button")[0];
      button.innerText = question.buttonText;
      button.onclick = function () {
          question.value = "You have clicked me";
      }

      //set the changed value into question value
      text.onchange = function () {
          question.value = text.value;
      }
      var onValueChangedCallback = function () {
          text.value = question.value ? question.value : "";
      }
      var onReadOnlyChangedCallback = function() {
        if (question.isReadOnly) {
          text.setAttribute('disabled', 'disabled');
          button.setAttribute('disabled', 'disabled');
        } else {
          text.removeAttribute("disabled");
          button.removeAttribute("disabled");
        }
      };
      //if question becomes readonly/enabled add/remove disabled attribute
      question.readOnlyChangedCallback = onReadOnlyChangedCallback;
      //if the question value changed in the code, for example you have changed it in JavaScript
      question.valueChangedCallback = onValueChangedCallback;
      //set initial value
      onValueChangedCallback();
      //make elements disabled if needed
      onReadOnlyChangedCallback();

  },
  //Use it to destroy the widget. It is typically needed by jQuery widgets
  willUnmount: function (question: any, el: any) {
      //We do not need to clear anything in our simple example
      //Here is the example to destroy the image picker
      //var $el = $(el).find("select");
      //$el.data('picker').destroy();
  }
}
//Register our widget in singleton custom widget collection
CustomWidgetCollection.Instance.add(widget, "textwithbutton");

const postRoundSurvey = {
  pages: [
    {
      name: "Demographics",
      elements: [{
      //   name: "Age",
      //   title: "Please enter your age:",
      //   type: "text",
      //   inputType: "number",
      //   isRequired: true
      // }, {
      //   name: "Gender",
      //   title: "Please enter the gender you identify as:",
      //   type: "dropdown",
      //   choices: [
      //     "Male",
      //     "Female",
      //     "Non-Binary",
      //     "Prefer Not To Say"
      //   ],
      //   isRequired: true
      // }, {
        type: "text",
        name: "q1",
        placeHolder: "put some text here",
        buttonText: "Custom button text"

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
