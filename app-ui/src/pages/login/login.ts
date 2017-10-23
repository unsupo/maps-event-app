import {Component} from '@angular/core';
import {IonicPage, NavController, NavParams, ToastController} from 'ionic-angular';
import {LoginpagePage} from "../loginpage/loginpage";
import {SignuppagePage} from "../signuppage/signuppage";

/**
 * Generated class for the LoginPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */
// declare const gapi: any;

@IonicPage()
@Component({
    selector: 'page-login',
    templateUrl: 'login.html',
})
export class LoginPage {
    tab1Root = LoginpagePage;
    tab1Params = {login:this};
    tab2Root = SignuppagePage;
    tab2Params = {login:this};

    constructor(public navCtrl: NavController, public navParams: NavParams, public toast: ToastController) {
    }

    public dismiss(){
        this.navCtrl.pop(this);
    }

    // private myClientId: string = '628531512920-5ch9465je33jersarg01eqd1147u7kkm.apps.googleusercontent.com';

    ionViewDidLoad() {
        console.log('ionViewDidLoad LoginpagePage');
    }

    public auth2:any;
    // ngAfterViewInit() {
        // gapi.load('auth2',  () => {
        //     this.auth2 = gapi.auth2.init({
        //         client_id: this.myClientId,
        //         cookiepolicy: 'single_host_origin',
        //         scope: 'profile email'
        //     });
        //     this.attachSignin(document.getElementById('glogin'));
        // });
    // }

    public attachSignin(element) {
        this.auth2.attachClickHandler(element, {},
            (loggedInUser) => {
                console.log( loggedInUser);
                this.toast.create({message:'Welcome '+loggedInUser.getBasicProfile().getName(),duration:2000}).present();
                this.dismiss();
            }, function (error) {
                // alert(JSON.stringify(error, undefined, 2));
                console.log(error);
                this.toast.create({message:'Failed To login: '+error,duration:2000}).present();
            });

    }
}
