import { Component } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

export const CUSTOM_TYPE = "nasatlx"


@Component({
  selector: 'app-nasa-tlx',
  templateUrl: './nasa-tlx.component.html',
  styleUrls: ['./nasa-tlx.component.sass']
})
export class NasaTLXComponent extends QuestionAngular<NasaTLXModel> {
  // need logic here (or a binding) to set the model.value
  updateSurveyValue(newValue: any) {
    this.model.value = newValue;
  }
}
AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", NasaTLXComponent);

export class NasaTLXModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new NasaTLXModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [],
  function () {
    return new NasaTLXModel("");
  },
  "question"
);