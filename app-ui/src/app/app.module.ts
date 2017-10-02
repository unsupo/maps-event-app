import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import {IonicApp, IonicErrorHandler, IonicModule} from 'ionic-angular';
import {SplashScreen} from '@ionic-native/splash-screen';
import {StatusBar} from '@ionic-native/status-bar';

import {MyApp} from './app.component';
import {HomePage} from '../pages/home/home';
import {HttpModule} from "@angular/http";
import {AddModalPageModule} from "../pages/add-modal/add-modal.module";
import {ObjectPageModule} from "../pages/objects/object.module";
import {EventPageModule} from "../pages/event/event.module";
import {NguiMapModule} from "@ngui/map";
import { StompService } from 'ng2-stomp-service';
import {ChatService} from "../services/SocketService";
import {LoginPageModule} from "../pages/login/login.module";
import {LoginpagePageModule} from "../pages/loginpage/loginpage.module";
import {SignuppagePageModule} from "../pages/signuppage/signuppage.module";
import {SearchGoogleMapsPageModule} from "../pages/search-google-maps/search-google-maps.module";
import { Geolocation } from '@ionic-native/geolocation';
import {GoogleMaps} from "@ionic-native/google-maps";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {GoogleMapsService} from "../services/GoogleMapsService";
import {LoginService} from "../services/LoginService";
import {UserinfopagePageModule} from "../pages/userinfopage/userinfopage.module";

@NgModule({
    declarations: [
        MyApp,
        HomePage
    ],
    imports: [
        BrowserModule,
        ObjectPageModule,
        EventPageModule,
        HttpModule,
        AddModalPageModule,
        LoginPageModule,
        LoginpagePageModule,
        SignuppagePageModule,
        SearchGoogleMapsPageModule,
        UserinfopagePageModule,
        FormsModule,
        ReactiveFormsModule,
        NguiMapModule.forRoot({
            apiUrl: 'AIzaSyD7unYcjIEqiieCDx2Q9pz1GRh07uir_J0'
        }),
        IonicModule.forRoot(MyApp)
    ],
    bootstrap: [IonicApp],
    entryComponents: [
        MyApp,
        HomePage
    ],
    providers: [
        StatusBar,
        SplashScreen,
        StompService,
        ChatService,
        GoogleMapsService,
        LoginService,
        // CurrentLocationService,
        Geolocation,
        GoogleMaps,
        {provide: ErrorHandler, useClass: IonicErrorHandler}
    ]
})
export class AppModule {
}
