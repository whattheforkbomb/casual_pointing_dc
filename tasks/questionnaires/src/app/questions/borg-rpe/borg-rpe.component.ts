import { Component } from '@angular/core';
import { AngularComponentFactory, QuestionAngular } from 'survey-angular-ui';
import { Question, Serializer, ElementFactory } from 'survey-core';

@Component({
  selector: 'app-borg-rpe',
  templateUrl: './borg-rpe.component.html',
  styleUrls: ['./borg-rpe.component.sass']
})
export class BorgRPEComponent extends QuestionAngular<BorgRPEModel> {
  // need logic here (or a binding) to set the model.value
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
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new BorgRPEModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [], // Define the additional properties we expect to see
  function () {
    return new BorgRPEModel("");
  },
  "question"
);