import {Component} from '@angular/core';
import {IonicPage, NavController, NavParams, ToastController} from 'ionic-angular';
import {LoginService} from "../../services/LoginService";
import {User} from "../../services/User";
import {LoginPage} from "../login/login";

/**
 * Generated class for the SignuppagePage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */

@IonicPage()
@Component({
    selector: 'page-signuppage',
    templateUrl: 'signuppage.html',
})
export class SignuppagePage {
    private email : string = "";
    private password : string = "";
    private passwordRepeat : string = "";
    private firstName : string = "";
    private lastName : string = "";

    private parent:LoginPage;
    constructor(public navCtrl: NavController, public navParams: NavParams,
                public loginService : LoginService, public toast : ToastController) {
        this.parent = navParams.get('login');
    }

    ionViewDidLoad() {
        console.log('ionViewDidLoad SignuppagePage');
    }

    public registerUser() : void {
        //TODO check if passwords match ngModel error if not
        if(this.password != this.passwordRepeat)
            this.toast.create({message: "Passwords Don't match, please try again", duration: 2000}).present();
        var u = new User();
        u.email = this.email;
        u.password = this.password;
        u.firstName = this.firstName;
        u.lastName = this.lastName;
        this.loginService.register(u).subscribe(res=>{
            if(res == null)
                this.errorHandle("");
            this.loginSuccess("");
        }, err =>this.errorHandle(err));
    }

    private loginSuccess(res: string) : void {
        this.toast.create({message: "Hello: "+this.loginService.user.email, duration: 2000}).present();
        this.dismiss();
    }

    private errorHandle(res: string) {
        this.toast.create({message: "Can't login " + res, duration: 2000}).present();
    }

    dismiss(){
        this.parent.dismiss();
    }
}
