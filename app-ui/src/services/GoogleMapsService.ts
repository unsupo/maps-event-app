import {Injectable} from "@angular/core";
import {Utility} from "./Utility";
import {MarkerObj} from "./MarkerObj";
import {AddModalPage} from "../pages/add-modal/add-modal";
import {ModalController, ToastController} from "ionic-angular";
import {Observable} from "rxjs/Observable";
import PlacesServiceStatus = google.maps.places.PlacesServiceStatus;

@Injectable()
export class GoogleMapsService {
    private map : google.maps.Map;
    private geocoder : google.maps.Geocoder;
    private defaultPosition : google.maps.LatLng;
    private currentLocation : google.maps.LatLng;
    private placesService : google.maps.places.PlacesService;

    private zoom : number = 4;
    private markersMap : Map<string,google.maps.Marker> = new Map();
    private searchBoxArray : Array<string> = new Array();

    public openedMarker : MarkerObj = new MarkerObj();
    private secondChild : HTMLDivElement;
    public isLockedOnCenter:boolean=false;
    private isTimerGoing:boolean=false;
    private canGetCurrentLocation : boolean = false;
    clickedOnYourLocation;

    private centerMarker : string = "centerMarker";
    private clickMarker : string = "clickMarker";

    constructor(private modalCtrl: ModalController, public toast: ToastController){
        // this.canGetCurrentLocation = this.currentLocationService.canGetCurrentLocation;
        // this.setCurrentLocation(currentLocationService.getCurrentLocation());
    }

    public getMapCenter():google.maps.LatLng{
        return this.getMap().getCenter();
    }
    public getMap() : google.maps.Map {
        if(!google.maps)
            return null;
        if(!this.map)
            return null;
        return this.map;
    }

    public setCurrentLocation(currentLocation:google.maps.LatLng):void{
        this.currentLocation = currentLocation;
        this.canGetCurrentLocation = true;
        var m = this.markersMap.get(this.centerMarker);
        if(!m)
            return;
        m.setMap(this.getMap());
        m.setPosition(this.currentLocation);
        if(this.isLockedOnCenter)
            this.getMap().setCenter(this.currentLocation);
    }

    public initialize(mapElementId : string):void{
        Observable.create((observer)=>{
            if (typeof google != 'undefined') {
                observer.next(true);
                observer.complete();
            }
        }).subscribe((res)=>this.init(mapElementId));
    }

    private init(mapElementId : string) : void {
        this.defaultPosition = new google.maps.LatLng(37.09024, -95.712891);

        var mapTag = document.getElementById(mapElementId);
        this.map = new google.maps.Map(mapTag, {
            center: this.defaultPosition,
            zoom: +this.zoom
        });
        this.geocoder = new google.maps.Geocoder();
        this.addCustomMarker(this.centerMarker, new google.maps.Marker({
            position: this.defaultPosition,
            animation: google.maps.Animation.DROP,
            icon: {
                url: 'assets/current_location.png',
                scaledSize: new google.maps.Size(32,32)
            }
        }));
        this.addDefaultListenerToMakerWithData(this.centerMarker,{
            map:this.getMap(),
            currentLocation:this.getCurrentLocation(),
            setAddress:this.getCurrentLocation(),
            setName:'Current Location'
        });
        this.placesService = new google.maps.places.PlacesService(this.getMap());
        this.setMapProperties();
        this.setUpSearchBox();
        this.addYourLocationButton();
        this.setUpMapListeners();
        this.setPlaceInfoWindow();
        this.setUpEventFilter();
        Observable.create(observer=>{
            if(document.getElementById('addFab')) {
                this.getMap().controls[google.maps.ControlPosition.RIGHT_CENTER].push(document.getElementById('addFab'));
                observer.next(); observer.complete();
            }
        });
        if(this.isLockedOnCenter)
            this.secondChild.style['background-position'] = '-144px 0';
    }

