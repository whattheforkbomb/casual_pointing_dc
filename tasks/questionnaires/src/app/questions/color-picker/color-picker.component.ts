import { Component } from '@angular/core';
import { QuestionAngular } from 'survey-angular-ui';
import { QuestionColorPickerModel } from '../questions';

@Component({
  selector: "color-picker",
  templateUrl: "./color-picker.component.html"
})
export class ColorPickerComponent extends QuestionAngular<QuestionColorPickerModel> {
  handleChange($event: Event) {
    console.log("Testing");
  }
}