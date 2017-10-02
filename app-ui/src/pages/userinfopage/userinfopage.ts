import {Component} from '@angular/core';
import {IonicPage, NavController, NavParams} from 'ionic-angular';
import {LoginService} from "../../services/LoginService";

/**
 * Generated class for the UserinfopagePage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */

@IonicPage()
@Component({
    selector: 'page-userinfopage',
    templateUrl: 'userinfopage.html',
})
export class UserinfopagePage {

    constructor(public navCtrl: NavController, public navParams: NavParams, public loginService : LoginService) {
    }

    dismiss() {
        this.navCtrl.pop(this);
    }

    public getUser(){
        return this.loginService.user;
    }

    ionViewDidLoad() {
        console.log('ionViewDidLoad UserinfopagePage');
    }

}
