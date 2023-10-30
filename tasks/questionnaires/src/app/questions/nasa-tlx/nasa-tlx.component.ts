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
  index = -1
  get value() {
    return this.index;
  }
  set value(i: number) {
    this.index = i;
  }
  updateSurveyValue(newValue: number) {
    this.value = newValue;
    let rounded = Math.ceil(newValue / 5) * 5; // get to nearest 5.
    if (rounded == 0) rounded = 5;
    this.model.value = -1;
    this.model.value = rounded
  }
}
AngularComponentFactory.Instance.registerComponent(CUSTOM_TYPE + "-question", NasaTLXComponent);

export class NasaTLXModel extends Question {
  override getType() {
    return CUSTOM_TYPE;
  }
  get scaleTitle() {
    return this.getPropertyValue("scaleTitle");
  }
  set scaleTitle(val) {
    this.setPropertyValue("scaleTitle", val);
  }
  get scaleDescription() {
    return this.getPropertyValue("scaleDescription");
  }
  set scaleDescription(val) {
    this.setPropertyValue("scaleDescription", val);
  }
}

ElementFactory.Instance.registerElement(CUSTOM_TYPE, (name) => {
  return new NasaTLXModel(name);
});
Serializer.addClass(
  CUSTOM_TYPE,
  [{
    name: "scaleTitle",
    category: "general",
    visibleIndex: 3
  }, {
    name: "scaleDescription",
    category: "general",
    visibleIndex: 4
  }],
  function () {
    return new NasaTLXModel("");
  },
  "question"
);