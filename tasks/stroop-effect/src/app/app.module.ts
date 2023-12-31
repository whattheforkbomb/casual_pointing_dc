import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MatProgressBarModule } from '@angular/material/progress-bar'

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TargetComponent } from './target/target.component';
import { LedFilterPipe } from './led-filter.pipe';
import { LedArrayComponent } from './led-array/led-array.component';

@NgModule({
  declarations: [
    AppComponent,
    TargetComponent,
    LedFilterPipe,
    LedArrayComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatProgressBarModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