    private setPlaceInfoWindow(){
        this.getMap().addListener('click',(event)=>{
            if(event.latLng.lat() && event.latLng.lng())
                if(this.openedMarker.infoWindow)
                    this.openedMarker.infoWindow.close();
                var window = new google.maps.InfoWindow({
                    content: `Name: Clicked Location<br/>
                                         <button id="createEvent">Create Event Here</button>`
                });
                window.setPosition(event.latLng);
                window.open(this.map);
                this.waitForId("createEvent", 'click', ()=>{
                    let modal = this.modalCtrl.create(AddModalPage,{
                        map:this.getMap(),
                        currentLocation:event.latLng.lat()+event.latLng.lng(),
                        setAddress:"CLICKED_LOCATION",
                        setName:'Clicked Location'
                    });
                    modal.present();
                });
                this.openedMarker.infoWindow = window;
        });
        this.getMap().addListener('click', (event)=>{
            if(event.placeId){
                console.log(event.placeId);
                var place = this.placesService.getDetails({placeId: event.placeId},(place,status:PlacesServiceStatus)=>{
                    if(status == PlacesServiceStatus.OK){
                        if(this.openedMarker.infoWindow)
                            this.openedMarker.infoWindow.close();
                        var window = new google.maps.InfoWindow({
                            content: `Name: `+place.name+`<br/>
                                     <button id="createEvent">Create Event Here</button>`
                        });
                        window.setPosition(place.geometry.location);
                        window.open(this.map);
                        this.waitForId("createEvent", 'click', ()=>{
                            let modal = this.modalCtrl.create(AddModalPage,{
                                map:this.getMap(),
                                currentLocation:place.geometry.location,
                                setAddress:place.formatted_address,
                                setName:place.name
                            });
                            modal.present();
                        });
                        this.openedMarker.infoWindow = window;
                    }
                });
                event.stop();
            }
        });
    }

    private setMapProperties() {
        this.getMap().setOptions({
           // mapTypeControlOptions: {
            //satellite/or other view type
           //     position: google.maps.ControlPosition.RIGHT_CENTER
           // }
            mapTypeControl: true,
            mapTypeControlOptions: {
                style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
                position: google.maps.ControlPosition.TOP_LEFT
            },
            zoomControlOptions: {
                position: google.maps.ControlPosition.RIGHT_CENTER
            },
            streetViewControlOptions: {
                position: google.maps.ControlPosition.RIGHT_CENTER
            },
            rotateControl:true,
            rotateControlOptions: {
                position: google.maps.ControlPosition.RIGHT_CENTER
            },
            scaleControl:true,
            fullscreenControl:false,
            fullscreenControlOptions: {
                position: google.maps.ControlPosition.TOP_LEFT
            }
        });
    }


    public getCurrentLocation():google.maps.LatLng{
        if(this.currentLocation && this.currentLocation.lng() && this.currentLocation.lat())
            return this.currentLocation;
        return this.defaultPosition;
    }

    public addMarker(name: string) : void {
        this.markersMap.set(name, new google.maps.Marker({
            position: this.defaultPosition,
            animation: google.maps.Animation.DROP
        }));
    }
    public addCustomMarker(name: string, marker : google.maps.Marker) : void {
        this.markersMap.set(name,marker);
    }

    public addListenerToMarker(markerName : string, listenerName : string, event : ()=>void) : void{
        this.markersMap.get(markerName).addListener(listenerName, event);
    }

    public addDefaultListenerToMakerWithData(markerName : string, data : {}) : void {
        var marker = this.markersMap.get(markerName);
        this.addListenerToMarker(markerName, 'mouseover', ()=>{
            this.createInfoWindowObj(marker,data);
        });
    }

    private createInfoWindowObj(marker:google.maps.Marker,data:{}){
        if(this.openedMarker.infoWindow)
            this.openedMarker.infoWindow.close();
        var window = new google.maps.InfoWindow({
            content: `Name: `+data['setName']+`<br/>
                    <button id="createEvent">Create Event Here</button>
                    `
        });
        window.open(this.getMap(),marker);
        this.waitForId("createEvent", 'click', ()=>{
            let modal = this.modalCtrl.create(AddModalPage,data);
            modal.present();
        });
        this.openedMarker.marker = marker;
        this.openedMarker.infoWindow = window;
    }

    waitForId(id : string,listName :string, func : ()=>void){
        var doc = document.getElementById(id);
        if(doc)
            return doc.addEventListener(listName, func);
        setTimeout(()=>{
            this.waitForId(id,listName,func);
        }, 1000);
    }

