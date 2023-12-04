import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, ViewContainerRef } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

@Component({
  selector: 'app-demo',
  templateUrl: './demo.component.html',
  styleUrls: ['./demo.component.sass']
})
export class DemoComponent extends QuestionAngular<DemoModel> {
  httpClient!: HttpClient;
  constructor(private http: HttpClient, override changeDetectorRef: ChangeDetectorRef,  override viewContainerRef?: ViewContainerRef) { 
    super(changeDetectorRef, viewContainerRef);
    this.httpClient = http;
  }

  startDemo() {
    console.log(this.model.data, this.model.data.getValue("0_1_colour-blindness"));
    let body = this.model.demoPath.includes("start") ? {pointingBehaviour: this.model.condition, colourFilter: this.model.data.getValue("0_1_colour-blindness")} : {};
    this.http.post(`http://${window.location.hostname}:8080/${this.model.demoPath}`, body, {responseType: 'text'}).subscribe((resp: string) => {
      console.log("Demo Request Sent");
    });
  }
}

export const CUSTOM_TYPE = "demo"

AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", DemoComponent);

export class DemoModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
  get questionText() {
    return this.getPropertyValue("questionText");
  }
  set questionText(val) {
    this.setPropertyValue("questionText", val);
  }
  get condition() {
    return this.getPropertyValue("condition");
  }
  set condition(val) {
    this.setPropertyValue("condition", val);
  }
  get demoPath() {
    return this.getPropertyValue("demoPath");
  }
  set demoPath(val) {
    this.setPropertyValue("demoPath", val);
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new DemoModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [{
    name: "questionText",
    category: "general",
    visibleIndex: 3
  },
  {
    name: "demoPath",
    category: "general",
    visibleIndex: 4
  },
  {
    name: "condition",
    category: "general",
    visibleIndex: 5
  }], // Define the additional properties we expect to see
  function () {
    return new DemoModel("");
  },
  "question"
);