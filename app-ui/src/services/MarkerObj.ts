import {EventObj} from "../objects/EventObj";

export class MarkerObj {
    display: boolean = false;
    lat: any;
    lng: any;
    event: any;
    m: EventObj;
    marker: google.maps.Marker;
    infoWindow: google.maps.InfoWindow;
}