    private setUpMapListeners() {
        google.maps.event.addListener(this.getMap(), 'zoom_changed',()=>{
            if(this.isLockedOnCenter)
                this.map.setCenter(this.getCurrentLocation());
        });
        google.maps.event.addListener(this.getMap(),'idle',()=>{
            this.saveInfo();
        });
        // google.maps.event.addListener(this.getMap(), 'click',()=>{
        //    //TODO when click on map add an event
        // });
    }

    public clearResults(){
        this.searchBoxArray.forEach((marker)=>{
            this.markersMap.get(marker).setMap(null);
            this.markersMap.delete(marker);
        });
        this.searchBoxArray = new Array();
    }

    private setUpSearchBox() {
        var input = <HTMLInputElement> document.getElementById('pac-input');
        var searchDiv = <HTMLDivElement> document.getElementById('search');

        var searchBox = new google.maps.places.SearchBox(input);
        this.getMap().controls[google.maps.ControlPosition.TOP_CENTER].push(searchDiv);
        this.getMap().addListener('bounds_changed',()=> {
            searchBox.setBounds(this.getMap().getBounds());
        });

        searchBox.addListener('places_changed',()=>{
            var places = searchBox.getPlaces();
            if (places.length == 0)
                return;

            // Clear out the old markers for the new search.
            this.searchBoxArray.forEach((marker)=> {
                this.markersMap.get(marker).setMap(null);
                this.markersMap.delete(marker);
            });
            this.searchBoxArray = new Array();

            // For each place, get the icon, name and location.
            var bounds = new google.maps.LatLngBounds();
            places.forEach((place)=> {
                if (!place.geometry) {
                    console.log("Returned place contains no geometry");
                    return;
                }
                var icon = { //this generates the icon specs for the different icons
                    url: place.icon,
                    size: new google.maps.Size(71, 71),
                    origin: new google.maps.Point(0, 0),
                    anchor: new google.maps.Point(17, 34),
                    scaledSize: new google.maps.Size(25, 25)
                };

                var m = new google.maps.Marker({
                    map: this.getMap(),
                    icon: icon,
                    title: place.name,
                    position: place.geometry.location
                });
                var name = Utility.uuid();
                this.addCustomMarker(name,m);
                this.addDefaultListenerToMakerWithData(name,{
                    map:this.getMap(),
                    currentLocation:place.geometry.location,
                    setAddress:place.geometry.location.toString(),
                    setName:place.name
                });

                // Create a marker for each place.
                this.searchBoxArray.push(name);


                if (place.geometry.viewport) {
                    // Only geocodes have viewport.
                    bounds.union(place.geometry.viewport);
                } else {
                    bounds.extend(place.geometry.location);
                }
            });
            this.getMap().fitBounds(bounds);
        });
    }

    private addYourLocationButton() : void {
        var controlDiv = document.createElement('div');

        var firstChild = document.createElement('button');
        firstChild.style.backgroundColor = '#fff';
        firstChild.style.border = 'none';
        firstChild.style.outline = 'none';
        firstChild.style.width = '28px';
        firstChild.style.height = '28px';
        firstChild.style.borderRadius = '2px';
        firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
        firstChild.style.cursor = 'pointer';
        firstChild.style.marginRight = '10px';
        firstChild.style.padding = '0';
        firstChild.title = 'Your Location';
        controlDiv.appendChild(firstChild);

        this.secondChild = document.createElement('div');
        this.secondChild.style.margin = '5px';
        this.secondChild.style.width = '18px';
        this.secondChild.style.height = '18px';
        this.secondChild.style.backgroundImage = 'url("assets/mylocation-sprite-2x.png")';
        this.secondChild.style.backgroundSize = '180px 18px';
        this.secondChild.style.backgroundPosition = '0 0';
        this.secondChild.style.backgroundRepeat = 'no-repeat';
        firstChild.appendChild(this.secondChild);

        google.maps.event.addListener(this.getMap(), 'drag', ()=> {
            if(this.isTimerGoing)
                return;
            this.secondChild.style['background-position'] = '0 0';
            this.isLockedOnCenter = false;
            localStorage.setItem('isLockedOnCenter','false');
        });

        firstChild.addEventListener('click', ()=> {
            if(!this.canGetCurrentLocation){
                this.toast.create({message:"Can't get gps location",duration:10000}).present();
                return;
            }

            var imgX = '0',
                animationInterval = setInterval(function () {
                    imgX = imgX === '-18' ? '0' : '-18';
                    this.secondChild.style['background-position'] = imgX+'px 0';
                }, 500); //this is for the flashing
            this.isTimerGoing = true;
            this.clickedOnYourLocation = setTimeout(()=>{this.isTimerGoing=false;},2000);

            this.markersMap.get(this.centerMarker).setPosition(this.getCurrentLocation());
            this.getMap().setCenter(this.getCurrentLocation());
            if(this.getMap().getZoom()<10)
                this.getMap().setZoom(10);
            localStorage.setItem('isLockedOnCenter','true');
            clearInterval(animationInterval);
            this.secondChild.style['background-position'] = '-144px 0';
            this.isLockedOnCenter = true;
        });

        controlDiv.tabIndex = 1;
        this.getMap().controls[google.maps.ControlPosition.RIGHT_CENTER].push(controlDiv);
    }

