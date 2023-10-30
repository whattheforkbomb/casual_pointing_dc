import { Component, OnInit } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

@Component({
  selector: 'app-borg-rpe',
  templateUrl: './borg-rpe.component.html',
  styleUrls: ['./borg-rpe.component.sass']
})
export class BorgRPEComponent extends QuestionAngular<BorgRPEModel> {
  borgScale: number[] = [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20];
  updateSurveyValue(newValue: any) {
    this.model.value = newValue;
  }
}
export const CUSTOM_TYPE = "BorgRPE"

AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", BorgRPEComponent);

export class BorgRPEModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
  get questionText() {
    return this.getPropertyValue("questionText");
  }
  set questionText(val) {
    this.setPropertyValue("questionText", val);
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new BorgRPEModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [{
    name: "questionText",
    category: "general",
    visibleIndex: 3
  }], // Define the additional properties we expect to see
  function () {
    return new BorgRPEModel("");
  },
  "question"
);