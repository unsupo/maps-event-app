import {Component} from '@angular/core';
import {LoadingController, ModalController, NavController, Platform, ToastController} from 'ionic-angular';
import {Http, Headers} from "@angular/http";
import {AddModalPage} from "../add-modal/add-modal";
import {EventPage} from "../event/event";
import {ChatService} from "../../services/SocketService";
import "rxjs/add/observable/timer";
import "rxjs/add/operator/takeWhile";
import {LoginPage} from "../login/login";
import {Geolocation} from '@ionic-native/geolocation';
import {GoogleMap, GoogleMapOptions, GoogleMaps, GoogleMapsEvent} from "@ionic-native/google-maps";
import {MarkerObj} from "../../services/MarkerObj";
import {GoogleMapsService} from "../../services/GoogleMapsService";
import {User} from "../../services/User";
import {Utility} from "../../services/Utility";
import {LoginService} from "../../services/LoginService";
import {Observable} from "rxjs/Observable";
import {UserinfopagePage} from "../userinfopage/userinfopage";


@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {
    private center: String = "USA";
    private zoom: String = "4";
    private currentLocation: any;
    private currentLocationLatLng: google.maps.LatLng;
    private canGetCurrentLocation: boolean = true;
    private viewLocation: google.maps.LatLng;

    public isPublic : boolean = true;
    public searchEvents : string = "";

    public rootPage: any = HomePage;
    public address;

    private geocoder: any;
    public gMap: any;
    public centerMarker: google.maps.Marker;

    private inputValue: string = "";

    marker: MarkerObj = new MarkerObj();

    public getCurrentLocation(): google.maps.LatLng {
        if (this.currentLocationLatLng && this.currentLocationLatLng.lng() && this.currentLocationLatLng.lat())
            return this.currentLocationLatLng;
        return new google.maps.LatLng(37.09024, -95.712891);
    }

    private loadingWindow;

    constructor(public navCtrl: NavController, private http: Http,
                private modalCtrl: ModalController, private chatService: ChatService,
                private loginService: LoginService,
                public plt: Platform, public geolocation: Geolocation,
                private googleMaps: GoogleMaps, public loadingCtrl: LoadingController,
                public toast: ToastController, private googleMapsService: GoogleMapsService) {

        localStorage.setItem('platform',JSON.stringify(this.plt.platforms()));
        this.loadingWindow = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        this.loadingWindow.present();

        this.chatService.setHome(this);

        this.initialGet();

        var vLocation = JSON.parse(localStorage.getItem('viewLocation'));
        if (vLocation) {
            var ll = JSON.parse(localStorage.getItem('viewLocation'))['location'];
            if (ll.latitude && ll.longitude)
                this.viewLocation = ll;
        }

        var location = JSON.parse(localStorage.getItem('location'));
        if (location)
            location = JSON.parse(localStorage.getItem('location'))['location'];
        if (!(location != null && location.latitude && location.longitude))
            location = vLocation;
        if (location != null && location.latitude && location.longitude) {
            this.currentLocation = location;
            if (typeof google != 'undefined')
                this.currentLocationLatLng = new google.maps.LatLng(this.currentLocation.latitude, this.currentLocation.longitude);
            this.center = this.currentLocation.latitude + "," + this.currentLocation.longitude;
            this.zoom = "10";
        }

        var z = localStorage.getItem('zoom');
        if (z)
            this.zoom = z;
        var zz = localStorage.getItem('isLockedOnCenter');
        if (zz)
            this.googleMapsService.isLockedOnCenter = zz == 'true';

        if (plt.is('core') && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                this.setLocation(position);
                this.canGetCurrentLocation = true;
            }, (err) => {
                this.toast.create({message: "Can't get gps location " + err, duration: 10000}).present();
                this.canGetCurrentLocation = false;
            });
            navigator.geolocation.watchPosition((pos) => {
                this.setLocation(pos);
                this.canGetCurrentLocation = true;
            });
        } else {
            this.geolocation.getCurrentPosition().then((res) => {
                this.setLocation(res);
                this.canGetCurrentLocation = true;
            }).catch((error) => {
                console.log("Error getting location", error);
                this.toast.create({message: "Can't get gps location " + error, duration: 10000}).present();
                this.canGetCurrentLocation = false;
            });
            let watch = this.geolocation.watchPosition();
            watch.subscribe((data) => {
                this.canGetCurrentLocation = true;
                this.setLocation(data);
            });
            this.plt.ready().then(() => {
                this.loadMap();
            });
        }
    }


    public logout() {
        this.loginService.logOut();
    }

    loading: boolean = true;
    auth2: any;

    public isLoggedIn(): boolean {
        return this.loginService.isLoggedIn;
    }

    public getUser(): User {
        return this.loginService.user;
    }

    userProfilePopUp() {
        if(!this.loginService.isLoggedIn) {
            this.loginPopup(() => {
                if (this.loginService.isLoggedIn)
                    this.createAddModal();
            });
            return;
        }
        let modal = this.modalCtrl.create(UserinfopagePage);
        modal.present();
    }

    ngAfterViewInit() {
        this.googleMapsService.initialize('mapm');
        Observable.create((observer)=>{
            if (typeof google != 'undefined') {
                console.log('Google is now defined');
                if(this.googleMapsService && this.googleMapsService.getMap()) {
                    console.log("Google Map is now defined");
                    observer.next(true);
                    observer.complete();
                }
            }
        }).subscribe((res)=>{if(res)this.init()});
    }
    private init() : void {
        google.maps.event.addListenerOnce(this.googleMapsService.getMap(), 'tilesloaded', () => {
            console.log('Google Maps Tiles are now loaded');
            this.loadingWindow.dismiss();
            this.loading = false;
            this.googleMapsService.setCurrentLocation(this.currentLocationLatLng);
            this.googleMapsService.getMap().setCenter(this.currentLocationLatLng);
            this.googleMapsService.getMap().setZoom(+this.zoom);
        });
    }

    clearResults() {
        this.googleMapsService.clearResults();
        this.inputValue = "";
    }

    geocodeCenter(geocode: String) {
        this.geocoder.geocode({'address': geocode}, (result, status) => {
            if (status == google.maps.GeocoderStatus.OK) {
                var loc = result[0].geometry.location;
                this.currentLocationLatLng = loc;
                this.gMap.setCenter(loc);
            } else {
                console.log(status);
            }
        });
    }

    setLocation(position) {
        this.currentLocation = position.coords;
        this.currentLocationLatLng = new google.maps.LatLng(this.currentLocation.latitude, this.currentLocation.longitude);
        this.googleMapsService.setCurrentLocation(this.currentLocationLatLng);
        if (this.centerMarker)
            this.centerMarker.setPosition(this.getCurrentLocation());
        if (this.googleMapsService.isLockedOnCenter && this.gMap)
            this.gMap.setCenter(this.getCurrentLocation());
        this.center = this.currentLocation.latitude + "," + this.currentLocation.longitude;
        this.zoom = "10";
    }

    getMessage() {
        return this.chatService.getResponse();
    }

    sendMsg(msg) {
        this.chatService.sendMessage(msg);
    }

    private initialGet() {
        //Initial get public events
        var id = Utility.uuid();
        if(this.isLoggedIn())
            id = this.loginService.user.userId;
        var h = new Headers();
        h.set('userId',id);
        this.http.post(Utility.getHost().concat('/getEvents'),"",{headers: h})
            .subscribe(
                res => {
                    this.chatService.parseResults(res["_body"].toString())
                },
                err => {
                    console.log(err);
                }
            );
        //TODO initial get categories
        this.http.get(Utility.getHost().concat('/getCategories'))
            .subscribe(res=>{
                localStorage.setItem(Utility.categories,res['_body']);
            }, err => {
                console.log(err);
            });
    }

    public getCategories(){
        return Utility.getCategories();
    }


    getMarkers() {
        if (!this.chatService.objs && this.getObjs().length > 0)
            return;
        var markers = [];
        for (var i in this.getObjs())
            markers.push(this.chatService.objs.get(this.getObjs()[i]));
        return markers;
    }

    copps(arr): any {
        var temp = Array.from(arr);
        return temp;
    }

    getObjs() {
        return this.copps(this.chatService.objs.keys());
    }

    createAddModal() {
        if (!this.loginService.isLoggedIn)
            this.loginPopup(() => {
                if (this.loginService.isLoggedIn)
                    this.createAddModal();
            });
        else
            this.showAddModal();
    }

    showAddModal() {
        let modal = this.modalCtrl.create(AddModalPage, {map: this.gMap, currentLocation: this.getCurrentLocation()});
        modal.present();
    }

    clickObj(obj) {
        let modal = this.modalCtrl.create(EventPage, {obj: this.chatService.objs.get(obj)});
        modal.onDidDismiss((data, role) => {
            this.chatService.objs.get(obj).didClick = false;
        });
        modal.present();
    }

    isGoing(eventName: string): boolean {
        if (!this.loginService.isLoggedIn)
            return false;
        // if (this.user && this.user.eventsGoing.some(x => x.name === eventName))
        //     return true;
        return false;
    }

    imGoingClick(eventName: string): void {
        if (!this.loginService.isLoggedIn) //TODO if going and need to log in at same time
            this.loginPopup(null);
        if (this.loginService.user)
            console.log();
        // this.user.eventsGoing.push(this.chatService.objs.get(eventName));
    }

    imNotGoingClick(eventName: string): void {
        if (!this.loginService.isLoggedIn)
            this.loginPopup(null);
        if (this.loginService.user)
            console.log();
        // this.user.eventsGoing.splice(this.user.eventsGoing.indexOf(this.chatService.objs.get(eventName)), 1);
    }

    loginPopup(callback): void {
        let modal = this.modalCtrl.create(LoginPage);
        if (callback)
            modal.onDidDismiss(callback);
        modal.present();
    }


    //TODO use native api for android.  Maybe this will be more responsive and fast
    map: GoogleMap;
    mapElement: HTMLElement;

    private loadMap() {
        this.mapElement = document.getElementById('map');

        let mapOptions: GoogleMapOptions = {
            camera: {
                target: {
                    lat: this.currentLocation.latitude,
                    lng: this.currentLocation.longitude
                },
                zoom: +this.zoom,
                // tilt: 30
            }
        };

        this.map = this.googleMaps.create(this.mapElement, mapOptions);
        this.map.setMyLocationEnabled(true);

        // Wait the MAP_READY before using any methods.
        this.map.one(GoogleMapsEvent.MAP_READY)
            .then(() => {
                console.log('Map is ready!');

                // Now you can use all methods safely.
                this.map.addMarker({
                    title: 'Ionic',
                    icon: 'blue',
                    animation: 'DROP',
                    position: {
                        lat: this.currentLocation.latitude,
                        lng: this.currentLocation.longitude
                    }
                }).then(marker => {
                    marker.on(GoogleMapsEvent.MARKER_CLICK)
                        .subscribe(() => {
                            alert('clicked');
                        });
                });

            });
    }
}
