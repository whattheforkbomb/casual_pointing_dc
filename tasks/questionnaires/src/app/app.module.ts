import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SurveyModule } from 'survey-angular-ui';
import { HttpClientModule } from '@angular/common/http';
import { ColorPickerComponent } from './questions/color-picker/color-picker.component'

@NgModule({
  declarations: [
    AppComponent,
    ColorPickerComponent
  ],
  imports: [
    BrowserModule,
    SurveyModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
