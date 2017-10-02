import {Geolocation} from '@ionic-native/geolocation';
import {Injectable} from "@angular/core";
import {ToastController} from "ionic-angular";

@Injectable()
export class CurrentLocationService{
    private currentLocation : any;
    public canGetCurrentLocation : boolean = false;

    constructor(public geolocation: Geolocation, public toast: ToastController){
        if (navigator.geolocation) {
            var options = {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
            };
            navigator.geolocation.getCurrentPosition(position => {
                this.currentLocation = position;
                this.canGetCurrentLocation = true;
            }, (err) => {
                this.toast.create({message: "Can't get gps location " + err, duration: 10000}).present();
                this.canGetCurrentLocation = false;
            },options);
            navigator.geolocation.watchPosition((pos) => {
                this.currentLocation = pos;
                this.canGetCurrentLocation = true;
            }, (err) =>{
                console.log("can't get location");
                this.canGetCurrentLocation = false;
            }, options);
        } else if(this.geolocation){
            this.geolocation.getCurrentPosition().then((res) => {
                this.currentLocation = res;
                this.canGetCurrentLocation = true;
            }).catch((error) => {
                console.log("Error getting location", error);
                this.toast.create({message: "Can't get gps location " + error, duration: 10000}).present();
                this.canGetCurrentLocation = false;
            });
            let watch = this.geolocation.watchPosition();
            watch.subscribe((data) => {
                this.canGetCurrentLocation = true;
                this.currentLocation = data;
            });
        }
    }

    public getCurrentLocation(){
        if(!this.canGetCurrentLocation)
            return null;
        return this.currentLocation;
    }

}