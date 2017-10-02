import {HomePage} from "../pages/home/home";
import {GoogleMapsService} from "../services/GoogleMapsService";
import {Utility} from "../services/Utility";

export class EventObj{
    public name:string;
    public address:string;
    public description: string;
    public usersGoing: Array<number> = [];
    public marker: google.maps.Marker;
    public position: google.maps.LatLng;
    public geocoder: google.maps.Geocoder;
    public infoWindow: google.maps.InfoWindow;

    constructor(name: string, json: any, public home: HomePage, private googleMapService : GoogleMapsService){

        this.geocoder = new google.maps.Geocoder();
        this.name = name;
        this.address = json['address'].replace("\n","");
        if(this.address.slice(0,1) == "(")
            this.address = this.address.slice(1);
        if(this.address.slice(this.address.length-1) == ")")
            this.address = this.address.slice(0,this.address.length-1);

        this.position = null;
        if(json['location'] && json['location'].coordinates && json['location'].coordinates.length == 2)
            this.position = new google.maps.LatLng(json['location'].coordinates[0],json['location'].coordinates[1]);

        this.marker = new google.maps.Marker({
            animation: google.maps.Animation.DROP,
            position: this.position,
            // icon: {
            //     url: 'https://lh3.googleusercontent.com/NnjwhsRf5Fj_DGC3jotZEoJGMiVqvIROeaqpuRXdR32IVFmCMaRfzTqN-OwTcLHzz_Y=w170',
            //     scaledSize: new google.maps.Size(32,32)
            // }
        });
        if(json['category']) {
            var cats = localStorage.getItem(Utility.categories);
            var c = JSON.parse(cats);
            var cc = "";
            for(var i in c)
                if(c[i]["name"] == json['category']){
                    if(c[i]["imageBytes"])
                        cc = Utility.getHost().concat('/image?imagePath=' + encodeURI(c[i]["url"].replace("res:","")));
                    else
                        cc = c[i]["url"];
                    break;
                }
            this.marker = new google.maps.Marker({
                animation: google.maps.Animation.DROP,
                position: this.position,
                icon: {
                    url: cc,
                    scaledSize: new google.maps.Size(32,32)
                }
            });
        }
        if(!this.position) {
            this.position = new google.maps.LatLng(0,0);
            this.geocoder.geocode({'address': this.address}, (results, status) => {
                if (status == google.maps.GeocoderStatus.OK) {
                    this.position = results[0].geometry.location;
                    this.marker.setPosition(this.position);
                    this.marker.setMap(this.googleMapService.getMap());
                } else
                    console.log(status);
            });
        }else {
            this.marker.setPosition(this.position);
            this.marker.setMap(this.googleMapService.getMap());
        }

        this.description = json['description'];
        this.usersGoing = json['usersGoing'];
        this.infoWindow = new google.maps.InfoWindow({
            content:
                `
                <div *ngIf="marker.display">
                  Name: `+this.name+`<br/>
                  Description: `+this.description+`<br/>
                  Users Going: `+this.usersGoing.length+`<br/>
        
                  <button id="eventPage">Open Event Page</button>
                  `+this.isGoing()+`
                </div>
                <!--<button (click)="hideMarkerInfo()">Hide Info</button>-->
                `
        });

        this.marker.addListener('mouseover',()=>{
            var markerObj = this.googleMapService.openedMarker;
            if(markerObj.infoWindow)
                markerObj.infoWindow.close();
            this.infoWindow.open(this.googleMapService.getMap(),this.marker);
            markerObj.m = this;
            markerObj.marker = this.marker;
            markerObj.infoWindow = this.infoWindow;

            document.getElementById("eventPage").addEventListener('click',()=>{
                if(this.didClick)
                    return;
                this.didClick = true;
                home.clickObj(this.name);
            });

            if(this.home.isGoing(this.name))
                document.getElementById("going").addEventListener('click',()=>{
                    home.isGoing(this.name);
                });
            else
                document.getElementById("notGoing").addEventListener('click',()=>{
                    home.imNotGoingClick(this.name);
                });
        });
    }
    public didClick: boolean = false;
    isGoing():string{
        if(this.home && this.home.isGoing(this.name))
            return "<button id='going'>I'm going to this</button>";
        return "<button id='notGoing'>I'm not going to this</button>";
    }
}