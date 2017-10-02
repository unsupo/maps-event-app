import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { AddModalPage } from './add-modal';

@NgModule({
  declarations: [
    AddModalPage,
  ],
  imports: [
    IonicPageModule.forChild(AddModalPage),
  ],
})
export class AddModalPageModule {}
