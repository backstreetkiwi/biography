import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms'; // <-- NgModel lives here
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { ImagesComponent } from './images/images.component';
import { YearSelectorComponent } from './year-selector/year-selector.component';
import { MonthSelectorComponent } from './month-selector/month-selector.component';
import { DaySelectorComponent } from './day-selector/day-selector.component';
import { ZoomViewerComponent } from './zoom-viewer/zoom-viewer.component';
import { ThumbSizeSelectorComponent } from './thumb-size-selector/thumb-size-selector.component';

@NgModule({
  declarations: [
    AppComponent,
    ImagesComponent,
    YearSelectorComponent,
    MonthSelectorComponent,
    DaySelectorComponent,
    ZoomViewerComponent,
    ThumbSizeSelectorComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
