import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map'
import {StompService} from 'ng2-stomp-service';
import {EventObj} from "../objects/EventObj";
import {HomePage} from "../pages/home/home";
import {Utility} from "./Utility";
import {GoogleMapsService} from "./GoogleMapsService";

@Injectable()
export class ChatService {

    private v: string = 'init';
    private subscriptionMessaging: any;
    private subscriptionEvent: any;
    private data: any;
    public objs: Map<string, EventObj> = new Map();
    public map: google.maps.Map;
    public home: HomePage;

    constructor(private stomp: StompService, private googleMapService : GoogleMapsService) {
        //configuration
        stomp.configure({
            host: Utility.getHost().concat('/gs-guide-websocket'),
            debug: true,
            queue: {'init': false}
        });

        //start connection
        stomp.startConnect().then(() => {
            stomp.done(this.v);
            console.log('connected');

            //subscribe
            this.subscriptionMessaging = stomp.subscribe('/topic/messaging', res => {
                this.data = res;
            });
            this.subscriptionEvent = stomp.subscribe('/topic/addEvent', res => {
                this.parseResults(res);
            });
        });
    }

    public parseResults(res) {
        // this.objs = new Map();
        console.log(res);
        var json = res;
        if (typeof res != 'object')
            json = JSON.parse(res);
        for (var i in json)
            if (!this.objs.has(i))
                this.objs.set(i, new EventObj(i, json[i], this.home, this.googleMapService));
    }


    public sendMessage(data: string) {
        this.stomp.after(this.v).then(() => {
            this.stomp.send('/app/messages', data);
        });
    }

    public sendMessageWithDestination(data: string, destination: string) {
        this.stomp.after(this.v).then(() => {
            this.stomp.send(destination, data);
        });
    }

    public getStomp(): any {
        return this.stomp;
    }

    public getResponse(): any {
        return this.data;
    }

    public getEventData() {
        return this.objs;
    }

    //response
    public response = (data) => {
        return data;
    };

    setMap(gMap: google.maps.Map) {
        this.map = gMap;
    }

    setHome(home: HomePage) {
        this.home = home;
    }
}