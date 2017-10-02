import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { SearchGoogleMapsPage } from './search-google-maps';

@NgModule({
  declarations: [
    SearchGoogleMapsPage,
  ],
  imports: [
    IonicPageModule.forChild(SearchGoogleMapsPage),
  ],
})
export class SearchGoogleMapsPageModule {}
