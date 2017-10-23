import {ChangeDetectorRef, Component} from '@angular/core';
import {IonicPage, ModalController, NavController, NavParams} from 'ionic-angular';
import {ChatService} from "../../services/SocketService";
import {SearchGoogleMapsPage} from "../search-google-maps/search-google-maps";
import {GoogleMapsService} from "../../services/GoogleMapsService";
import {Utility} from "../../services/Utility";
import {LoginService} from "../../services/LoginService";


/**
 * Generated class for the AddModalPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */

@IonicPage()
@Component({

    selector: 'page-add-modal',
    templateUrl: 'add-modal.html',
})
export class AddModalPage {

    autocomplete: google.maps.places.Autocomplete;
    map:google.maps.Map;
    currentLocation:google.maps.LatLng;

    private name: string;
    private address: string;
    private description: string;
    private category: string;
    private isPublic: boolean = true;

    private nameError: string = null;
    private addressError: string = null;
    private categoryError: string = null;
    private error : string = "";

    constructor(public navCtrl: NavController, public navParams: NavParams,
                private socketService: ChatService, private googleMapService : GoogleMapsService,
                private modalCtrl: ModalController, private ref : ChangeDetectorRef,
                private loginService : LoginService) {
        this.currentLocation = navParams.get('currentLocation');
        var setAddress = navParams.get('setAddress');
        if(setAddress)
            this.address = setAddress;
        var setName = navParams.get('setName');
        if(setName)
            this.name = setName;
        if(!this.currentLocation || !this.currentLocation.lat() || !this.currentLocation.lng()) { //checks if undefined, null, or empty
            this.address = "CURRENT_LOCATION";
        }
    }
    initialized(autocomplete: any) {
        this.autocomplete = autocomplete;
    }


    // private subscription: Subscription;
    ngOnInit(){
        // this.subscription = Observable.fromEvent(document,'keypress').subscribe((e:KeyboardEvent)=>{
        //     if(e.key == "Enter")
        //         this.submit();
        // });
    }
    ngOnDestroy(){
        // this.subscription.unsubscribe();
    }

    ngAfterViewInit() {
        var input = document.getElementById('autocomplete').getElementsByTagName('input')[0];
        var options = {componentRestrictions: {country: 'us'},bounds:this.googleMapService.getMap().getBounds()};
        var aut = new google.maps.places.Autocomplete(input, options);
        aut.addListener('place_changed',()=> {
            var places = aut.getPlace();
            if (!places)
                return;
            this.address = places.formatted_address;
            this.currentLocation = places.geometry.location;
        });
    }

    ionViewDidLoad() {
        // console.log('ionViewDidLoad AddModalPage');
    }

    dismiss() {
        this.navCtrl.pop(this);
    }
    getCategories(){
        if(!localStorage.getItem(Utility.categories)) {
            console.log("No categories provided");
            return;
        }
        //TODO this might be able to be inproved by caching the parsed list, not a big deal though
        //TODO add timestamp to last gathered categories time.  Then update the list after a certain amount of time.
        var cats = JSON.parse(localStorage.getItem(Utility.categories));
        var c = [];
        for(var i in cats)
            c.push(cats[i]['name']);
        return c;
    }


    openSearch() {
        let modal = this.modalCtrl.create(SearchGoogleMapsPage);
        modal.present();
    }
    setCurrentLocation(){
        // this.address=this.googleMapService.getCurrentLocation().toString();
        this.address = "CURRENT_LOCATION";
        this.currentLocation = this.googleMapService.getCurrentLocation();
        this.ref.detectChanges();
    }

    submit() {
        var err;
        if(!this.name) { //checks if undefined, null, or empty
            this.nameError = "Username must not be empty";
            err=true;
        }else
            this.nameError = null;
        if(!this.address){ //checks if undefined, null, or empty
            this.addressError = "Address must not be empty";
            err=true;
        }else
            this.addressError = null;
        // if(!this.currentLocation || !this.currentLocation.lat() || !this.currentLocation.lng()){ //checks if undefined, null, or empty
        //     this.addressError = "Address must not be empty";
        //     err=true;
        // }else
        //     this.addressError = null;
        if(!this.category){ //checks if undefined, null, or empty
            this.categoryError = "Category must not be empty";
            err=true;
        }else{
            this.categoryError=null;
            var cats = Utility.getCategories();
            var f = false;
            for(var i in cats)
                if(cats[i] === this.category)
                    f = true;
            if(!f){
                this.categoryError="Invalid category";
                err=true;
            }
        }
        if(!this.loginService || !this.loginService.user || !this.loginService.user.token) {
            this.error = this.error.concat("Login is invalid");
            this.loginService.logOut();
            this.dismiss();
            err=true;
        }
        if(!this.socketService){
            this.error = this.error.concat("No connection to server");
            err=true;
        }
        if(err) {
            console.log(this.error);
            return;
        }
        this.nameError=null; this.categoryError = null; this.addressError = null; this.error = "";

        this.socketService.sendMessageWithDestination(
            JSON.stringify({
                lat: this.currentLocation.lat(), lng: this.currentLocation.lng(),
                eventName: this.name, address: this.address, description: this.description,
                token: this.loginService.user.token, category: this.category, isPublic: this.isPublic
            }),
            "/app/event");
        this.dismiss();
    }
}
