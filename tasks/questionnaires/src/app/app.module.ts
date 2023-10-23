import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SurveyModule } from 'survey-angular-ui';
import { HttpClientModule } from '@angular/common/http';
import { NasaTLXComponent } from './questions/nasa-tlx/nasa-tlx.component';
import { BorgRPEComponent } from './questions/borg-rpe/borg-rpe.component';
import { ImiComponent } from './questions/imi/imi.component';
import { LikertComponent } from './questions/likert/likert.component'

@NgModule({
  declarations: [
    AppComponent,
    NasaTLXComponent,
    BorgRPEComponent,
    ImiComponent,
    LikertComponent
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
