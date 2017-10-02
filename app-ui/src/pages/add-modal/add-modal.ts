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
    // private url: String = "http://localhost:8080";

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
        if (!this.name || !this.loginService || !this.loginService.user || !this.loginService.user.token)
            return;
        this.socketService.sendMessageWithDestination(
            JSON.stringify({
                lat: this.currentLocation.lat(), lng: this.currentLocation.lng(),
                eventName: this.name, address: this.address, description: this.description,
                token: this.loginService.user.token, category: this.category
            }),
            "/app/event");
        this.dismiss();
        // var headers = new Headers();
        // headers.set('eventName',this.name);
        // headers.set('address',this.address);
        //   headers.set('description',this.description);
        // this.http.post(this.url.concat("/addEvent").toString(),"",{
        //   headers: headers
        // }).subscribe(res=>{},err=>{console.log(err)});

    }
}
