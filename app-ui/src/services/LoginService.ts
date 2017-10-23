import {Injectable} from "@angular/core";
import {Http, Headers} from "@angular/http";
import {User} from "./User";
import {Observable} from "rxjs/Observable";
import {ToastController} from "ionic-angular";
import {Utility} from "./Utility";

@Injectable()
export class LoginService{
    public isLoggedIn : boolean = false;
    public user : User;

    private auth2 : any;

    private myClientId: string = '628531512920-5ch9465je33jersarg01eqd1147u7kkm.apps.googleusercontent.com';

    constructor(private http: Http, public toast: ToastController){
        var u = localStorage.getItem(Utility.user);
        if(u) {
            this.user = JSON.parse(u);
            this.isTokenValid(this.user.token).subscribe(res=>{
                if(res) {
                    if(!this.user.email)
                        this.getUserData(this.user.token).subscribe(
                            res => {
                                   this.loggedIn();
                            }
                        );
                    else
                        this.loggedIn();
                }else
                    this.logOut();
            });
        }
    }

    private loggedIn() : void {
        this.isLoggedIn = true;
        localStorage.setItem(Utility.user,JSON.stringify(this.user));
        this.toast.create({message:'Welcome '+this.user.email,duration:2000}).present();
    }

    public initGoogleLogin(elementId : string){
        gapi.load('auth2',  () => {
            this.auth2 = gapi.auth2.init({
                client_id: this.myClientId,
                cookie_policy: 'single_host_origin',
                scope: 'profile email',
            });
            this.attachSignin(document.getElementById(elementId));
        });
    }
    private attachSignin(element) {
        this.auth2.attachClickHandler(element, {},
            (loggedInUser) => {
                this.user = loggedInUser;
                var prof = loggedInUser.getBasicProfile();
                this.user.userId = loggedInUser.getId();
                var name = prof.getName().split(" ");
                this.user.firstName = name[0];
                this.user.lastName = name[1];
                this.user.token = loggedInUser.getAuthResponse().id_token;
                this.user.email = prof.getEmail();
                this.user.imageUrl = prof.getImageUrl();
                this.loggedIn();
            }, function (error) {
                // alert(JSON.stringify(error, undefined, 2));
                console.log(error);
                this.toast.create({message:'Failed To login: '+error,duration:2000}).present();
                this.logOut();
            });
    }

    public register(u : User) : Observable<User> {
        this.user = u;
        var h = new Headers();
        h.set('email',u.email);
        h.set('password',u.password);
        h.set('firstName',u.firstName);
        h.set('lastName',u.lastName);
        return this.http.post(Utility.getHost().concat('/register'),"",{
            headers : h
        }).map(
            res =>{
                this.user.token = res["_body"];
                this.isLoggedIn = true;
                localStorage.setItem(Utility.user,JSON.stringify(this.user));
                return this.user;
            }, err =>{
                this.user = null;
                return null;
            }
        );
    }

    public login(u : User) : Observable<User> {
        this.user = u;
        var h = new Headers();
        h.set('email',u.email);
        h.set('password',u.password);
        return this.http.post(Utility.getHost().concat('/login'),"",{
            headers : h
        }).map(
            res =>{
                this.isLoggedIn = true;
                this.user.token = res["_body"];
                localStorage.setItem(Utility.user,JSON.stringify(this.user));
                return this.user;
            }, err =>{
                this.user = null;
                return null;
            }
        );
    }

    public isTokenValid(token : string) : Observable<boolean> {
        var h = new Headers();
        h.set('token',token);
        return this.http.post(Utility.getHost().concat('/isValidToken'),"",{
            headers: h
        }).map(
            res =>{
                return res["_body"] == 'true';
            }, err =>{
                return false;
            }
        );
    }

    public getUserData(token : string) : Observable<User> {
        var h = new Headers();
        h.set('token',token);
        return this.http.post(Utility.getHost().concat('/getUserData'),"",{
            headers: h
        }).map(
            res =>{
                var json = JSON.parse(res["_body"]);
                var u = new User();
                u.token = json['sessionId'];
                u.email = json['emailAddress'];
                u.lastName = json['lastName'];
                u.firstName = json['firstName'];
                u.locale = json['locale'];
                u.userGroup = json['userGroup'];
                u.imageUrl = json['imageUrl'];
                u.userId = json['userId'];
                u.eventsMade.push(json['eventsMadeIds']);
                u.eventsGoing.push(json['eventsGoingIds']);

                this.user = u;
                localStorage.setItem(Utility.user,JSON.stringify(u));
                return u;
            }, err =>{
                this.user = null;
                return null;
            }
        );
    }

    public loginWithGoogle(token : string) : Observable<boolean> {
        var h = new Headers();
        h.set('token',token);
        return this.http.post(Utility.getHost().concat('/loginWithGoogle'),"",{
            headers: h
        }).map(
            res =>{
                localStorage.setItem("token",res['_body']);
                this.isLoggedIn = true;
                return true;
            }, err =>{
                this.isLoggedIn = false;
                this.user = null;
                return false;
            }
        );

    }

    public logOut() : void {
        this.user = null;
        this.isLoggedIn = false;
        localStorage.clear();
    }
}