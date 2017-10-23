import {Component} from '@angular/core';
import {IonicPage, NavController, NavParams, ToastController} from 'ionic-angular';
import {LoginPage} from "../login/login";
import {LoginService} from "../../services/LoginService";
import {User} from "../../services/User";
import {Observable} from "rxjs/Observable";
import {Subscription} from "rxjs/Subscription";


/**
 * Generated class for the LoginpagePage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */

// declare const gapi: any;
@IonicPage()
@Component({
    selector: 'page-loginpage',
    templateUrl: 'loginpage.html',
})
export class LoginpagePage {
    private subscription: Subscription;
    private email : string = "";
    private password : string = "";

    parent:LoginPage;
    constructor(public navCtrl: NavController, public navParams: NavParams,
                public toast: ToastController, public loginService : LoginService) {
        this.parent = navParams.get('login');
    }
    private myClientId: string = '628531512920-5ch9465je33jersarg01eqd1147u7kkm.apps.googleusercontent.com';


    ngOnInit(){
        this.subscription = Observable.fromEvent(document,'keypress').subscribe((e:KeyboardEvent)=>{
            if(e.key == "Enter")
                this.submitLogin();
        });
    }

    ngOnDestroy(){
        this.subscription.unsubscribe();
    }

    initGoogleLogin(){
        gapi.signin2.render('my-signin2', {
            'scope': 'profile email',
            'width': 240,
            'height': 50,
            'longtitle': true,
            'theme': 'dark',
            'onsuccess': (googleUser)=>{
                console.log( googleUser);
                this.toast.create({message:'Welcome '+googleUser.getBasicProfile().getName(),duration:2000}).present();
                this.dismiss();
                },
            'onfailure': (error)=>{
                console.log(error);
                this.toast.create({message:'Failed To login: '+error,duration:2000}).present();
            }
        });
    }

    ionViewDidLoad() {
        console.log('ionViewDidLoad LoginpagePage');
    }

    public auth2:any;

    ngAfterViewInit() {

        // this.initGoogleLogin();

        gapi.load('auth2',  () => {
            this.auth2 = gapi.auth2.init({
                client_id: this.myClientId,
                cookie_policy: 'single_host_origin',
                scope: 'profile email',
            });
            this.attachSignin(document.getElementById('glogin'));
        });
    }

    public attachSignin(element) {
        this.auth2.attachClickHandler(element, {},
            (loggedInUser) => {
                console.log( loggedInUser);
                localStorage.setItem("googleUser",JSON.stringify(loggedInUser.getBasicProfile()));
                this.loginService.loginWithGoogle(loggedInUser.getAuthResponse().id_token).subscribe(res=>{
                    if(!res)
                        this.errorHandle("");
                    else{
                        this.loginService.getUserData(loggedInUser.getAuthResponse().id_token).subscribe(res=>{
                            this.loginSuccess(res.firstName);
                        },err=>this.errorHandle(err));
                    }
                }, err =>this.errorHandle(err));
            },  (error)=> {
                this.errorHandle(error);
            });
    }

    public submitLogin() : void {
        var u = new User();
        u.email = this.email;
        u.password = this.password;
        this.loginService.login(u).subscribe(res=>{
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