    private isEventFilterClicked : boolean = false;
    private setUpEventFilter() {
        var controlDiv = document.createElement('div');

        Observable.create((observer)=>{
            if (//document.getElementById('filterEvents') &&
                    document.getElementById('searchBox')) {
                observer.next(true);
                observer.complete();
            }
        }).subscribe((res)=>{
            // var firstChild = document.getElementById('filterEvents');
            var firstChild = document.createElement('div');
            firstChild.style.backgroundColor = '#ffffff';
            firstChild.style.border = 'none';
            firstChild.style.outline = 'none';
            firstChild.style.width = '28px';
            firstChild.style.height = '28px';
            firstChild.style.borderRadius = '2px';
            firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
            firstChild.style.cursor = 'pointer';
            firstChild.style.marginRight = '10px';
            firstChild.style.padding = '0';
            firstChild.title = 'Filter Events';
            firstChild.style.opacity = '0.85';
            controlDiv.appendChild(firstChild);

            var secondChild = document.createElement('div');
            secondChild.style.margin = '5px';
            secondChild.style.width = '18px';
            secondChild.style.height = '18px';
            secondChild.style.position = 'relative';
            secondChild.style.top = '5px';
            secondChild.style.backgroundImage = 'url("assets/icon/Magnifying_glass_icon.svg.png")';
            secondChild.style.backgroundSize = '100% 100%';
            secondChild.style.backgroundPosition = '0 0';
            secondChild.style.backgroundRepeat = 'no-repeat';
            firstChild.appendChild(secondChild);

            var thirdChild = document.getElementById('searchBox');
            thirdChild.style.visibility = 'hidden';
            thirdChild.style.marginRight = '5px';
            thirdChild.style.width = '180px';
            thirdChild.style.height = '180px';
            thirdChild.style.backgroundColor = '#222222';
            thirdChild.style.position = 'relative';
            thirdChild.style.top = '5px';
            thirdChild.style.right = '150px';
            thirdChild.style.opacity = '0.85';
            firstChild.appendChild(thirdChild);

            secondChild.addEventListener('click', ()=> {
                if(!this.isEventFilterClicked) {
                    firstChild.style.backgroundColor = '#222222';
                    thirdChild.style.visibility = 'visible';
                }else{
                    firstChild.style.backgroundColor = '#ffffff';
                    thirdChild.style.visibility = 'hidden';
                }

                this.isEventFilterClicked = !this.isEventFilterClicked;
            });

            controlDiv.tabIndex = 1;
            this.getMap().controls[google.maps.ControlPosition.TOP_RIGHT].push(controlDiv);
        });
    }

    private saveInfo() : void {
        if(!this.canGetCurrentLocation)
            this.defaultPosition = this.getMap().getCenter();
        if(this.getMap()) {
            localStorage.setItem('zoom',this.getMap().getZoom()+"");
            if(this.getMap().getCenter())
            localStorage.setItem('viewLocation', JSON.stringify({
                location: {
                    latitude: this.getMap().getCenter().lat(),
                    longitude: this.getMap().getCenter().lng()
                }
            }));
        }
        localStorage.setItem('location',JSON.stringify({
            location: {
                latitude: this.getCurrentLocation().lat(),
                longitude: this.getCurrentLocation().lng()
            }}));
    }
}