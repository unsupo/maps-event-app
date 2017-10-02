import {Component} from '@angular/core';
import {IonicPage, NavController, NavParams} from 'ionic-angular';
import {EventObj} from "../../objects/EventObj";

/**
 * Generated class for the EventPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */

@IonicPage()
@Component({
    selector: 'page-event',
    templateUrl: 'event.html',
})
export class EventPage {
    private obj: EventObj;
    constructor(public navCtrl: NavController, public navParams: NavParams) {
        this.obj = navParams.get("obj");
    }



    ionViewDidLoad() {
        console.log('ionViewDidLoad EventPage');
    }

    dismiss() {
        this.navCtrl.pop(this);
    }
}
