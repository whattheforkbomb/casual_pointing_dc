import { Component } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

@Component({
  selector: 'app-imi',
  templateUrl: './imi.component.html',
  styleUrls: ['./imi.component.sass']
})
export class ImiComponent extends QuestionAngular<ImiModel> {
  // need logic here (or a binding) to set the model.value
  updateSurveyValue(newValue: any) {
    this.model.value = newValue;
  }
}
export const CUSTOM_TYPE = "imi"

AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", ImiComponent);

export class ImiModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new ImiModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [], // Define the additional properties we expect to see
  function () {
    return new ImiModel("");
  },
  "question"
);