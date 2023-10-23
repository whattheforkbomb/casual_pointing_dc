import { Component } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

@Component({
  selector: 'app-likert',
  templateUrl: './likert.component.html',
  styleUrls: ['./likert.component.sass']
})
export class LikertComponent extends QuestionAngular<LikertModel> {
  // need logic here (or a binding) to set the model.value
  updateSurveyValue(newValue: any) {
    this.model.value = newValue;
  }
}
export const CUSTOM_TYPE = "likert"

AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", LikertComponent);

export class LikertModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new LikertModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [],
  function () {
    return new LikertModel("");
  },
  "question"
);